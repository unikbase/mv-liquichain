package io.liquichain.communication;

import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.time.Instant;
import java.util.stream.Collectors;  

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

public class CreateChatConversation extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(CreateChatConversation.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();

    private String createrWalletId;
    private String title;
  	private Long groupId;

    private String result;

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);

        createrWalletId = normalizeHash(createrWalletId);
        Wallet createrWallet = null;
      
        try {
            createrWallet = crossStorageApi.find(defaultRepo, createrWalletId, Wallet.class);
        } catch (EntityDoesNotExistsException ex) {
            String errorMessage = "Failed to find creater wallet with hash = " + createrWalletId;
            result = "{\"status\": \"failed\", \"result\": \"" + errorMessage + "\"}";
            LOG.error(errorMessage, ex);
            return;
        }
      
        List<ChatConversationParticipant> participants = crossStorageApi.find(defaultRepo, ChatConversationParticipant.class)
                    .by("participant", createrWallet)                    
                    .getResults();
        Optional<ChatConversation> existingChatConversation = Optional.ofNullable(null);  
        if(participants.size() > 0){
        	List<String> chatConversationUUIds = participants.stream().map(participant -> participant.getChatConversation().getUuid() ).collect(Collectors.toList());
        	if(chatConversationUUIds != null && chatConversationUUIds.size() > 0 ){
                List<ChatConversation> chatConversations = crossStorageApi.find(defaultRepo, ChatConversation.class)
                    	.by("inList uuid", chatConversationUUIds)
                    	.getResults(); 
          		existingChatConversation = chatConversations.stream().filter(c -> (groupId == null && title.equals(c.getTitle()) ) || groupId.equals(c.getGroupId())  ).findFirst();
        	}
        }
      
        
        if(existingChatConversation.isPresent()){
            result = "{\"status\": \"success\", \"result\": \"" + existingChatConversation.get().getUuid() + "\"}";
            LOG.info("Chat Conversation exixts already with Id: " + existingChatConversation.get().getUuid() );
        }else{
        	
        	try {
            	ChatConversation chatConversation = new ChatConversation();
            	chatConversation.setTitle(this.title);
            	chatConversation.setCreationDate(Instant.now());
              	chatConversation.setGroupId(this.groupId);

            	String uuid = crossStorageApi.createOrUpdate(defaultRepo, chatConversation);

            	LOG.info("Chat Conversation created with Id: " + uuid);
            	result = "{\"status\": \"success\", \"result\": \"" + uuid + "\"}";

        	} catch (Exception ex) {
            	String errorMessage = "Failed to create Chat Conversation with createrWalletId = " + createrWalletId;
            	result = "{\"status\": \"failed\", \"result\": \"" + errorMessage + "\"}";
            	LOG.error(errorMessage, ex);
        	}    
        }
    }

    private String normalizeHash(String hash) {
        return hash.startsWith("0x") ? hash.substring(2).toLowerCase() : hash.toLowerCase();
    }

    public void setCreaterWalletId(String createrWalletId) {
        this.createrWalletId = createrWalletId;
    }

    public void setTitle(String title) {
        this.title = title;
    }
  
  	public void setGroupId(Long groupId){
    	this.groupId = groupId;
    }

    public String getResult() {
        return this.result;
    }
}