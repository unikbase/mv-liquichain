package io.liquichain.communication;

import java.util.Map;

import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;

import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.storage.Repository;
import org.meveo.service.storage.RepositoryService;

import org.meveo.model.customEntities.ChatConversationParticipant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaveChatConversation extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(LeaveChatConversation.class);
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

        ChatConversationParticipant participant = null;

        try {
            participant = crossStorageApi.find(defaultRepo, ChatConversationParticipant.class)
                    .by("participant", participantWalletId)
                    .by("chatConversation", chatConversationId)
                    .getResult();
        } catch (Exception ex) {
            String errorMessage = "Failed to find Chat Conversation Participant with chatConversationId = " + chatConversationId + " participantWalletId = " + participantWalletId;
            LOG.error(errorMessage, ex);
            result = "{\"status\": \"failed\", \"result\": \"" + errorMessage + "\"}";
            return;
        }

      	if(participant != null) {
          	try {
            	crossStorageApi.remove(defaultRepo, participant.getUuid(), ChatConversationParticipant.class);
            	String successMessage = "Chat Conversation Participant delete with uuId: " + participant.getUuid();
            	LOG.info(successMessage);
           		result = "{\"status\": \"success\", \"result\": \"" + successMessage + "\"}";
            } catch (Exception ex) {
            	String errorMessage = "Failed to delete Chat Conversation Participant with chatConversationId = " + chatConversationId + " participantWalletId = " + participantWalletId;
            	LOG.error(errorMessage, ex);
            	result = "{\"status\": \"failed\", \"result\": \"" + errorMessage + "\"}";
            }
        } else {
        	String errorMessage = "Failed to find Chat Conversation Participant with chatConversationId = " + chatConversationId + " participantWalletId = " + participantWalletId;
            LOG.info(errorMessage);
            result = "{\"status\": \"failed\", \"result\": \"" + errorMessage + "\"}";
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