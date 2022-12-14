package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;
import java.util.List;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.customEntities.Wallet;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserConfiguration implements CustomEntity {

    public UserConfiguration() {
    }

    public UserConfiguration(String uuid) {
        this.uuid = uuid;
    }

    private String uuid;

    @JsonIgnore()
    private DBStorageType storages;

    private String autoReplyMessage;

    private Boolean isChatNotificationsEnabled;

    private Boolean isOrderUpdatesEnabled;

    private Boolean isSellerInfoUpdatesEnabled;

    private Wallet user;

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

    public String getAutoReplyMessage() {
        return autoReplyMessage;
    }

    public void setAutoReplyMessage(String autoReplyMessage) {
        this.autoReplyMessage = autoReplyMessage;
    }

    public Boolean getIsChatNotificationsEnabled() {
        return isChatNotificationsEnabled;
    }

    public void setIsChatNotificationsEnabled(Boolean isChatNotificationsEnabled) {
        this.isChatNotificationsEnabled = isChatNotificationsEnabled;
    }

    public Boolean getIsOrderUpdatesEnabled() {
        return isOrderUpdatesEnabled;
    }

    public void setIsOrderUpdatesEnabled(Boolean isOrderUpdatesEnabled) {
        this.isOrderUpdatesEnabled = isOrderUpdatesEnabled;
    }

    public Boolean getIsSellerInfoUpdatesEnabled() {
        return isSellerInfoUpdatesEnabled;
    }

    public void setIsSellerInfoUpdatesEnabled(Boolean isSellerInfoUpdatesEnabled) {
        this.isSellerInfoUpdatesEnabled = isSellerInfoUpdatesEnabled;
    }

    public Wallet getUser() {
        return user;
    }

    public void setUser(Wallet user) {
        this.user = user;
    }

    @Override()
    public String getCetCode() {
        return "UserConfiguration";
    }
}
