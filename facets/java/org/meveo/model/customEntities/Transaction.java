package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;
import java.util.List;
import org.meveo.model.persistence.DBStorageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Transaction implements CustomEntity {

    public Transaction() {
    }

    public Transaction(String uuid) {
        this.uuid = uuid;
    }

    private String uuid;

    @JsonIgnore()
    private DBStorageType storages;

    @JsonProperty(required = true)
    private String signedHash;

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

    public String getSignedHash() {
        return signedHash;
    }

    public void setSignedHash(String signedHash) {
        this.signedHash = signedHash;
    }

    @Override()
    public String getCetCode() {
        return "Transaction";
    }
}
