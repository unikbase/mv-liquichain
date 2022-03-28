package io.liquichain.api.core;

import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Collections;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.model.customEntities.Transaction;
import org.meveo.model.customEntities.Wallet;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;
import org.meveo.service.script.Script;
import org.meveo.service.storage.RepositoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.ClientConnectionException;
import org.web3j.protocol.Service;
import org.web3j.tx.RawTransactionManager;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;
import org.web3j.utils.Numeric;

import io.liquichain.core.BlockForgerScript;

public class LiquichainTransaction extends Script {
    private static final Logger log = LoggerFactory.getLogger(LiquichainTransaction.class);
    private static final long LIQUICHAIN_CHAINID = 76l;
    private static final int SLEEP_DURATION = 15000;
    private static final int ATTEMPTS = 40;
    private static final String INSUFFICIENT_BALANCE = "Insufficient balance";
    private static final String TRANSACTION_FAILED = "Transaction failed";

    private CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private Repository defaultRepo = repositoryService.findDefaultRepository();
    private ParamBeanFactory paramBeanFactory = getCDIBean(ParamBeanFactory.class);
    private ParamBean config = paramBeanFactory.getInstance();
    private String besuApiUrl = config
            .getProperty("besu.api.url", "https://testnet.liquichain.io/rpc");
    private BigInteger defaultGasLimit =
            new BigInteger(config.getProperty("besu.gas.limit", "21000"));
    private BigInteger defaultGasPrice = new BigInteger(config.getProperty("besu.gas.price", "0"));
    private String smartContract = config
            .getProperty("besu.smart.contract", "0x0Cd07348D582a6F4A3641D3192f1f467586BE990");

    private Web3j web3j = Web3j.build(new HttpService(besuApiUrl));

    private enum BLOCKCHAIN_TYPE {
        DATABASE, BESU, FABRIC, SMART_CONTRACT, BESU_DB
    }

    private String blockchainType = config.getProperty("txn.blockchain.type", "BESU");
    private BLOCKCHAIN_TYPE BLOCKCHAIN_BACKEND = BLOCKCHAIN_TYPE.valueOf(blockchainType);

    private String fromAddress;
    private String toAddress;
    private String value;
    private String result;

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getResult() {
        return result;
    }

    private String normalizeHash(String hash) {
        if (hash.startsWith("0x")) {
            return hash.substring(2);
        }
        return hash;
    }

    private String toHexHash(String hash) {
        if (hash.startsWith("0x")) {
            return hash;
        }
        return "0x" + hash;
    }

    private Optional<TransactionReceipt> sendTransactionReceiptRequest(String transactionHash)
            throws Exception {
        EthGetTransactionReceipt transactionReceipt = web3j
                .ethGetTransactionReceipt(transactionHash)
                .sendAsync()
                .get();

        return transactionReceipt.getTransactionReceipt();
    }

    private Optional<TransactionReceipt> getTransactionReceipt(String transactionHash,
            int sleepDuration, int attempts) throws Exception {
        Optional<TransactionReceipt> receiptOptional =
                sendTransactionReceiptRequest(transactionHash);
        for (int i = 0; i < attempts; i++) {
            if (!receiptOptional.isPresent()) {
                Thread.sleep(sleepDuration);
                receiptOptional = sendTransactionReceiptRequest(transactionHash);
            } else {
                break;
            }
        }
        return receiptOptional;
    }

    private TransactionReceipt waitForTransactionReceipt(String transactionHash) throws Exception {
        Optional<TransactionReceipt> transactionReceiptOptional =
                getTransactionReceipt(transactionHash, SLEEP_DURATION, ATTEMPTS);

        if (!transactionReceiptOptional.isPresent()) {
            throw new BusinessException(
                    "Transaction receipt not generated after " + ATTEMPTS + " attempts");
        }
        return transactionReceiptOptional.get();
    }

    private void updateWalletBalances(String from, String to) throws Exception {
        Wallet fromWallet = crossStorageApi.find(defaultRepo, from, Wallet.class);
        Wallet toWallet = crossStorageApi.find(defaultRepo, to, Wallet.class);

        EthGetBalance toBalance = web3j.ethGetBalance(to, LATEST).sendAsync().get();
        EthGetBalance fromBalance = web3j.ethGetBalance(from, LATEST).sendAsync().get();

        fromWallet.setBalance(fromBalance.getBalance().toString());
        toWallet.setBalance(toBalance.getBalance().toString());
        crossStorageApi.createOrUpdate(defaultRepo, fromWallet);
        crossStorageApi.createOrUpdate(defaultRepo, toWallet);
    }

    private String transferDB(String from, String to, BigInteger amount, String type,
            String description) throws Exception {
        String transactionHash = "";
        Wallet toWallet = crossStorageApi.find(defaultRepo, to, Wallet.class);
        Wallet fromWallet = crossStorageApi.find(defaultRepo, from, Wallet.class);
        if (fromWallet.getPrivateKey() == null) {
            throw new Exception("wallet has no private key");
        }
        String privateKey = normalizeHash(fromWallet.getPrivateKey());

        BigInteger originBalance = new BigInteger(fromWallet.getBalance());
        if (amount.compareTo(originBalance) > 0) {
            throw new BusinessException(INSUFFICIENT_BALANCE);
        }

        List<Transaction> walletTransactions = crossStorageApi.find(defaultRepo, Transaction.class)
                .by("fromHexHash", fromWallet.getUuid()).getResults();
        BigInteger nonce = BigInteger.ONE;
        if (walletTransactions != null && walletTransactions.size() > 0) {
			
            Collections.sort(walletTransactions, new Comparator<Transaction>() {
              @Override
              public int compare(Transaction lhs, Transaction rhs) {
                  if (Long.parseLong(lhs.getNonce())<=Long.parseLong(rhs.getNonce())){
                      return 1;
                  }
                  return -1;
              }
          	});
			
            Transaction lastTransaction = walletTransactions.get(0);
            try {
                nonce = BigInteger.valueOf(Long.parseLong(lastTransaction.getNonce()) + 1);
            } catch (Exception e) {
                log.error("invalid nonce :{}", lastTransaction.getNonce());
            }
        }
        String recipientAddress = "0x" + toWallet.getUuid();
        String data = String.format("{\"type\":\"%s\",\"description\":\"%s\"", type, description);
        BigInteger gasLimit = BigInteger.ZERO;
        BigInteger gasPrice = BigInteger.ZERO;
        RawTransaction rawTransaction = RawTransaction
                .createTransaction(nonce, gasPrice, gasLimit, recipientAddress, amount, data);
        Credentials credentials = Credentials.create(privateKey);
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String encodedTransaction = Numeric.toHexString(signedMessage);

        Transaction transaction = new Transaction();
        transactionHash = Hash.sha3(encodedTransaction);
        transaction.setHexHash(normalizeHash(transactionHash));
        transaction.setFromHexHash(fromWallet.getUuid());
        transaction.setToHexHash(toWallet.getUuid());
        transaction.setNonce("" + nonce);
        transaction.setGasPrice(gasPrice.toString());
        transaction.setGasLimit(gasLimit.toString());
        transaction.setValue(amount.toString());
        transaction.setData(data);
        transaction.setSignedHash(normalizeHash(encodedTransaction));
        transaction.setCreationDate(java.time.Instant.now());

        crossStorageApi.createOrUpdate(defaultRepo, transaction);

        // FIXME: you should get the BlockForgerScript from scriptService
        BlockForgerScript.addTransaction(transaction);

        return transactionHash;
    }

    private String transferBesu(String from, String to, BigInteger amount,
            String type, String description) throws Exception {
        Wallet fromWallet = crossStorageApi.find(defaultRepo, from, Wallet.class);
        String privateKey = fromWallet.getPrivateKey();
        BigInteger balance = BigInteger.ZERO;

        if (fromWallet.getBalance() == null || fromWallet.getBalance().isEmpty()) {
            balance = web3j.ethGetBalance(from, LATEST).sendAsync().get().getBalance();

        } else {
            balance = new BigInteger(fromWallet.getBalance());
        }

        if (balance.compareTo(amount) < 0) {
            throw new BusinessException(INSUFFICIENT_BALANCE);
        }

        Credentials credentials = Credentials.create(privateKey);
        EthGetTransactionCount ethGetTransactionCount = web3j
                .ethGetTransactionCount(credentials.getAddress(), LATEST)
                .send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        org.web3j.protocol.core.methods.request.Transaction transaction =
                org.web3j.protocol.core.methods.request.Transaction
                        .createEtherTransaction(from, nonce, defaultGasPrice,
                                defaultGasLimit, to, amount);

        BigInteger estimatedGas = web3j.ethEstimateGas(transaction).send().getAmountUsed();
        log.debug("estimatedGas: {}", estimatedGas);
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        log.debug("gasPrice: {}", gasPrice);

        RawTransaction rawTransaction = RawTransaction
                .createEtherTransaction(nonce, gasPrice, defaultGasLimit, to, amount);

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String encodedTransaction = Numeric.toHexString(signedMessage);

        EthSendTransaction ethSendTransaction = web3j
                .ethSendRawTransaction(encodedTransaction)
                .sendAsync()
                .get();

        String transactionHash = ethSendTransaction.getTransactionHash();
        log.debug("pending transactionHash: {}", transactionHash);

        if (transactionHash == null || transactionHash.isEmpty()) {
            throw new BusinessException(TRANSACTION_FAILED);
        }

        TransactionReceipt transactionReceipt = waitForTransactionReceipt(transactionHash);

        String completedTransactionHash = transactionReceipt.getTransactionHash();
        log.debug("completed transactionHash: {}", completedTransactionHash);

        updateWalletBalances(from, to);

        return completedTransactionHash;
    }

    private String transferBesuDB(String from, String to, BigInteger amount,
            String type, String description) throws Exception {

        Wallet fromWallet = crossStorageApi.find(defaultRepo, from, Wallet.class);
        Wallet toWallet = crossStorageApi.find(defaultRepo, to, Wallet.class);

        String privateKey = fromWallet.getPrivateKey();
        BigInteger balance = BigInteger.ZERO;

        if (fromWallet.getBalance() == null || fromWallet.getBalance().isEmpty()) {
            balance = web3j.ethGetBalance(from, LATEST).sendAsync().get().getBalance();

        } else {
            balance = new BigInteger(fromWallet.getBalance());
        }

        if (balance.compareTo(amount) < 0) {
            throw new BusinessException(INSUFFICIENT_BALANCE);
        }

        Credentials credentials = Credentials.create(privateKey);
        EthGetTransactionCount ethGetTransactionCount = web3j
                .ethGetTransactionCount(credentials.getAddress(), LATEST)
                .send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        log.debug("gasPrice: {}", gasPrice);

        RawTransaction rawTransaction = RawTransaction
                .createEtherTransaction(nonce, gasPrice, defaultGasLimit, to, amount);

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String encodedTransaction = Numeric.toHexString(signedMessage);

        EthSendTransaction ethSendTransaction = web3j
                .ethSendRawTransaction(encodedTransaction)
                .sendAsync()
                .get();

        String transactionHash = ethSendTransaction.getTransactionHash();
        log.debug("pending transactionHash: {}", transactionHash);

        if (transactionHash == null || transactionHash.isEmpty()) {
            throw new BusinessException(TRANSACTION_FAILED);
        }

        TransactionReceipt transactionReceipt = waitForTransactionReceipt(transactionHash);

        String completedTransactionHash = transactionReceipt.getTransactionHash();
        log.debug("completed transactionHash: {}", completedTransactionHash);

        String data = String.format("{\"type\":\"%s\",\"description\":\"%s\"", type, description);

        Transaction transaction = new Transaction();
        transaction.setHexHash(normalizeHash(completedTransactionHash));
        transaction.setFromHexHash(fromWallet.getUuid());
        transaction.setToHexHash(toWallet.getUuid());
        transaction.setNonce("" + nonce);
        transaction.setGasPrice(gasPrice.toString());
        transaction.setGasLimit(defaultGasLimit.toString());
        transaction.setValue(amount.toString());
        transaction.setData(data);
        transaction.setSignedHash(normalizeHash(encodedTransaction));
        transaction.setCreationDate(java.time.Instant.now());

        crossStorageApi.createOrUpdate(defaultRepo, transaction);

        updateWalletBalances(from, to);

        return completedTransactionHash;
    }

    private String transferSmartContract(String from, String to, BigInteger amount,
            String type, String description) throws Exception {
        String sender = normalizeHash(from);
        String recipient = normalizeHash(to);

        Wallet fromWallet = crossStorageApi.find(defaultRepo, sender, Wallet.class);
        String privateKey = fromWallet.getPrivateKey();
        Credentials credentials = Credentials.create(privateKey);
        BigInteger balance = BigInteger.ZERO;

        if (fromWallet.getBalance() == null || fromWallet.getBalance().isEmpty()) {
            balance = web3j.ethGetBalance(from, LATEST).sendAsync().get().getBalance();
        } else {
            balance = new BigInteger(fromWallet.getBalance());
        }

        if (balance.compareTo(amount) < 0) {
            throw new BusinessException(INSUFFICIENT_BALANCE);
        }

        RawTransactionManager manager = new RawTransactionManager(web3j, credentials);
        Function function = new Function(
                "transferFrom",
                Arrays.asList(new Address(from), new Address(to), new Uint256(amount)),
                Arrays.asList(new TypeReference<Bool>() {}));
        String data = FunctionEncoder.encode(function);

        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        EthSendTransaction transaction = manager.sendTransaction(
                gasPrice,
                defaultGasLimit,
                smartContract,
                data,
                null);
        String transactionHash = transaction.getTransactionHash();
        log.debug("pending transactionHash: {}", transactionHash);

        if (transactionHash == null || transactionHash.isEmpty()) {
            throw new BusinessException(TRANSACTION_FAILED);
        }

        TransactionReceipt transactionReceipt = waitForTransactionReceipt(transactionHash);

        String completedTransactionHash = transactionReceipt.getTransactionHash();
        log.debug("completed transactionHash: {}", completedTransactionHash);

        updateWalletBalances(sender, recipient);

        return completedTransactionHash;
    }

    private String transferFabric(String from, String to, BigInteger amount,
            String type, String description) throws Exception {
        return "";
    }

    public String transfer(String from, String to, BigInteger amount)
            throws Exception {
        String message = String.format(
                "You received %s coins !",
                Convert.fromWei(amount.toString(), Unit.ETHER));
        return transfer(from, to, amount, "transfer", "Transfer coins", message);
    }

    public String transfer(String from, String to, BigInteger amount, String type,
            String description, String message) throws Exception {
        String transactionHash = "";
        String recipientAddress = normalizeHash(to);
        String senderAddress = normalizeHash(from);
        switch (BLOCKCHAIN_BACKEND) {
            case BESU:
                transactionHash = transferBesu(
                        senderAddress,
                        recipientAddress,
                        amount,
                        type,
                        description);
                break;
            case FABRIC:
                transactionHash = transferFabric(
                        senderAddress,
                        recipientAddress,
                        amount,
                        type,
                        description);
                break;
            case SMART_CONTRACT:
                transactionHash = transferSmartContract(
                        from,
                        to,
                        amount,
                        type,
                        description);
                break;
            case BESU_DB:
                transactionHash = transferBesuDB(
                        from,
                        to,
                        amount,
                        type,
                        description);
                break;
            default:
                transactionHash = transferDB(
                        senderAddress,
                        recipientAddress,
                        amount,
                        type,
                        description);
                break;
        }
        // TODO - send notification to the user e.g. CloudMessaging
        // try{
        // if(!transactionHash.isEmpty()){
        // cloudMessaging.setUserId(recipientAddress);
        // cloudMessaging.setTitle("Telecel Play");
        // cloudMessaging.setBody(message);
        // cloudMessaging.execute(null);
        // }
        // } catch(Exception e){
        // log.warn("cannot send notification to {}:{}",toAddress,message);
        // }
        return transactionHash;
    }

    // used to transfer from local account
    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        String transactionHash = "";
        try {
            transactionHash = transfer(fromAddress, toAddress, new BigInteger(value));
            result = "{\"transaction_hash\":\"" + transactionHash + "\"}";
        } catch (Exception e) {
            log.error("Transfer error", e);
            result = "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

}


class HttpService extends Service {

    public static final String DEFAULT_URL = "http://localhost:8545/";
    private static final Logger log = LoggerFactory.getLogger(HttpService.class);

    private Client httpClient;
    private final String url;
    private final boolean includeRawResponse;
    private Map<String, String> headers = new HashMap<>();

    public HttpService(String url, Client httpClient, boolean includeRawResponse) {
        super(includeRawResponse);
        this.url = url;
        this.httpClient = httpClient;
        this.includeRawResponse = includeRawResponse;
    }

    public HttpService(Client httpClient, boolean includeRawResponse) {
        this(DEFAULT_URL, httpClient, includeRawResponse);
    }

    public HttpService(String url, Client httpClient) {
        this(url, httpClient, false);
    }

    public HttpService(String url) {
        this(url, createHttpClient());
    }

    public HttpService(String url, boolean includeRawResponse) {
        this(url, createHttpClient(), includeRawResponse);
    }

    public HttpService(Client httpClient) {
        this(DEFAULT_URL, httpClient);
    }

    public HttpService(boolean includeRawResponse) {
        this(DEFAULT_URL, includeRawResponse);
    }

    public HttpService() {
        this(DEFAULT_URL);
    }

    private static Client createHttpClient() {
        return ClientBuilder.newClient();
    }

    @Override
    protected InputStream performIO(String request) throws IOException {

        log.debug("Request: {}", request);

        Response response = null;
        try {
            response = httpClient.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .headers(convertHeaders())
                    .post(Entity.json(request));
        } catch (ClientConnectionException e) {
            throw new IOException("Unable to connect to " + url, e);
        }

        if (response.getStatus() != 200) {
            throw new IOException(
                    "Error " + response.getStatus() + ": " + response.readEntity(String.class));
        }

        if (includeRawResponse) {
            return new BufferedInputStream(response.readEntity(InputStream.class));
        }

        return new ByteArrayInputStream(response.readEntity(String.class).getBytes());
    }

    private MultivaluedMap<String, Object> convertHeaders() {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        for (Map.Entry<String, String> entry : this.headers.entrySet()) {
            headers.put(entry.getKey(), Arrays.asList(entry.getValue()));
        }
        return headers;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void addHeaders(Map<String, String> headersToAdd) {
        headers.putAll(headersToAdd);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void close() throws IOException {}

}
