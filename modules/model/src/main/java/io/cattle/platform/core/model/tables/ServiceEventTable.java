/*
 * This file is generated by jOOQ.
*/
package io.cattle.platform.core.model.tables;


import io.cattle.platform.core.model.CattleTable;
import io.cattle.platform.core.model.Keys;
import io.cattle.platform.core.model.tables.records.ServiceEventRecord;
import io.cattle.platform.db.jooq.converter.DataConverter;
import io.cattle.platform.db.jooq.converter.DateConverter;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.3"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ServiceEventTable extends TableImpl<ServiceEventRecord> {

    private static final long serialVersionUID = -1713622364;

    /**
     * The reference instance of <code>cattle.service_event</code>
     */
    public static final ServiceEventTable SERVICE_EVENT = new ServiceEventTable();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ServiceEventRecord> getRecordType() {
        return ServiceEventRecord.class;
    }

    /**
     * The column <code>cattle.service_event.id</code>.
     */
    public final TableField<ServiceEventRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>cattle.service_event.name</code>.
     */
    public final TableField<ServiceEventRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

    /**
     * The column <code>cattle.service_event.account_id</code>.
     */
    public final TableField<ServiceEventRecord, Long> ACCOUNT_ID = createField("account_id", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>cattle.service_event.kind</code>.
     */
    public final TableField<ServiceEventRecord, String> KIND = createField("kind", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

    /**
     * The column <code>cattle.service_event.uuid</code>.
     */
    public final TableField<ServiceEventRecord, String> UUID = createField("uuid", org.jooq.impl.SQLDataType.VARCHAR.length(128).nullable(false), this, "");

    /**
     * The column <code>cattle.service_event.description</code>.
     */
    public final TableField<ServiceEventRecord, String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.VARCHAR.length(1024), this, "");

    /**
     * The column <code>cattle.service_event.state</code>.
     */
    public final TableField<ServiceEventRecord, String> STATE = createField("state", org.jooq.impl.SQLDataType.VARCHAR.length(128).nullable(false), this, "");

    /**
     * The column <code>cattle.service_event.created</code>.
     */
    public final TableField<ServiceEventRecord, Date> CREATED = createField("created", org.jooq.impl.SQLDataType.TIMESTAMP, this, "", new DateConverter());

    /**
     * The column <code>cattle.service_event.removed</code>.
     */
    public final TableField<ServiceEventRecord, Date> REMOVED = createField("removed", org.jooq.impl.SQLDataType.TIMESTAMP, this, "", new DateConverter());

    /**
     * The column <code>cattle.service_event.remove_time</code>.
     */
    public final TableField<ServiceEventRecord, Date> REMOVE_TIME = createField("remove_time", org.jooq.impl.SQLDataType.TIMESTAMP, this, "", new DateConverter());

    /**
     * The column <code>cattle.service_event.data</code>.
     */
    public final TableField<ServiceEventRecord, Map<String,Object>> DATA = createField("data", org.jooq.impl.SQLDataType.CLOB, this, "", new DataConverter());

    /**
     * The column <code>cattle.service_event.host_id</code>.
     */
    public final TableField<ServiceEventRecord, Long> HOST_ID = createField("host_id", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>cattle.service_event.healthcheck_uuid</code>.
     */
    public final TableField<ServiceEventRecord, String> HEALTHCHECK_UUID = createField("healthcheck_uuid", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

    /**
     * The column <code>cattle.service_event.instance_id</code>.
     */
    public final TableField<ServiceEventRecord, Long> INSTANCE_ID = createField("instance_id", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>cattle.service_event.reported_health</code>.
     */
    public final TableField<ServiceEventRecord, String> REPORTED_HEALTH = createField("reported_health", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

    /**
     * The column <code>cattle.service_event.external_timestamp</code>.
     */
    public final TableField<ServiceEventRecord, Long> EXTERNAL_TIMESTAMP = createField("external_timestamp", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>cattle.service_event.creator_id</code>.
     */
    public final TableField<ServiceEventRecord, Long> CREATOR_ID = createField("creator_id", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * Create a <code>cattle.service_event</code> table reference
     */
    public ServiceEventTable() {
        this("service_event", null);
    }

    /**
     * Create an aliased <code>cattle.service_event</code> table reference
     */
    public ServiceEventTable(String alias) {
        this(alias, SERVICE_EVENT);
    }

    private ServiceEventTable(String alias, Table<ServiceEventRecord> aliased) {
        this(alias, aliased, null);
    }

    private ServiceEventTable(String alias, Table<ServiceEventRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return CattleTable.CATTLE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<ServiceEventRecord, Long> getIdentity() {
        return Keys.IDENTITY_SERVICE_EVENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<ServiceEventRecord> getPrimaryKey() {
        return Keys.KEY_SERVICE_EVENT_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<ServiceEventRecord>> getKeys() {
        return Arrays.<UniqueKey<ServiceEventRecord>>asList(Keys.KEY_SERVICE_EVENT_PRIMARY, Keys.KEY_SERVICE_EVENT_IDX_SERVICE_EVENT_UUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<ServiceEventRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<ServiceEventRecord, ?>>asList(Keys.FK_SERVICE_EVENT__ACCOUNT_ID, Keys.FK_SERVICE_EVENT__HOST_ID, Keys.FK_SERVICE_EVENT__INSTANCE_ID, Keys.FK_SERVICE_EVENT__CREATOR_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceEventTable as(String alias) {
        return new ServiceEventTable(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ServiceEventTable rename(String name) {
        return new ServiceEventTable(name, null);
    }
}
