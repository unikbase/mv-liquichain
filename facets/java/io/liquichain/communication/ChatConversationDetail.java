package io.liquichain.communication;

import java.util.Set;
import java.util.HashSet;
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
    private Set<String> allWalletIds = new HashSet<>();

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);
    }

    public UiChatConversation getChatConversationById(ChatConversation chatConversation) {
      	Map<String, Wallet> walletMap = new HashMap<>();  
        this.allWalletIds = new HashSet<String>();
      	UiChatConversation conversation = this.getOutputChatConversation(chatConversation);
      
      	List<Wallet> walletList = crossStorageApi.find(defaultRepo, Wallet.class)
                .by("inList uuid", new ArrayList<String>(this.allWalletIds))
                .getResults();
		walletList.stream().forEach(w-> walletMap.put(w.getUuid(), w));
      	
      	if(conversation.getParticipants() != null && !conversation.getParticipants().isEmpty()){
        	conversation.getParticipants().forEach(cp->cp.setName(walletMap.get(cp.getWallet()).getName() ));
        }        
        
        return conversation;
    }

    public List<UiChatConversation> getOutputChatConversationList(List<ChatConversation> chatConversations) {
        List<UiChatConversation> conversations = new ArrayList<>();
      	this.allWalletIds = new HashSet<String>();
        Map<String, Wallet> walletMap = new HashMap<>();  
      
        chatConversations.forEach(cc -> { 
          	UiChatConversation conversation = getOutputChatConversation(cc);          	
          	conversations.add(conversation);
        });
      
      	List<Wallet> walletList = crossStorageApi.find(defaultRepo, Wallet.class)
                .by("inList uuid", new ArrayList<String>(this.allWalletIds))
                .getResults();
     	 
      	walletList.stream().forEach(w-> walletMap.put(w.getUuid(), w));      
      
      	conversations.forEach(c->c.getParticipants().forEach(cp->cp.setName(walletMap.get(cp.getWallet()).getName() ) ));      	
      
        return conversations;
    }

   

    private UiChatConversation getOutputChatConversation(ChatConversation cc) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<ChatConversationParticipant> conversationParticipantsList = crossStorageApi.find(defaultRepo, ChatConversationParticipant.class)
                .by("chatConversation", cc)
                .getResults();

        UiChatConversation uiChatConversation = new UiChatConversation(cc.getUuid(), cc.getMessageCount(), cc.getConversationGroupId(), conversationParticipantsList,
                LocalDateTime.ofInstant(cc.getCreationDate(), ZoneId.systemDefault()).format(formatter), cc.getTitle());

        // fillup list to later call out one query and populate nested objects later to reduce overhead
      	this.allWalletIds.addAll(conversationParticipantsList.stream().map(cp -> cp.getParticipant().getUuid()).collect(Collectors.toSet()));
      	conversationParticipantsList.stream().forEach(cp -> { LOG.info(cp.getParticipant().getUuid()); } );
      
        return uiChatConversation;
    }

}

class UiChatConversation {
    private String uuid;
    private Long messageCount;
    private String conversationGroupId;
    private String creationDate;
    private String title;
    private List<UiChatConversationParticipant> participants;

    public UiChatConversation(String uuid, Long messageCount, String conversationGroupId, List<ChatConversationParticipant> chatParticipants, String creationDate, String title) {
        this.uuid = uuid;
        this.messageCount = messageCount;
        this.conversationGroupId = conversationGroupId;
        this.creationDate = creationDate;
        this.title = title;
        this.participants = chatParticipants.stream().map(cp -> new UiChatConversationParticipant(cp.getUuid(), cp.getParticipant().getUuid())).collect(Collectors.toList());
    }

    public List<UiChatConversationParticipant> getParticipants() {
        return this.participants;
    }

    public void setParticipants(List<UiChatConversationParticipant> participants) {
        this.participants = participants;
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
}

class UiChatConversationParticipant {
    private String participantUuid;
    private String wallet;
    private String name;    

    public UiChatConversationParticipant(String participantUuid, String walletId) {
        this.participantUuid = participantUuid;
        this.wallet = walletId;
    }

    public String getParticipantUuid() {
        return this.participantUuid;
    }

    public void setParticipantUuid(String participantUuid) {
        this.participantUuid = participantUuid;
    }

    public String getWallet() {
        return this.wallet;
    }

    public void setWallet(String wallet) {
        this.wallet = wallet;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }    
}   
