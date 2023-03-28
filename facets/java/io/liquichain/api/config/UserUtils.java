package io.liquichain.api.config;

import java.util.Map;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.customEntities.BlockedUser;
import org.meveo.model.customEntities.UserConfiguration;
import org.meveo.model.customEntities.Wallet;
import org.meveo.model.storage.Repository;
import org.meveo.security.MeveoUser;
import org.meveo.service.crm.impl.CurrentUserProducer;
import org.meveo.service.script.Script;
import org.meveo.service.storage.RepositoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserUtils extends Script {
    private static final Logger log = LoggerFactory.getLogger(UserUtils.class);

    private final CrossStorageApi crossStorageApi;
    private final Repository defaultRepo;

    private Wallet userWallet;
    private UserConfiguration userConfig;

    public UserUtils() {
        super();
        crossStorageApi = getCDIBean(CrossStorageApi.class);
        RepositoryService repositoryService = getCDIBean(RepositoryService.class);
        defaultRepo = repositoryService.findDefaultRepository();
    }


    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);
    }

    public boolean isUserEmailNotificationsAllowed() {
        return loadUserConfig().getIsEmailNotificationsEnabled();
    }

    public boolean isUserOrderUpdatesAllowed() {
        return loadUserConfig().getIsOrderUpdatesEnabled();
    }

    public boolean isUserSellerInfoUpdatesAllowed() {
        return loadUserConfig().getIsSellerInfoUpdatesEnabled();
    }

    public boolean isUserChatNotificationsAllowed() {
        return loadUserConfig().getIsChatNotificationsEnabled();
    }

    public boolean isUserChatFromProfilePageAllowed() {
        return loadUserConfig().getIsChatEnabledFromProfilePage();
    }

    public boolean isUserAutoReplyMessageAllowed() {
        return loadUserConfig().getIsAutoReplyEnabled();
    }

    public String getUserAutoReplyMessage() {
        return loadUserConfig().getAutoReplyMessage();
    }

    public boolean isUserBlocked(String walletId, String targetWalletId) {

        log.debug("checking if targetWalletId: {} is blocked by wallet: {}", targetWalletId, walletId);
        walletId = (walletId.startsWith("0x") ? walletId.substring(2) : walletId).toLowerCase();
        targetWalletId = (targetWalletId.startsWith("0x") ? targetWalletId.substring(2) : targetWalletId).toLowerCase();

        BlockedUser blockedUser = crossStorageApi.find(defaultRepo, BlockedUser.class)
                                                 .by("targetWallet", targetWalletId)
                                                 .by("wallet", walletId).getResult();

        return blockedUser != null;
    }

    public UserConfiguration loadUserConfigByWalletId(String walletId) {
        UserConfiguration config;
        try {
            Wallet wallet = crossStorageApi.find(defaultRepo, walletId, Wallet.class);
            if (wallet == null) {
                throw new RuntimeException("Wallet with id: " + walletId + ", does not exist.");
            }
            config = crossStorageApi.find(defaultRepo, UserConfiguration.class)
                                    .by("user", walletId)
                                    .getResult();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve the user's configurations for wallet id :"
                + walletId + " errorMessage: " + e.getMessage());
        }
        return loadDefaultsIfNull(config);
    }

    private UserConfiguration loadUserConfig() {
        if (userConfig == null) {
            try {
                CurrentUserProducer userProducer = getCDIBean(CurrentUserProducer.class);
                MeveoUser user = userProducer.getCurrentUser();
                String username = user.getUserName();
                userWallet = crossStorageApi.find(defaultRepo, Wallet.class)
                                            .by("likeCriterias publicInfo privateInfo", "*" + username + "*")
                                            .getResult();
                userConfig = crossStorageApi.find(defaultRepo, UserConfiguration.class)
                                            .by("user", userWallet.getUuid())
                                            .getResult();
            } catch (Exception e) {
                throw new RuntimeException("Failed to retrieve the user's configurations for wallet id :"
                    + userWallet.getUuid() + " errorMessage: " + e.getMessage());
            }
        }
        userConfig = loadDefaultsIfNull(userConfig);
        return userConfig;
    }

    private UserConfiguration loadDefaultsIfNull(UserConfiguration configuration) {
        if (configuration == null) {
            configuration = new UserConfiguration();
            configuration.setIsEmailNotificationsEnabled(true);
            configuration.setIsOrderUpdatesEnabled(true);
            configuration.setIsSellerInfoUpdatesEnabled(true);
            configuration.setIsChatNotificationsEnabled(true);
            configuration.setIsChatEnabledFromProfilePage(true);
            configuration.setIsAutoReplyEnabled(true);
            configuration.setAutoReplyMessage("");
            configuration.setUser(userWallet);
        }
        return configuration;
    }
}
