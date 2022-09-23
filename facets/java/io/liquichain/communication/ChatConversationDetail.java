package io.liquichain.communication;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.LocalDateTime;

import org.meveo.model.customEntities.ChatConversation;
import org.meveo.model.customEntities.ChatConversationParticipant;
import org.meveo.model.customEntities.Wallet; 

import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;

import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.storage.Repository;
import org.meveo.service.storage.RepositoryService;
import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.exception.EntityDoesNotExistsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatConversationDetail extends Script {
  
    private static final Logger LOG = LoggerFactory.getLogger(GetChatConversations.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();
	
	@Override
	public void execute(Map<String, Object> parameters) throws BusinessException {
		super.execute(parameters);      
	}
  
    public List<UiChatConversation> getOutputChatConversationList(List<ChatConversation> chatConversations){
        List<UiChatConversation> conversations = new ArrayList<>();
        chatConversations.forEach(cc-> conversations.add(getOutputChatConversation(cc)));
        return conversations;
    }
  
    public static ChatConversationDetail getInstance(){
      return new ChatConversationDetail();
    }
  
    public UiChatConversation getOutputChatConversation(ChatConversation cc){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<ChatConversationParticipant> conversationParticipantsList = crossStorageApi.find(defaultRepo, ChatConversationParticipant.class)
                    		.by("chatConversation", cc)
                    		.getResults();     
        
        UiChatConversation uiChatConversation = new UiChatConversation(cc.getUuid(), cc.getMessageCount(), cc.getConversationGroupId(), conversationParticipantsList,
                                                         LocalDateTime.ofInstant(cc.getCreationDate(), ZoneId.systemDefault()).format(formatter), cc.getTitle());
        return uiChatConversation;
    }
  
      
    class UiChatConversation {
    	
    	public UiChatConversation(String uuid, Long messageCount, String conversationGroupId, List<ChatConversationParticipant> participants, String creationDate,  String title) {
        	this.uuid = uuid;
        	this.messageCount = messageCount;
        	this.conversationGroupId = conversationGroupId;
        	this.creationDate = creationDate;
        	this.title = title;
            this.populateParticipants(participants);            
    	}

    	private String uuid;
    	private Long messageCount;
    	private String conversationGroupId;
    	private String creationDate;
    	private String title;
        private List<UiChatConversationParticipant> participants;
      
        public List<UiChatConversationParticipant> getParticipants(){
          return this.participants;
        }
      
        public void setParticipants(List<UiChatConversationParticipant> participants){
          this.participants = participants;
        }
      
        public void populateParticipants(List<ChatConversationParticipant> participants){
            if(participants!= null){
                List<UiChatConversationParticipant> uiChatConversationParticipants = new ArrayList<>();
                participants.forEach(p-> uiChatConversationParticipants.add(new UiChatConversationParticipant(p.getUuid(), p.getParticipant())) );
                this.participants = uiChatConversationParticipants;
            }
        }
      
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
      
        class UiChatConversationParticipant{
            private String uuid;
            private Wallet participant;    
          
			public  UiChatConversationParticipant(String uuid, Wallet participant){
                this.uuid = uuid;
                this.participant = participant;
            }
          
            public String getUuId(){
              	return this.uuid;
            }
          
            public void setUuId(String uuid){
              	this.uuid = uuid;
            }
          
            public Wallet getParticipant(){
              	return this.participant;
            }
          
            public void setUuId(Wallet participant){
              	this.participant = participant;
            }
            
        }
    }
	
}