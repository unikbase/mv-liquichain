package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;
import java.util.List;
import org.meveo.model.persistence.DBStorageType;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class BlockedUser implements CustomEntity {

    public BlockedUser() {
    }

    public BlockedUser(String uuid) {
        this.uuid = uuid;
    }

    private String uuid;

    @JsonIgnore()
    private DBStorageType storages;

    private Instant blockDate;

    @Override()
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public DBStorageType getStorages() {
        return storages;
    }

    public void setStorages(DBStorageType storages) {
        this.storages = storages;
    }

    public Instant getBlockDate() {
        return blockDate;
    }

    public void setBlockDate(Instant blockDate) {
        this.blockDate = blockDate;
    }

    @Override()
    public String getCetCode() {
        return "BlockedUser";
    }
}