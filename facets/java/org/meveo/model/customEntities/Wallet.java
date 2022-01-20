package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;
import java.util.List;
import org.meveo.model.persistence.DBStorageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.meveo.model.customEntities.LiquichainApp;
import org.meveo.model.customEntities.GroupPurchase;
import java.util.ArrayList;
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

    private String accountHash;

    @JsonProperty(required = true)
    private LiquichainApp application;

    private String balance;

    @JsonProperty(required = true)
    private String name;

    private String publicInfo;

    private String applicationInstanceUUID;

    private String publicKey;

    private List<GroupPurchase> groupPurchases = new ArrayList<>();

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

    public String getAccountHash() {
        return accountHash;
    }

    public void setAccountHash(String accountHash) {
        this.accountHash = accountHash;
    }

    public LiquichainApp getApplication() {
        return application;
    }

    public void setApplication(LiquichainApp application) {
        this.application = application;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublicInfo() {
        return publicInfo;
    }

    public void setPublicInfo(String publicInfo) {
        this.publicInfo = publicInfo;
    }

    public String getApplicationInstanceUUID() {
        return applicationInstanceUUID;
    }

    public void setApplicationInstanceUUID(String applicationInstanceUUID) {
        this.applicationInstanceUUID = applicationInstanceUUID;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public List<GroupPurchase> getGroupPurchases() {
        return groupPurchases;
    }

    public void setGroupPurchases(List<GroupPurchase> groupPurchases) {
        this.groupPurchases = groupPurchases;
    }

    @Override()
    public String getCetCode() {
        return "Wallet";
    }
}
