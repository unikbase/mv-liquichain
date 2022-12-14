package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;
import java.util.List;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.customEntities.VerifiedPhoneNumber;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Address implements CustomEntity {

    public Address() {
    }

    public Address(String uuid) {
        this.uuid = uuid;
    }

    private String uuid;

    @JsonIgnore()
    private DBStorageType storages;

    private String country;

    private VerifiedPhoneNumber phoneNumber;

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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public VerifiedPhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(VerifiedPhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override()
    public String getCetCode() {
        return "Address";
    }
}
