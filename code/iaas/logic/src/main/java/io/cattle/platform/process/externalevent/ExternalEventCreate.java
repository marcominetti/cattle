package io.cattle.platform.process.externalevent;

import static io.cattle.platform.process.externalevent.ExternalEventConstants.*;
import static io.cattle.platform.core.model.tables.AgentTable.*;
import io.cattle.platform.core.constants.CommonStatesConstants;
import io.cattle.platform.core.dao.AccountDao;
import io.cattle.platform.core.dao.GenericResourceDao;
import io.cattle.platform.core.dao.HostDao;
import io.cattle.platform.core.dao.StoragePoolDao;
import io.cattle.platform.core.dao.VolumeDao;
import io.cattle.platform.core.model.Agent;
import io.cattle.platform.core.model.ExternalEvent;
import io.cattle.platform.core.model.Host;
import io.cattle.platform.core.model.StoragePool;
import io.cattle.platform.core.model.StoragePoolHostMap;
import io.cattle.platform.core.model.Volume;
import io.cattle.platform.engine.handler.HandlerResult;
import io.cattle.platform.engine.process.ProcessInstance;
import io.cattle.platform.engine.process.ProcessState;
import io.cattle.platform.engine.process.impl.ProcessCancelException;
import io.cattle.platform.json.JsonMapper;
import io.cattle.platform.lock.LockCallbackNoReturn;
import io.cattle.platform.lock.LockManager;
import io.cattle.platform.object.meta.ObjectMetaDataManager;
import io.cattle.platform.object.process.StandardProcess;
import io.cattle.platform.object.resource.ResourceMonitor;
import io.cattle.platform.object.util.DataAccessor;
import io.cattle.platform.object.util.DataUtils;
import io.cattle.platform.process.base.AbstractDefaultProcessHandler;
import io.cattle.platform.process.common.util.ProcessUtils;
import io.cattle.platform.util.type.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class ExternalEventCreate extends AbstractDefaultProcessHandler {

    public static final String FIELD_AGENT_ID = "agentId";

    private static final Logger log = LoggerFactory.getLogger(ExternalEventCreate.class);

    @Inject
    AccountDao accountDao;
    @Inject
    LockManager lockManager;
    @Inject
    ResourceMonitor resourceMonitor;
    @Inject
    GenericResourceDao resourceDao;
    @Inject
    StoragePoolDao storagePoolDao;
    @Inject
    VolumeDao volumeDao;
    @Inject
    HostDao hostDao;
    @Inject
    JsonMapper jsonMapper;

    @Override
    public HandlerResult handle(ProcessState state, ProcessInstance process) {
        ExternalEvent event = (ExternalEvent)state.getResource();

        if (StringUtils.isEmpty(event.getExternalId())) {
            log.warn("External event doesn't have an external id: {}", event);
            return null;
        }

        if (ExternalEventConstants.KIND_VOLUME_EVENT.equals(event.getKind())) {
            handleVolumeEvent(event, state, process);
        } else if (ExternalEventConstants.KIND_STORAGE_POOL_EVENT.equals(event.getKind())) {
            handleStoragePoolEvent(event, state, process);
        } else {
            new IllegalStateException("Unknown external event type: " + event.getKind());
        }

        return null;
    }

    protected void handleVolumeEvent(final ExternalEvent event, ProcessState state, ProcessInstance process) {
        lockManager.lock(new ExternalEventLock(VOLUME_POOL_LOCK_NAME, event.getAccountId(), event.getExternalId()), new LockCallbackNoReturn() {
            @Override
            public void doWithLockNoResult() {
                String spExtId = DataAccessor.fieldString(event, FIELD_SP_EXT_ID);
                StoragePool storagePool = storagePoolDao.findStoragePoolByExternalId(event.getAccountId(), spExtId);
                if (storagePool == null) {
                    log.warn("Unknown storage pool. Returning. External id: {}", spExtId);
                    return;
                }
                Volume volume = volumeDao.findVolumeByExternalId(storagePool.getId(), event.getExternalId());
                switch (event.getEventType()) {
                case ExternalEventConstants.TYPE_VOLUME_CREATE:
                    if (volume == null) {
                        Map<String, Object> volumeData = CollectionUtils.toMap(DataUtils.getFields(event).get(FIELD_VOLUME));
                        if (volumeData.isEmpty()) {
                            log.warn("Empty volume for externalVolumeEvent: {}. StoragePool: {}", event, volumeData);
                            return;
                        }

                        volumeData.put(ObjectMetaDataManager.ACCOUNT_FIELD, event.getAccountId());
                        volumeData.put(FIELD_ATTACHED_STATE, CommonStatesConstants.INACTIVE);
                        volumeData.put(FIELD_ALLOC_STATE, CommonStatesConstants.ACTIVE);
                        volumeData.put(FIELD_ZONE_ID, 1L);
                        volumeData.put(FIELD_DEV_NUM, -1);

                        try {
                            volumeDao.createVolumeInStoragePool(volumeData, storagePool);
                        } catch (ProcessCancelException e) {
                            log.info("Create process cancelled for volumeData {}. ProcessCancelException message: {}", volumeData, e.getMessage());
                        }
                    }
                    break;
                case ExternalEventConstants.TYPE_VOLUME_DELETE:
                    if (volume != null) {
                        try {
                            objectProcessManager.scheduleStandardProcess(StandardProcess.DEACTIVATE, volume,
                                    ProcessUtils.chainInData(new HashMap<String, Object>(), PROC_VOL_DEACTIVATE, PROC_VOL_REMOVE));
                        } catch (ProcessCancelException e) {
                            log.info("Deactivate and remove process cancelled for volume {}. ProcessCancelException message: {}", volume, e.getMessage());
                        }
                    }
                    break;
                default:
                    log.error("Unknown event type: {} for event {}", event.getEventType(), event);
                    return;
                }
            }
        });
    }

    protected void handleStoragePoolEvent(final ExternalEvent event, ProcessState state, ProcessInstance process) {
        lockManager.lock(new ExternalEventLock(STORAGE_POOL_LOCK_NAME, event.getAccountId(), event.getExternalId()), new LockCallbackNoReturn() {
            @Override
            public void doWithLockNoResult() {
                StoragePool storagePool = storagePoolDao.findStoragePoolByExternalId(event.getAccountId(), event.getExternalId());
                if (storagePool == null) {
                    Map<String, Object> spData = CollectionUtils.toMap(DataUtils.getFields(event).get(FIELD_STORAGE_POOL));
                    if (spData.isEmpty()) {
                        log.warn("Null or empty storagePool for externalStoragePoolEvent: {}. StoragePool: {}", event, spData);
                        return;
                    }

                    Agent agent = objectManager.findOne(Agent.class, AGENT.ACCOUNT_ID, event.getReportedAccountId(), AGENT.STATE, CommonStatesConstants.ACTIVE);
                    spData.put(FIELD_AGENT_ID, agent.getId());
                    spData.put(ObjectMetaDataManager.ACCOUNT_FIELD, event.getAccountId());
                    spData.put(FIELD_ZONE_ID, 1L);

                    try {
                        storagePool = resourceDao.createAndSchedule(StoragePool.class, spData);
                    } catch (ProcessCancelException e) {
                        log.info("Create process cancelled for storagePool {}. ProcessCancelException message: {}", storagePool, e.getMessage());
                    }
                }

                List<String> hostUuids = new ArrayList<String>();
                for (Object item : CollectionUtils.toList(DataUtils.getFields(event).get(FIELD_HOST_UUIDS))) {
                    if (item != null)
                        hostUuids.add(item.toString());
                }

                Map<Long, StoragePoolHostMap> maps = constructStoragePoolMaps(storagePool, hostUuids);
                try {
                    removeOldMaps(storagePool, maps);
                    createNewMaps(storagePool, maps);
                } catch (ProcessCancelException e) {
                    log.info("Process cancelled while syncing volumes to storagePool {}. ProcessCancelException message: {}", storagePool, e.getMessage());
                }
            }
        });
    }

    protected void createNewMaps(StoragePool storagePool, Map<Long, StoragePoolHostMap> maps) {
        for (StoragePoolHostMap m : maps.values()) {
            storagePoolDao.createStoragePoolHostMap(m);
        }
    }

    protected void removeOldMaps(StoragePool storagePool, Map<Long, StoragePoolHostMap> newMaps) {
        List<? extends StoragePoolHostMap> existingMaps = storagePoolDao.findMapsToRemove(storagePool.getId());
        List<StoragePoolHostMap> toRemove = new ArrayList<StoragePoolHostMap>();

        for (StoragePoolHostMap m : existingMaps) {
            if (!newMaps.containsKey(m.getHostId())) {
                toRemove.add(m);
            }
        }

        for (StoragePoolHostMap m : toRemove) {
            StoragePoolHostMap remove = storagePoolDao.findNonremovedMap(m.getStoragePoolId(), m.getHostId());
            if (remove != null) {
                objectProcessManager.scheduleStandardProcess(StandardProcess.REMOVE, remove, null);
            }
        }
    }

    protected Map<Long, StoragePoolHostMap> constructStoragePoolMaps(StoragePool storagePool, List<String> hostUuids) {
        List<? extends Host> hosts = hostDao.getHosts(storagePool.getAccountId(), hostUuids);
        Map<Long, StoragePoolHostMap> maps = new HashMap<Long, StoragePoolHostMap>();
        for (Host h : hosts) {
            StoragePoolHostMap sphm = objectManager.newRecord(StoragePoolHostMap.class);
            sphm.setHostId(h.getId());
            sphm.setStoragePoolId(storagePool.getId());
            maps.put(h.getId(), sphm);
        }
        return maps;
    }
}