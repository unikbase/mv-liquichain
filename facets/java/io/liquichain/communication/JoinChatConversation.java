package io.liquichain.communication;

import java.util.Map;

import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.exception.EntityDoesNotExistsException;

import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.storage.Repository;
import org.meveo.service.storage.RepositoryService;

import org.meveo.model.customEntities.Wallet;
import org.meveo.model.customEntities.ChatConversation;
import org.meveo.model.customEntities.ChatConversationParticipant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoinChatConversation extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(JoinChatConversation.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();

    private String chatConversationId;
    private String participantWalletId;

    private String result;

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);

        chatConversationId = normalizeHash(chatConversationId);
        participantWalletId = normalizeHash(participantWalletId);

        ChatConversation chatConversation = null;
        Wallet participantWallet = null;

        try {
            chatConversation = crossStorageApi.find(defaultRepo, chatConversationId, ChatConversation.class);
        } catch (EntityDoesNotExistsException ex) {
            String errorMessage = "Failed to find ChatConversation with hash = " + chatConversationId;
            result = "{\"status\": \"failed\", \"result\": \"" + errorMessage + "\"}";
            LOG.error(errorMessage, ex);
            return;
        }

        try {
            participantWallet = crossStorageApi.find(defaultRepo, participantWalletId, Wallet.class);
        } catch (EntityDoesNotExistsException ex) {
            String errorMessage = "Failed to find participant Wallet with hash = " + participantWalletId;
            result = "{\"status\": \"failed\", \"result\": \"" + errorMessage + "\"}";
            LOG.error(errorMessage, ex);
            return;
        }

        try {
            ChatConversationParticipant participant = new ChatConversationParticipant();
            participant.setParticipant(participantWallet);
            participant.setChatConversation(chatConversation);

            String uuid = crossStorageApi.createOrUpdate(defaultRepo, participant);

            LOG.info("Chat Conversation Participant created with Id: " + uuid);
            result = "{\"status\": \"success\", \"result\": \"" + uuid + "\"}";

        } catch (Exception ex) {
            String errorMessage = "Failed to Join Chat Conversation with chatConversationId = " + chatConversationId + " participantWalletId = " + participantWalletId;
            result = "{\"status\": \"failed\", \"result\": \"" + errorMessage + "\"}";
            LOG.error(errorMessage, ex);
        }
    }

    private String normalizeHash(String hash) {
        return hash.startsWith("0x") ? hash.substring(2).toLowerCase() : hash.toLowerCase();
    }

    public void setChatConversationId(String chatConversationId) {
        this.chatConversationId = chatConversationId;
    }

    public void setParticipantWalletId(String participantWalletId) {
        this.participantWalletId = participantWalletId;
    }

    public String getResult() {
        return this.result;
    }
}