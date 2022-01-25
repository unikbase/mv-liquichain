package io.liquichain.api.core;

import com.google.gson.Gson;
import io.liquichain.core.BlockForgerScript;

import java.util.*;
import java.time.Instant;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.io.IOException;

import org.meveo.service.script.Script;
import org.apache.commons.collections.CollectionUtils;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.persistence.CrossStorageService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.NativeCustomEntityInstanceService;
import org.meveo.admin.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigInteger;
import org.meveo.model.customEntities.Wallet;
import org.meveo.model.customEntities.PaypalOrder;
import org.meveo.model.customEntities.Transaction;
import org.meveo.model.storage.Repository;
import org.meveo.service.storage.RepositoryService;
import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.api.exception.EntityDoesNotExistsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.web3j.crypto.*;

import javax.servlet.http.HttpServletRequest;

public class LiquichainTransaction extends Script {

    private static final Logger log = LoggerFactory.getLogger(LiquichainTransaction.class);

    private long chainId = 76;

    private String result;

    private String orderId = null;

    private String originWallet = "82328dd96f7b7d526fe8ac29c9c116376a733612".toLowerCase();

    private CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private Repository defaultRepo = repositoryService.findDefaultRepository();
    private CustomEntityTemplateService customEntityTemplateService = getCDIBean(CustomEntityTemplateService.class);
    private CrossStorageService crossStorageService = getCDIBean(CrossStorageService.class);

    public String getResult() {
        return result;
    }

    public void setOrderId(String orderId) {
      	log.info("orderId setter {}", orderId);
        this.orderId = orderId;
    }
  
  	public String getOrderId() {
    	return orderId;
    }

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
      	log.info("orderId from path={}", getOrderId());
        String message = "Wallet does not exists";
        Map<String, Object> from = (Map<String, Object>) parameters.get("from");
        Map<String, Object> to = (Map<String, Object>) parameters.get("to");
        String publicAddress = (String) parameters.get("account");
        String returnUrl = "https://account.liquichain.io/";
		log.info("orderId from setter :{}",orderId);
        String path = "";
        if(path.lastIndexOf("/")==16){
        	orderId = path.substring(17);
        }
        log.info("execute paymentScript, orderId={}  account=0x{}, path={}, lastIndex:{}",orderId,publicAddress,path,path.lastIndexOf("/"));
       

        if (orderId == null || orderId.equals("0")) {
            if (StringUtils.isNotBlank(publicAddress)) {
                try {
                    Wallet toWallet = crossStorageApi.find(defaultRepo,publicAddress.toLowerCase(), Wallet.class);
                    Wallet fromWallet = crossStorageApi.find(defaultRepo, originWallet,Wallet.class);
                    BigInteger amount = new BigDecimal(to.get("amount").toString()).movePointRight(18).toBigInteger();
				    BigInteger originBalance = new BigInteger(fromWallet.getBalance());
        			log.info("originWallet 0x{} old balance:{} amount:{}",originWallet,fromWallet.getBalance(),amount);
        			if(amount.compareTo(originBalance)<=0){
                      	log.info("persisted paypalOrder, result order:{}",result);
        			} else {
                      	log.error("Insufficient global balance");
                        result = createErrorResponse(null, "501", "Insufficient global balance");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    result = createErrorResponse(null, "-32700", e.getMessage());
                }
            } else {
                log.error("account publicAddress:{}",publicAddress);
                result = createErrorResponse(null, "404", message);
            }
        } else {
                Transaction transac = new Transaction();
        		transac.setHexHash(orderId);
        		transac.setFromHexHash(originWallet);
        		transac.setToHexHash("");
        		
        		//FIXME: increment the nonce
        		transac.setNonce("1");
      
        		transac.setGasPrice("0");
        		transac.setGasLimit("0");
        		transac.setValue("");
              
        		//FIXME: sign the transaction
        		transac.setSignedHash(UUID.randomUUID().toString());
              
        		transac.setCreationDate(java.time.Instant.now());
                try {
        			crossStorageApi.createOrUpdate(defaultRepo, transac);

                	//FIXME: you should get the BlockForgerScript from scriptService
        			BlockForgerScript.addTransaction(transac);
                    result = "Success";
                    result = createResponse("", null);
                } catch(Exception e){
                	result = createErrorResponse(orderId, "406", "");
                }
        }

    }

    private String createResponse(String requestId,Object order) {
        String res = "{\n";
        res += "  \"id\": \"" + requestId + "\",\n";
        res += "  \"status\": \"" + 200 + "\",\n";
       // res += "  \"jsonrpc\": \"2.0\",\n";
        if (order != null) {
            try {
                res += "  \"result\": " + new ObjectMapper().writeValueAsString(order) + "\n";
            } catch (JsonProcessingException jpe) {
                // used error code from https://github.com/claudijo/json-rpc-error
                return createErrorResponse(requestId, "-32700", jpe.getMessage());
            }
        } else {
            res += "  \"message\": \"" + result + "\"\n";
        }
        res += "}";
        // log.info("res:{}", res);
        return res;

    }

    private String createErrorResponse(String requestId, String errorCode, String message) {
        String res = "{\n";
        if (requestId != null) {
            res += "  \"id\": \""  + requestId + "\",\n";
        }
        res += "  \"status\": \"" + errorCode + "\",\n";
        res += "  \"message\": \"" + message + "\"\n";
        res += "}";
        log.info("err:{}", res);
        return res;
    }

}
