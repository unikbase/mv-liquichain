package io.liquichain.api.core;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.customEntities.Transaction;
import org.meveo.model.customEntities.Wallet;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;
import org.meveo.service.script.Script;
import org.meveo.service.storage.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;

import io.liquichain.core.BlockForgerScript;

public class LiquichainTransaction extends Script {

    private static final long LIQUICHAIN_CHAINID = 76l; 
    
    private static final Logger log = LoggerFactory.getLogger(LiquichainTransaction.class);

    private CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private Repository defaultRepo = repositoryService.findDefaultRepository();

    private static enum BLOCKCHAIN_TYPE {
        DATABASE,
        BESU,
        FABRIC
    }

    private static final BLOCKCHAIN_TYPE BLOCKCHAIN_BACKEND = BLOCKCHAIN_TYPE.DATABASE;


    private String fromAddress;
    private String toAddress;
    private String value;
    private String result;
    
    public void setFromAddress(String fromAddress){
        this.fromAddress=fromAddress;
    }

    public void setToAddress(String toAddress){
        this.toAddress=toAddress;
    }

    public void setValue(String value){
        this.value=value;
    }

    public String getResult(){
        return result;
    }

    public String transferDB(String fromAddress,String toAddress,BigInteger value) throws Exception {
        String transactionHash="";
        Wallet toWallet = crossStorageApi.find(defaultRepo,toAddress, Wallet.class);
        Wallet fromWallet = crossStorageApi.find(defaultRepo,fromAddress,Wallet.class);
        if(fromWallet.getPrivateKey()==null){
            throw new Exception("wallet has no private key");
        }
        String privateKey = fromWallet.getPrivateKey();
        if(privateKey.startsWith("0x")){
            privateKey = privateKey.substring(2);
        }
        BigInteger originBalance = new BigInteger(fromWallet.getBalance());
        log.info("originWallet 0x{} old balance:{} amount:{}",fromAddress,fromWallet.getBalance(),value);
        if(value.compareTo(originBalance)>0){
            throw new  BusinessException ("Insufficient balance");
        }
        
        List<Transaction> walletTransactions = crossStorageApi.find(defaultRepo, Transaction.class).by("fromHexHash", fromWallet.getUuid()).getResults();
        BigInteger nonce= BigInteger.ONE;
        if(walletTransactions!=null && walletTransactions.size()>0){
            walletTransactions.sort(Comparator.comparing(Transaction::getNonce).reversed());
            Transaction lastTransaction = walletTransactions.get(0);
            try{
                nonce=BigInteger.valueOf(Long.parseLong(lastTransaction.getNonce())+1);
            } catch(Exception e){
                log.error("invalid nonce :{}",lastTransaction.getNonce());
            }
        }
        String recipientAddress = "0x"+toWallet.getUuid();
        BigInteger gasLimit = BigInteger.ZERO;
        BigInteger gasPrice = BigInteger.ZERO;
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit,recipientAddress, value);
        Credentials credentials = Credentials.create(privateKey);
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        
        Transaction transac = new Transaction();
        transactionHash=Hash.sha3(hexValue);
        transac.setHexHash(transactionHash);
        transac.setFromHexHash(fromWallet.getUuid());
        transac.setToHexHash(toWallet.getUuid());
        transac.setNonce(""+nonce);
        transac.setGasPrice("0");
        transac.setGasLimit("0");
        transac.setValue(""+value);
      
        transac.setSignedHash(hexValue);
      
        transac.setCreationDate(java.time.Instant.now());
        
        crossStorageApi.createOrUpdate(defaultRepo, transac);
        //FIXME: you should get the BlockForgerScript from scriptService
        
        BlockForgerScript.addTransaction(transac);
        return transactionHash;
    }

    private String transferBesu(String fromAddress,String toAddress,BigInteger amount) throws Exception {
        return "";
    }

    private String transferFabric(String fromAddress,String toAddress,BigInteger amount)  throws Exception {
        return "";
    }

    public String transfer(String fromAddress,String toAddress,BigInteger amount) throws Exception{
        String transactionHash = "";
        switch (BLOCKCHAIN_BACKEND){
            case BESU:
                transactionHash = transferBesu(fromAddress,toAddress,amount);
                break;
            case FABRIC:
                transactionHash = transferFabric(fromAddress,toAddress,amount);
                break;
            default:
                transactionHash = transferDB(fromAddress,toAddress,amount);
                break;
        }
        return transactionHash;
    }

    //used to transfer from local account
    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        String transactionHash = "";
        try{
            transactionHash =  transfer(fromAddress,toAddress,new BigInteger(value));
            result = "{\"transaction_hash\":\""+transactionHash+"\"}";
        } catch (Exception e){
            log.error(" transafer error", e);
            result = "{\"error\":\""+e.getMessage()+"\"}";
        }
    }

}
