package io.liquichain.api.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.customEntities.BlockedUser;
import org.meveo.model.customEntities.VerifiedPhoneNumber;
import org.meveo.model.customEntities.Wallet;
import org.meveo.model.storage.Repository;
import org.meveo.service.script.Script;
import org.meveo.service.storage.RepositoryService;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GetBlockUser extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(GetBlockUser.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();
    private final Gson gson = new Gson();

    private String walletId;

    private String result;

    public String getResult() {
        return result;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    private String returnError(String errorCode, String message) {
        String res = "{\n";
        res += "  \"error\": { \"code\" : \"" + errorCode + "\" , \"message\" : \"" + message + "\"}\n";
        res += "}";
        return res;
    }

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);

        LOG.debug("get blocked users of - walletId = {}", walletId);
        walletId = (walletId.startsWith("0x") ? walletId.substring(2) : walletId).toLowerCase();
        Wallet user = crossStorageApi.find(defaultRepo, Wallet.class).by("uuid", walletId).getResult();
        if (user == null) {
            result = returnError("USER_NOT_FOUND", "user not found against provided walletId.");
            return;
        }

        List<BlockedUser> blockedUsers =
            crossStorageApi.find(defaultRepo, BlockedUser.class).by("wallet", user).getResults();
        if (blockedUsers == null || blockedUsers.isEmpty()) {
            result = "{ \"status\" : \"success\" , \"message\": \"user's block list is empty.\"}";
            return;
        }

        JsonArray responseObj = new JsonArray();
        List<String> targetWalletIds =
            blockedUsers.stream().map(b -> b.getTargetWallet().getUuid()).collect(Collectors.toList());
        LOG.debug("total target walletIds = {}", targetWalletIds.size());
        targetWalletIds.forEach(u -> LOG.debug("walletId = {}", u));
        //== loading the target wallet data
        Map<String, Wallet> walletMap = new HashMap<>();
        List<Wallet> walletList = crossStorageApi.find(defaultRepo, Wallet.class)
                                                 .by("inList uuid", new ArrayList<String>(targetWalletIds))
                                                 .getResults();
        walletList.stream().forEach(w -> walletMap.put(w.getUuid(), w));
        //== preparing the response
        for (BlockedUser blockedUser : blockedUsers) {
            JsonObject userObj = gson.toJsonTree(blockedUser).getAsJsonObject();
            userObj.remove("wallet");
            String targetWalletId = userObj.get("targetWallet").getAsJsonObject().get("uuid").getAsString();
            Wallet targetWallet = (Wallet) walletMap.get(targetWalletId);

            userObj.addProperty("name", targetWallet.getName());
            //== loading blocked user's phone number
            if (targetWallet.getPhoneNumber() != null) {
                VerifiedPhoneNumber phoneNumber = crossStorageApi.find(defaultRepo, VerifiedPhoneNumber.class)
                                                                 .by("uuid", targetWallet.getPhoneNumber())
                                                                 .getResult();
                if (phoneNumber != null)
                    userObj.addProperty("phoneNumber", phoneNumber.getPhoneNumber());
            }
            //== loading blocked user's avatar info from publicInfo
            if (StringUtils.isNotBlank(targetWallet.getPublicInfo())) {
                JsonObject publicInfoObj =
                    ((JsonElement) gson.fromJson(targetWallet.getPublicInfo(), JsonElement.class)).getAsJsonObject();
                if (publicInfoObj != null) {
                    if (publicInfoObj.get("avatar") != null && publicInfoObj.get("avatar").getAsInt() != 0) {
                        userObj.addProperty("avatar", publicInfoObj.get("avatar").getAsInt());
                    }
                    if (publicInfoObj.get("base64Avatar") != null && !publicInfoObj.get("base64Avatar").isJsonNull()
                        && StringUtils.isNotBlank(publicInfoObj.get("base64Avatar").getAsString())) {
                        userObj.addProperty("base64Avatar", publicInfoObj.get("base64Avatar").getAsString());
                    }
                }
            }

            responseObj.add(userObj);
        }

        result = "{\"status\" : \"success\", \"result\" : " + responseObj.toString() + "}";
    }

}
