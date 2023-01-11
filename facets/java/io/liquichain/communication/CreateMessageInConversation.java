package io.liquichain.communication;

import java.util.Map;
import java.time.Instant;

import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.storage.Repository;
import org.meveo.service.storage.RepositoryService;
import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.exception.EntityDoesNotExistsException;

import org.meveo.model.customEntities.ChatConversation;
import org.meveo.model.customEntities.ChatConversationParticipant;
import org.meveo.model.customEntities.ChatMessage;
import org.meveo.model.customEntities.Wallet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateMessageInConversation extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(CreateMessageInConversation.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();

    private String chatConversationId;
    private String senderWalletId;
    private String message;
    private String result;

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);

        chatConversationId = normalizeHash(chatConversationId);
        senderWalletId = normalizeHash(senderWalletId);

        ChatConversation chatConversation = null;
        Wallet senderWallet = null;
        ChatConversationParticipant participant = null;

        try {
            chatConversation = crossStorageApi.find(defaultRepo, chatConversationId, ChatConversation.class);
        } catch (EntityDoesNotExistsException ex) {
            String errorMessage = "Failed to find Chat Conversation with hash = " + chatConversationId;
            result = "{\"status\": \"failed\", \"result\": \"" + errorMessage + "\"}";
            LOG.error(errorMessage, ex);
            return;
        }

        try {
            senderWallet = crossStorageApi.find(defaultRepo, senderWalletId, Wallet.class);
        } catch (EntityDoesNotExistsException ex) {
            String errorMessage = "Failed to find Sender Wallet with hash = " + senderWalletId;
            result = "{\"status\": \"failed\", \"result\": \"" + errorMessage + "\"}";
            LOG.error(errorMessage, ex);
            return;
        }

        try {
            participant = crossStorageApi.find(defaultRepo, ChatConversationParticipant.class)
                    .by("chatConversation", chatConversationId)
                    .by("participant", senderWalletId)
                    .getResult();

        } catch (Exception ex) {
            String errorMessage = "Failed to find Chat Conversation participant with hash = " + chatConversationId;
            LOG.error(errorMessage, ex);
        }

        try {
            if (participant == null) {
                participant = new ChatConversationParticipant();
                participant.setParticipant(senderWallet);
                participant.setChatConversation(chatConversation);

                crossStorageApi.createOrUpdate(defaultRepo, participant);
            }
          
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSender(senderWallet);
            chatMessage.setChatConversation(chatConversation);
            chatMessage.setMessage(this.message);
            chatMessage.setCreationDate(Instant.now());

            String uuid = crossStorageApi.createOrUpdate(defaultRepo, chatMessage);
            LOG.info("Chat Message created with Id: " + uuid);
            result = "{\"status\": \"success\", \"result\": \"" + uuid + "\"}";

        } catch (Exception ex) {
            String errorMessage = "Failed to create Chat Message with chatConversationId = " + chatConversationId;
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

    public void setSenderWalletId(String senderWalletId) {
        this.senderWalletId = senderWalletId;
    }
  
    public void setMessage(String message) {
        this.message = message;
    }

    public String getResult() {
        return this.result; 
    }
}