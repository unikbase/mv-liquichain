package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;
import java.util.List;
import org.meveo.model.persistence.DBStorageType;
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

    private String hexCode;

    @JsonProperty(required = true)
    private String name;

    private String registrationRules;

    private String iconUrl;

    private Instant creationDate;

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

    public String getHexCode() {
        return hexCode;
    }

    public void setHexCode(String hexCode) {
        this.hexCode = hexCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
