package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;
import java.util.List;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.customEntities.VerifiedEmail;
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

    private String accountHash;

    private String publicInfo;

    private Boolean verified;

    private String applicationInstanceUUID;

    private String publicKey;

    private String privateKey;

    private VerifiedEmail emailAddress;

    private VerifiedPhoneNumber phoneNumber;

    @JsonProperty(required = true)
    private LiquichainApp application;

    private String balance;

    private String hexHash;

    @JsonProperty(required = true)
    private String name;

    private String keyPair;

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

    public String getPublicInfo() {
        return publicInfo;
    }

    public void setPublicInfo(String publicInfo) {
        this.publicInfo = publicInfo;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
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

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public VerifiedEmail getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(VerifiedEmail emailAddress) {
        this.emailAddress = emailAddress;
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

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getHexHash() {
        return hexHash;
    }

    public void setHexHash(String hexHash) {
        this.hexHash = hexHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(String keyPair) {
        this.keyPair = keyPair;
    }

    @Override()
    public String getCetCode() {
        return "Wallet";
    }
}
