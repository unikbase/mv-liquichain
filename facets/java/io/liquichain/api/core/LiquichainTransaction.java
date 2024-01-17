package io.liquichain.api.core;

import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.firebase.CloudMessaging;
import org.meveo.model.customEntities.LiquichainApp;
import org.meveo.model.customEntities.Transaction;
import org.meveo.model.customEntities.Wallet;
import org.meveo.model.storage.Repository;
import org.meveo.service.script.Script;
import org.meveo.service.storage.RepositoryService;

import io.liquichain.core.BlockForgerScript;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.exceptions.ClientConnectionException;
import org.web3j.tx.RawTransactionManager;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;
import org.web3j.utils.Numeric;

public class LiquichainTransaction extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(LiquichainTransaction.class);

    private static final int SLEEP_DURATION = 1000;
    private static final int ATTEMPTS = 40;
    private static final String INSUFFICIENT_BALANCE = "Insufficient balance";
    private static final String TRANSACTION_FAILED = "Transaction failed";
    private static final String TRANSACTION_DATA_FORMAT = "{\"type\":\"%s\",\"description\":\"%s\"}";

    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final ParamBeanFactory paramBeanFactory = getCDIBean(ParamBeanFactory.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    //    private final ScriptInstanceService scriptInstanceService = getCDIBean(ScriptInstanceService.class);
    //    private final ScriptInterface cloudMessagingScript = scriptInstanceService.getExecutionEngine(
    //            CloudMessaging.class.getName(), null);
    private final CloudMessaging cloudMessaging = new CloudMessaging();

    private final Repository defaultRepo = repositoryService.findDefaultRepository();
    private final ParamBean config = paramBeanFactory.getInstance();
    private final BigInteger defaultGasLimit = new BigInteger(config.getProperty("besu.gas.limit", "120000"));
    private final BigInteger defaultGasPrice = new BigInteger(config.getProperty("besu.gas.price", "0"));
    private final String appName = config.getProperty("eth.api.appname", "licoin");
    private final String besuApiUrl = config.getProperty("besu.api.url", "https://testnet.liquichain.io/rpc");
    private final String blockchainType = config.getProperty("txn.blockchain.type", "BESU");

    private enum BLOCKCHAIN_TYPE {DATABASE, BESU_ONLY, FABRIC, BESU;}

    private final BLOCKCHAIN_TYPE BLOCKCHAIN_BACKEND = BLOCKCHAIN_TYPE.valueOf(blockchainType);

    private final String smartContract;
    private final Web3j web3j;

    {
        LiquichainApp liquichainApp = crossStorageApi.find(defaultRepo, LiquichainApp.class)
                                                     .by("name", appName)
                                                     .getResult();
        smartContract = liquichainApp.getHexCode();
        web3j = Web3j.build(new HttpService(besuApiUrl));
    }

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
            return hash.substring(2).toLowerCase();
        }
        return hash.toLowerCase();
    }

    private String toHexHash(String hash) {
        if (hash.startsWith("0x")) {
            return hash.toLowerCase();
        }
        return "0x" + hash.toLowerCase();
    }

    public Optional<TransactionReceipt> sendTransactionReceiptRequest(String transactionHash) throws Exception {
        EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();

        return transactionReceipt.getTransactionReceipt();
    }

    private Optional<TransactionReceipt> getTransactionReceipt(String transactionHash) throws Exception {
        Optional<TransactionReceipt> receiptOptional = sendTransactionReceiptRequest(transactionHash);
        for (int i = 0; i < ATTEMPTS; i++) {
            if (!receiptOptional.isPresent()) {
                Thread.sleep(SLEEP_DURATION);
                receiptOptional = sendTransactionReceiptRequest(transactionHash);
            } else {
                break;
            }
        }
        return receiptOptional;
    }

    private TransactionReceipt waitForTransactionReceipt(String transactionHash) throws Exception {
        Optional<TransactionReceipt> transactionReceiptOptional = getTransactionReceipt(transactionHash);
        if (!transactionReceiptOptional.isPresent()) {
            throw new BusinessException("Transaction receipt not generated after " + ATTEMPTS + " attempts");
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

    private String transferDB(String from, String to, BigInteger amount, String type, String description)
            throws Exception {
        String transactionHash;

        String sender = normalizeHash(from);
        String recipient = normalizeHash(to);

        Wallet fromWallet = crossStorageApi.find(defaultRepo, sender, Wallet.class);
        Wallet toWallet = crossStorageApi.find(defaultRepo, recipient, Wallet.class);
        if (fromWallet.getPrivateKey() == null) {
            throw new Exception("wallet has no private key");
        }
        String privateKey = normalizeHash(fromWallet.getPrivateKey());

        BigInteger originBalance = new BigInteger(fromWallet.getBalance());
        if (amount.compareTo(originBalance) > 0) {
            throw new BusinessException(INSUFFICIENT_BALANCE);
        }

        Transaction lastTransaction = crossStorageApi.find(defaultRepo, Transaction.class)
                                                     .by("fromHexHash", fromWallet.getUuid())
                                                     .orderBy("nonce", false) // by largest to smallest
                                                     .getResult();
        BigInteger nonce = BigInteger.ONE;
        if (lastTransaction != null) {
            try {
                nonce = BigInteger.valueOf(Long.parseLong(lastTransaction.getNonce()) + 1);
            } catch (NumberFormatException e) {
                LOG.error("invalid nonce :{}", lastTransaction.getNonce());
            }
        }

        String recipientAddress = "0x" + toWallet.getUuid();
        String data = String.format(TRANSACTION_DATA_FORMAT, type, description);
        BigInteger gasLimit = BigInteger.ZERO;
        BigInteger gasPrice = BigInteger.ZERO;
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, recipientAddress,
                amount, data);
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
        transaction.setType(type);
        transaction.setSignedHash(normalizeHash(encodedTransaction));
        transaction.setCreationDate(java.time.Instant.now());
        transaction.setInitiator(fromWallet.getUuid());

        crossStorageApi.createOrUpdate(defaultRepo, transaction);

        // FIXME: you should get the BlockForgerScript from scriptService
        BlockForgerScript.addTransaction(transaction);

        return transactionHash;
    }

    private String transferBesu(String from, String to, BigInteger amount, String type, String description)
            throws Exception {
        String sender = normalizeHash(from);

        Wallet fromWallet = crossStorageApi.find(defaultRepo, sender, Wallet.class);
        String privateKey = fromWallet.getPrivateKey();
        BigInteger balance;

        if (fromWallet.getBalance() == null || fromWallet.getBalance().isEmpty()) {
            balance = web3j.ethGetBalance(from, LATEST).sendAsync().get().getBalance();

        } else {
            balance = new BigInteger(fromWallet.getBalance());
        }

        if (balance.compareTo(amount) < 0) {
            throw new BusinessException(INSUFFICIENT_BALANCE);
        }

        Credentials credentials = Credentials.create(privateKey);
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(credentials.getAddress(), LATEST)
                                                             .send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        org.web3j.protocol.core.methods.request.Transaction transaction =
                org.web3j.protocol.core.methods.request.Transaction.createEtherTransaction(
                        from, nonce, defaultGasPrice, defaultGasLimit, to, amount);

        BigInteger estimatedGas = web3j.ethEstimateGas(transaction).send().getAmountUsed();
        LOG.debug("estimatedGas: {}", estimatedGas);
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        LOG.debug("gasPrice: {}", gasPrice);

        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, defaultGasLimit, to,
                amount);

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String encodedTransaction = Numeric.toHexString(signedMessage);

        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(encodedTransaction).sendAsync().get();

        String transactionHash = ethSendTransaction.getTransactionHash();
        LOG.debug("pending transactionHash: {}", transactionHash);

        if (transactionHash == null || transactionHash.isEmpty()) {
            throw new BusinessException(TRANSACTION_FAILED);
        }

        TransactionReceipt transactionReceipt = waitForTransactionReceipt(transactionHash);

        String completedTransactionHash = transactionReceipt.getTransactionHash();
        LOG.debug("completed transactionHash: {}", completedTransactionHash);

        //        updateWalletBalances(from, to);

        return completedTransactionHash;
    }

    private synchronized String transferBesuDB(String from, String to, BigInteger amount, String type,
            String description) throws Exception {

        String sender = normalizeHash(from);
        String recipient = normalizeHash(to);

        Wallet fromWallet = crossStorageApi.find(defaultRepo, sender, Wallet.class);
        Wallet toWallet = crossStorageApi.find(defaultRepo, recipient, Wallet.class);

        String privateKey = fromWallet.getPrivateKey();
        BigInteger balance = web3j.ethGetBalance(from, LATEST).sendAsync().get().getBalance();
        LOG.debug("{} balance: {}", from, balance);

        if (balance.compareTo(amount) < 0) {
            throw new BusinessException(INSUFFICIENT_BALANCE);
        }

        Credentials credentials = Credentials.create(privateKey);
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(credentials.getAddress(), LATEST)
                                                             .send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        LOG.debug("gasPrice: {}", gasPrice);
        LOG.debug("gasLimit: {}", defaultGasLimit);

        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, defaultGasLimit, to,
                amount);
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String encodedTransaction = Numeric.toHexString(signedMessage);

        RawTransactionManager manager = new RawTransactionManager(web3j, credentials);

        LOG.debug("raw transaction manager created");

        Function function = new Function("transfer", Arrays.asList(new Address(toHexHash(to)), new Uint256(amount)),
                List.of(new TypeReference<Bool>() {
                }));
        String data = FunctionEncoder.encode(function);

        LOG.debug("transfer function encoded");

        EthSendTransaction sendTransaction = manager.sendTransaction(gasPrice, defaultGasLimit, smartContract, data,
                null);

        LOG.debug("sending transaction");

        String transactionHash = sendTransaction.getTransactionHash();

        LOG.debug("pending transactionHash: {}", transactionHash);

        if (transactionHash == null || transactionHash.isEmpty()) {
            throw new BusinessException(TRANSACTION_FAILED);
        }

        // TransactionReceipt transactionReceipt = waitForTransactionReceipt(transactionHash);
        // String completedTransactionHash = transactionReceipt.getTransactionHash();
        // LOG.debug("completed transactionHash: {}", completedTransactionHash);

        String extraData = String.format(TRANSACTION_DATA_FORMAT, type, description);

        Transaction transaction = new Transaction();
        // transaction.setHexHash(normalizeHash(completedTransactionHash));
        transaction.setHexHash(normalizeHash(transactionHash));
        transaction.setFromHexHash(fromWallet.getUuid());
        transaction.setToHexHash(toWallet.getUuid());
        transaction.setNonce("" + nonce);
        transaction.setGasPrice(gasPrice.toString());
        transaction.setGasLimit(defaultGasLimit.toString());
        transaction.setValue(amount.toString());
        transaction.setData(extraData);
        transaction.setType(type);
        transaction.setSignedHash(normalizeHash(encodedTransaction));
        transaction.setCreationDate(java.time.Instant.now());
        // transaction.setBlockNumber(normalizeHash(transactionReceipt.getBlockNumberRaw()));
        // transaction.setBlockHash(normalizeHash(transactionReceipt.getBlockHash()));
        // transaction.setTransactionIndex(transactionReceipt.getTransactionIndex().longValue());
        transaction.setBlockNumber("1");
        transaction.setBlockHash("e8594f30d08b412027f4546506249d09134b9283530243e01e4cdbc34945bcf0");
        transaction.setInitiator(fromWallet.getUuid());

        crossStorageApi.createOrUpdate(defaultRepo, transaction);

        // updateWalletBalances(from, to);
        return transactionHash;
    }

    public String transferSmartContract(String from, String to, BigInteger amount, String type, String description,
            String message, String initiator) throws Exception {
        return this.transferSmartContract(0, from, to, amount, type, description, message, initiator);
    }

    public String transferSmartContract(int tokenId, String from, String to, BigInteger amount, String type,
            String description, String message, String initiator) throws Exception {
        String sender = normalizeHash(from);
        String recipient = normalizeHash(to);

        LOG.debug("transfer amount:{} to:{}", amount, toHexHash(to));

        Wallet fromWallet = crossStorageApi.find(defaultRepo, sender, Wallet.class);
        Wallet toWallet = crossStorageApi.find(defaultRepo, recipient, Wallet.class);

        LOG.debug("wallets retrieved sender:{} recipient:{}", sender, recipient);

        String privateKey = fromWallet.getPrivateKey();
        Credentials credentials = Credentials.create(privateKey);
        RawTransactionManager manager = new RawTransactionManager(web3j, credentials);
        LOG.debug("raw transaction manager created");

        BigInteger balance = BigInteger.ZERO;

        Function function = new Function("transfer",
                Arrays.asList(new Address(toHexHash(to)), new Uint256(tokenId), new Uint256(amount)),
                List.of(new TypeReference<Bool>() {
                }));
        String data = FunctionEncoder.encode(function);

        LOG.debug("transfer function encoded");

        BigInteger gasPrice = BigInteger.ZERO;
        EthSendTransaction sendTransaction = manager.sendTransaction(gasPrice, defaultGasLimit, smartContract, data,
                null);

        LOG.debug("sending transaction");

        String transactionHash = sendTransaction.getTransactionHash();

        LOG.debug("pending transactionHash: {}", transactionHash);

        if (transactionHash == null || transactionHash.isEmpty()) {
            throw new BusinessException(TRANSACTION_FAILED);
        }

        // TransactionReceipt transactionReceipt = waitForTransactionReceipt(transactionHash);

        // String completedTransactionHash = transactionReceipt.getTransactionHash();
        // LOG.debug("completed transactionHash: {}", completedTransactionHash);
        // LOG.debug("transactionReceipt: {}", transactionReceipt.toString());

        LOG.debug("fetching nonce");

        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(credentials.getAddress(), LATEST)
                                                             .send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        LOG.debug("transaction nonce: {}", nonce);

        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, defaultGasLimit, to,
                amount);
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String encodedTransaction = Numeric.toHexString(signedMessage);

        String transactionData = String.format(TRANSACTION_DATA_FORMAT, type, description);

        LOG.debug("saving transaction to database");

        Transaction transaction = new Transaction();
        transaction.setHexHash(normalizeHash(transactionHash));
        transaction.setFromHexHash(fromWallet.getUuid());
        transaction.setToHexHash(toWallet.getUuid());
        transaction.setNonce("" + nonce);
        transaction.setGasPrice(gasPrice.toString());
        transaction.setGasLimit(defaultGasLimit.toString());
        transaction.setValue(amount.toString());
        transaction.setData(transactionData);
        transaction.setType(type);
        transaction.setSignedHash(normalizeHash(encodedTransaction));
        transaction.setCreationDate(java.time.Instant.now());
        // transaction.setBlockNumber(normalizeHash(transactionReceipt.getBlockNumberRaw()));
        // transaction.setBlockHash(normalizeHash(transactionReceipt.getBlockHash()));
        // transaction.setTransactionIndex(transactionReceipt.getTransactionIndex().longValue());
        transaction.setBlockNumber("1");
        transaction.setBlockHash("e8594f30d08b412027f4546506249d09134b9283530243e01e4cdbc34945bcf0");
        transaction.setInitiator(initiator);

        crossStorageApi.createOrUpdate(defaultRepo, transaction);

        // updateWalletBalances(sender, recipient);

        LOG.debug("sending transaction notification");
        Map<String, String> notificationData = new HashMap<>() {{
            put("type", CloudMessaging.formatType(type));
        }};

        cloudMessaging.sendNotification(crossStorageApi, defaultRepo, recipient, "Telecel Play", message,
                notificationData);
        return transactionHash;
    }

    private String transferFabric(String from, String to, BigInteger amount, String type, String description)
            throws Exception {
        return "";
    }

    public synchronized String transfer(String from, String to, BigInteger amount) throws Exception {
        String message = String.format("You received %s coins !", Convert.fromWei(amount.toString(), Unit.ETHER));
        return transfer(from, to, amount, "transfer", "Transfer coins", message);
    }

    public synchronized String transfer(String from, String to, BigInteger amount, String type, String description,
            String message) throws Exception {
        String transactionHash;
        String recipientAddress = normalizeHash(to);
        String senderAddress = normalizeHash(from);
        switch (BLOCKCHAIN_BACKEND) {
        case BESU_ONLY:
            transactionHash = transferBesu(senderAddress, recipientAddress, amount, type, description);
            break;
        case FABRIC:
            transactionHash = transferFabric(senderAddress, recipientAddress, amount, type, description);
            break;
        case BESU:
            transactionHash = transferBesuDB(from, to, amount, type, description);
            break;
        default:
            transactionHash = transferDB(senderAddress, recipientAddress, amount, type, description);
            break;
        }

        if (!transactionHash.isEmpty()) {
            Map<String, String> notificationData = new HashMap<>() {{
                put("type", CloudMessaging.formatType(type));
            }};

            cloudMessaging.sendNotification(crossStorageApi, defaultRepo, recipientAddress, "Telecel Play", message,
                    notificationData);
        }

        return transactionHash;
    }

    // used to transfer from local account
    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        String transactionHash;
        try {
            transactionHash = transfer(fromAddress, toAddress, new BigInteger(value));
            result = "{\"transaction_hash\":\"" + transactionHash + "\"}";
        } catch (Exception e) {
            LOG.error("Transfer error", e);
            result = "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}

class HttpService extends Service {

    public static final String DEFAULT_URL = "http://localhost:8545/";
    private static final Logger LOG = LoggerFactory.getLogger(HttpService.class);

    private Client httpClient;
    private final String url;
    private final boolean includeRawResponse;
    private final Map<String, String> headers = new HashMap<>();

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

        LOG.debug("Request: {}", request);

        Response response;
        try {
            response = httpClient.target(url).request(MediaType.APPLICATION_JSON).headers(convertHeaders())
                                 .post(Entity.json(request));
        } catch (ClientConnectionException e) {
            throw new IOException("Unable to connect to " + url, e);
        }

        if (response.getStatus() != 200) {
            throw new IOException("Error " + response.getStatus() + ": " + response.readEntity(String.class));
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
    public void close() throws IOException {
    }

}
