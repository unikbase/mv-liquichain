package io.liquichain.api.config;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import com.google.gson.Gson;
import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.customEntities.Wallet;
import org.meveo.model.customEntities.BlockedUser;

import org.meveo.commons.utils.StringUtils;

import org.meveo.model.storage.Repository;
import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.meveo.service.storage.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IsUserBlockedByUser extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(IsUserBlockedByUser.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();

    private String walletId;
    private List<Map<String, Object>> blockers;
    private final Map<String, Object> result = new HashMap<>();


    public Map<String, Object> getResult() {
        return result;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public void setBlockers(List<Map<String, Object>> blockers) {
        this.blockers = blockers;
    }

    private void mapError(String errorCode, String message) {
        LOG.error(message);
        Map<String, String> errorMap = new HashMap<>() {{
            put("code", errorCode);
            put("message", message);
        }};
        result.put("error", errorMap);
    }


    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);

        LOG.info("verify blockers of user - walletId = {}", walletId);
        if (StringUtils.isBlank(walletId)) {
            mapError("WALLET_ID_NOT_FOUND", "walletId not found.");
            return;
        }

        LOG.info("Checking if wallets: {}, have blocked the user", new Gson().toJson(blockers));
        if (blockers == null || blockers.isEmpty()) {
            mapError("BLOCKER_WALLET_IDS_NOT_FOUND", "blocker walletIds not found.");
            return;
        }
        //== mapping blocked users data
        mapBlockersData();
    }

    private String normalize(String data) {
        return (data.startsWith("0x") ? data.substring(2) : data).toLowerCase();
    }

    private void mapBlockersData() {
        try {
            walletId = normalize(walletId);
            Wallet wallet = crossStorageApi.find(defaultRepo, Wallet.class).by("uuid", walletId).getResult();
            if (wallet == null) {
                mapError("USER_NOT_FOUND", "user not found against provided walletId.");
                return;
            }

            List<String> blockerWalletIds = new ArrayList<>();
            for (Map<String, Object> blocker : blockers) {
                String blockerId = "" + blocker.get("walletId");
                if (StringUtils.isNotBlank(blockerId)) {
                    blockerId = normalize(blockerId);
                    // replace wallet id with normalized wallet id
                    blocker.put("walletId", blockerId);
                    blockerWalletIds.add(blockerId);
                }
            }

            List<BlockedUser> blockedUsers = crossStorageApi.find(defaultRepo, BlockedUser.class)
                                                            .by("targetWallet", wallet)
                                                            .by("inList wallet", blockerWalletIds)
                                                            .getResults();

            //== preparing the response
            for (Map<String, Object> blocker : blockers) {
                String blockerId = "" + blocker.get("walletId");
                // set blocked field whether true or false
                blocker.put("blocked", blockedUsers
                    .stream()
                    .anyMatch(blockedUser -> blockerId.equals(blockedUser.getWallet().getUuid()))
                );
            }

            result.put("status", "success");
            result.put("result", blockers);
        } catch (Exception e) {
            String errorMessage = "Error when creating the state [" + e.getMessage() + "]";
            LOG.error(errorMessage, e);
            mapError("STATE_CREATION_ERROR", errorMessage);
        }
    }
}
