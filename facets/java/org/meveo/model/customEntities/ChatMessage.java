package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;
import java.util.List;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.customEntities.Wallet;
import org.meveo.model.customEntities.ChatConversation;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ChatMessage implements CustomEntity {

    public ChatMessage() {
    }

    public ChatMessage(String uuid) {
        this.uuid = uuid;
    }

    private String uuid;

    @JsonIgnore()
    private DBStorageType storages;

    private Wallet sender;

    private ChatConversation chatConversation;

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

    public Wallet getSender() {
        return sender;
    }

    public void setSender(Wallet sender) {
        this.sender = sender;
    }

    public ChatConversation getChatConversation() {
        return chatConversation;
    }

    public void setChatConversation(ChatConversation chatConversation) {
        this.chatConversation = chatConversation;
    }

    @Override()
    public String getCetCode() {
        return "ChatMessage";
    }
}
