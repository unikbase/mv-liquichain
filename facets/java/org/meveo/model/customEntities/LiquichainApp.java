package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;
import java.util.List;
import org.meveo.model.persistence.DBStorageType;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class LiquichainApp implements CustomEntity {

    public LiquichainApp() {
    }

    public LiquichainApp(String uuid) {
        this.uuid = uuid;
    }

    private String uuid;

    @JsonIgnore()
    private DBStorageType storages;

    private Map<String, String> contractMethodHandlers = new HashMap<>();

    private String upgradeRules;

    private String hexCode;

    private String previousHash;

    @JsonProperty(required = true)
    private String name;

    private String abi;

    private String description;

    private String registrationRules;

    private String iconUrl;

    private Instant creationDate;

    private String version;

    @JsonProperty(required = true)
    private String shortCode;

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

    public Map<String, String> getContractMethodHandlers() {
        return contractMethodHandlers;
    }

    public void setContractMethodHandlers(Map<String, String> contractMethodHandlers) {
        this.contractMethodHandlers = contractMethodHandlers;
    }

    public String getUpgradeRules() {
        return upgradeRules;
    }

    public void setUpgradeRules(String upgradeRules) {
        this.upgradeRules = upgradeRules;
    }

    public String getHexCode() {
        return hexCode;
    }

    public void setHexCode(String hexCode) {
        this.hexCode = hexCode;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbi() {
        return abi;
    }

    public void setAbi(String abi) {
        this.abi = abi;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRegistrationRules() {
        return registrationRules;
    }

    public void setRegistrationRules(String registrationRules) {
        this.registrationRules = registrationRules;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    @Override()
    public String getCetCode() {
        return "LiquichainApp";
    }
}
