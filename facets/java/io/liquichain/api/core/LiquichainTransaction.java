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
import org.meveo.model.customEntities.Transaction;
import org.meveo.model.storage.Repository;
import org.meveo.service.storage.RepositoryService;
import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.api.persistence.CrossStorageRequest;
import org.meveo.api.exception.EntityDoesNotExistsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.web3j.crypto.*;

import javax.servlet.http.HttpServletRequest;

public class LiquichainTransaction extends Script {

    private static final Logger log = LoggerFactory.getLogger(LiquichainTransaction.class);

    private CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private Repository defaultRepo = repositoryService.findDefaultRepository();
    private CrossStorageService crossStorageService = getCDIBean(CrossStorageService.class);

    private static enum BLOCKCHAIN_TYPE {
        DATABASE,
        BESU,
        FABRIC
    }

    private static final BLOCKCHAIN_TYPE BLOCKCHAIN_BACKEND = BLOCKCHAIN_TYPE.DATABASE;

    public void transferDB(String fromAddress,String toAddress,BigInteger amount) throws Exception {
        String result="ok";
        Wallet toWallet = crossStorageApi.find(defaultRepo,toAddress, Wallet.class);
        Wallet fromWallet = crossStorageApi.find(defaultRepo,fromAddress,Wallet.class);
        BigInteger originBalance = new BigInteger(fromWallet.getBalance());
        log.info("originWallet 0x{} old balance:{} amount:{}",fromAddress,fromWallet.getBalance(),amount);
        if(amount.compareTo(originBalance)<=0){
              log.info("persisted paypalOrder, result order:{}",result);
        } else {
            throw new  BusinessException ("Insufficient balance");
        }
        Transaction transac = new Transaction();
        //transac.setHexHash(orderId);
        transac.setFromHexHash(fromWallet.getUuid());
        transac.setToHexHash(toWallet.getUuid());
        
        //CrossStorageRequest<Transaction> lastTransactionReq = crossStorageApi.find(defaultRepo, Transaction.class);
        //lastTransactionReq.by("wallet", fromWallet.getUuid()).fetch("uuid").
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
            //result = createResponse("", null);
        } catch(Exception e){
            result = "error";//createErrorResponse(orderId, "406", "");
        }
        //return result;
    }

    private void transferBesu(String fromAddress,String toAddress,BigInteger amount) throws Exception {
        //return "";
    }

    private void transferFabric(String fromAddress,String toAddress,BigInteger amount)  throws Exception {
        //return "";
    }

    public void transfer(String fromAddress,String toAddress,BigInteger amount) throws Exception{
        
        switch (BLOCKCHAIN_BACKEND){
            case BESU:
                transferBesu(fromAddress,toAddress,amount);
                break;
            case FABRIC:
                transferFabric(fromAddress,toAddress,amount);
                break;
            default:
                transferDB(fromAddress,toAddress,amount);
                break;
        }
    }

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
    }

}
