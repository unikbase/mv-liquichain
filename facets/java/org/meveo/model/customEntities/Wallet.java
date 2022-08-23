package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;
import java.util.List;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.customEntities.VerifiedPhoneNumber;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Wallet implements CustomEntity {

    public Wallet() {
    }

    public Wallet(String uuid) {
        this.uuid = uuid;
    }

    private String uuid;

    @JsonIgnore()
    private DBStorageType storages;

    private VerifiedPhoneNumber phoneNumber;

    private String test;

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

    public VerifiedPhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(VerifiedPhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    @Override()
    public String getCetCode() {
        return "Wallet";
    }
}
