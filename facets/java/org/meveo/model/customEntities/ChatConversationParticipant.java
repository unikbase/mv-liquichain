package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;
import java.util.List;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.customEntities.ChatConversation;
import org.meveo.model.customEntities.Wallet;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ChatConversationParticipant implements CustomEntity {

    public ChatConversationParticipant() {
    }

    public ChatConversationParticipant(String uuid) {
        this.uuid = uuid;
    }

    private String uuid;

    @JsonIgnore()
    private DBStorageType storages;

    private ChatConversation chatConversation;

    private Wallet participant;

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

    public ChatConversation getChatConversation() {
        return chatConversation;
    }

    public void setChatConversation(ChatConversation chatConversation) {
        this.chatConversation = chatConversation;
    }

    public Wallet getParticipant() {
        return participant;
    }

    public void setParticipant(Wallet participant) {
        this.participant = participant;
    }

    @Override()
    public String getCetCode() {
        return "ChatConversationParticipant";
    }
}
