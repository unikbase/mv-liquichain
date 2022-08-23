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

    private Boolean IsSellerInfoUpdatesEnabled;

    private Boolean isChatEnabledFromProfilePage;

    private Boolean isEmailNotificationsEnabled;

    private Boolean isOrderUpdatesEnabled;

    private Boolean isAutoReplyEnabled;

    private Boolean isEmailNotificationsEnabled;

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

    public Boolean getIsSellerInfoUpdatesEnabled() {
        return IsSellerInfoUpdatesEnabled;
    }

    public void setIsSellerInfoUpdatesEnabled(Boolean IsSellerInfoUpdatesEnabled) {
        this.IsSellerInfoUpdatesEnabled = IsSellerInfoUpdatesEnabled;
    }

    public Boolean getIsChatEnabledFromProfilePage() {
        return isChatEnabledFromProfilePage;
    }

    public void setIsChatEnabledFromProfilePage(Boolean isChatEnabledFromProfilePage) {
        this.isChatEnabledFromProfilePage = isChatEnabledFromProfilePage;
    }

    public Boolean getIsEmailNotificationsEnabled() {
        return isEmailNotificationsEnabled;
    }

    public void setIsEmailNotificationsEnabled(Boolean isEmailNotificationsEnabled) {
        this.isEmailNotificationsEnabled = isEmailNotificationsEnabled;
    }

    public Boolean getIsOrderUpdatesEnabled() {
        return isOrderUpdatesEnabled;
    }

    public void setIsOrderUpdatesEnabled(Boolean isOrderUpdatesEnabled) {
        this.isOrderUpdatesEnabled = isOrderUpdatesEnabled;
    }

    public Boolean getIsAutoReplyEnabled() {
        return isAutoReplyEnabled;
    }

    public void setIsAutoReplyEnabled(Boolean isAutoReplyEnabled) {
        this.isAutoReplyEnabled = isAutoReplyEnabled;
    }

    public Boolean getIsEmailNotificationsEnabled() {
        return isEmailNotificationsEnabled;
    }

    public void setIsEmailNotificationsEnabled(Boolean isEmailNotificationsEnabled) {
        this.isEmailNotificationsEnabled = isEmailNotificationsEnabled;
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
