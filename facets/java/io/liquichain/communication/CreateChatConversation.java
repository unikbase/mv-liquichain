package io.liquichain.communication;

import java.util.Map;
import java.util.List;
import java.time.Instant;

import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.exception.EntityDoesNotExistsException;

import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.storage.Repository;
import org.meveo.service.storage.RepositoryService;

import org.meveo.model.customEntities.Wallet;
import org.meveo.model.customEntities.ChatConversation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateChatConversation extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(CreateChatConversation.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();

    private String createrWalletId;
    private String title;

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

        try {
            ChatConversation chatConversation = new ChatConversation();
            chatConversation.setTitle(this.title);
            chatConversation.setCreationDate(Instant.now());

            String uuid = crossStorageApi.createOrUpdate(defaultRepo, chatConversation);

            LOG.info("Chat Conversation created with Id: " + uuid);
            result = "{\"status\": \"success\", \"result\": \"" + uuid + "\"}";

        } catch (Exception ex) {
            String errorMessage = "Failed to create Chat Conversation with createrWalletId = " + createrWalletId;
            result = "{\"status\": \"failed\", \"result\": \"" + errorMessage + "\"}";
            LOG.error(errorMessage, ex);
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

    public String getResult() {
        return this.result;
    }
}