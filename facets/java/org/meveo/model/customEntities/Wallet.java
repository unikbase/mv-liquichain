package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;
import java.util.List;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.customEntities.VerifiedPhoneNumber;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.meveo.model.customEntities.LiquichainApp;
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

    @JsonProperty(required = true)
    private LiquichainApp application;

    @JsonProperty(required = true)
    private String name;

    private String publicKey;

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

    public LiquichainApp getApplication() {
        return application;
    }

    public void setApplication(LiquichainApp application) {
        this.application = application;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override()
    public String getCetCode() {
        return "Wallet";
    }
}
