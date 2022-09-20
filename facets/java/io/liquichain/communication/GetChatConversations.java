package io.liquichain.communication;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.storage.Repository;
import org.meveo.service.storage.RepositoryService;
import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.exception.EntityDoesNotExistsException;

import org.meveo.model.customEntities.ChatConversation;
import org.meveo.model.customEntities.ChatConversationParticipant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetChatConversations extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(GetChatConversations.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();

    private String walletId;
    private String result;

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);

        walletId = normalizeHash(walletId);
        List<ChatConversation> chatConversations = null;

        try {
            List<ChatConversationParticipant> participants = crossStorageApi.find(defaultRepo, ChatConversationParticipant.class)
                    .by("participant", walletId)
                    .select("chatConversation")
                    .getResults();

            List<String> chatConversationIds = participants.stream().map(participant -> {
                return participant.getChatConversation().getUuid();
            }).collect(Collectors.toList());
			
          	if(chatConversationIds != null && !chatConversationIds.isEmpty()){
            	chatConversations = crossStorageApi.find(defaultRepo, ChatConversation.class)
                    	.by("inList uuid", chatConversationIds)
                    	.getResults();
            }
        } catch (Exception ex) {
            String errorMessage = "Failed to find ChatConversationParticipant with hash = " + walletId;
            LOG.error(errorMessage, ex);
        }

      	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (chatConversations != null && !chatConversations.isEmpty()) {
            List<UiChatConversation> conversations = new ArrayList<>();
          	chatConversations.forEach(cc-> {
                conversations.add(new UiChatConversation(cc.getUuid(), cc.getMessageCount(), cc.getConversationGroupId(), 
                                                         LocalDateTime.ofInstant(cc.getCreationDate(), ZoneId.systemDefault()).format(formatter), cc.getTitle()));
            });
          
            Gson gson = new GsonBuilder()
    					.serializeNulls()
               			.setPrettyPrinting()
    					.create();
            this.result = new Gson().toJson(chatConversations); //gson.toJson(chatConversations);
        } else {
            this.result = "[]";
        }
    }
  

    private String normalizeHash(String hash) {
        return hash.startsWith("0x") ? hash.substring(2).toLowerCase() : hash.toLowerCase();
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getResult() {
        return this.result;
    }
  
      
    class UiChatConversation {
    	
    	public UiChatConversation(String uuid, Long messageCount, String conversationGroupId, String creationDate, String title) {
        	this.uuid = uuid;
        	this.messageCount = messageCount;
        	this.conversationGroupId = conversationGroupId;
        	this.creationDate = creationDate;
        	this.title = title;
    	}

    	private String uuid;
    	private Long messageCount;
    	private String conversationGroupId;
    	private String creationDate;
    	private String title;
      
   		public String getUuid() {
        	return uuid;
    	}

    	public void setUuid(String uuid) {
        	this.uuid = uuid;
    	}

    	public Long getMessageCount() {
        	return messageCount;
    	}

    	public void setMessageCount(Long messageCount) {
        	this.messageCount = messageCount;
    	}

   		public String getConversationGroupId() {
        	return conversationGroupId;
    	}

    	public void setConversationGroupId(String conversationGroupId) {
        	this.conversationGroupId = conversationGroupId;
    	}

    	public String getCreationDate() {
        	return creationDate;
   		}

    	public void setCreationDate(String creationDate) {
        	this.creationDate = creationDate;
    	}

    	public String getTitle() {
        	return title;
    	}

    	public void setTitle(String title) {
        	this.title = title;
    	}     
    }
}