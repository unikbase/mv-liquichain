package io.liquichain.core;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.model.customEntities.Block;
import org.meveo.model.customEntities.Transaction;
import org.meveo.model.customEntities.Wallet;
import org.meveo.model.persistence.CEIUtils;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;
import org.meveo.service.custom.CustomTableService;
import org.meveo.service.script.Script;
import org.meveo.service.storage.RepositoryService;

import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Hash;

public class BlockForgerScript extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(BlockForgerScript.class);

    private static long chainId = 76;

    private int networkId = 7;

    static public long blockHeight = 1;

    private Block parentBlock = null;

    private String exampleBlock = "{" +
        "\"difficulty\":\"0x5\"," +
        "\"extraData\":\"0xd58301090083626f7286676f312e3133856c696e75780000000000000000000021c9effaf6549e725463c7877ddebe9a2916e03228624e4bfd1e3f811da792772b54d9e4eb793c54afb4a29f014846736755043e4778999046d0577c6e57e72100\","
        + "\"gasLimit\":\"0xe984c2\"," + "\"gasUsed\":\"0x0\","
        + "\"hash\":\"0xaa14340feb15e26bc354bb839b2aa41cc7984676249c155ac5e4d281a8d08809\","
        + "\"logsBloom\":\"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\","
        + "\"miner\":\"0x0000000000000000000000000000000000000000\"," +
        "\"mixHash\":\"0x0000000000000000000000000000000000000000000000000000000000000000\"," +
        "\"nonce\":\"0x0000000000000000\"," +
        "\"number\":\"0x1b4\"," +
        "\"parentHash\":\"0xc8ccb81f484a428a3a1669d611f55f880b362b612f726711947d98f5bc5af573\"," +
        "\"receiptsRoot\":\"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\"," +
        "\"sha3Uncles\":\"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\"," +
        "\"size\":\"0x260\"," +
        "\"stateRoot\":\"0xffcb834d62706995e9e7bf10cc9a9e42a82fea998d59b3a5cfad8975dbfe3f87\"," +
        "\"timestamp\":\"0x5ed9a43f\"," +
        "\"totalDifficulty\":\"0x881\"," +
        "\"transactions\":[" + "]," +
        "\"transactionsRoot\":\"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\"," +
        "\"uncles\":[  " + "]}";


    private CustomFieldsCacheContainerProvider cetCache = getCDIBean(CustomFieldsCacheContainerProvider.class);
    private CrossStorageService crossStorageService = getCDIBean(CrossStorageService.class);
    private CustomTableService customTableService = getCDIBean(CustomTableService.class);

    private static PaginationConfiguration lastBlockPC =
        new PaginationConfiguration("blockNumber", SortOrder.DESCENDING);

    private CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private Repository defaultRepo = repositoryService.findDefaultRepository();


    private static List<Transaction> currentTransactions = new ArrayList<>();
    private static List<Transaction> nextTransactions = new ArrayList<>();

    private static Instant nextBlockDate;
    private static AtomicBoolean isForging = new AtomicBoolean(false);

    public static void addTransaction(Transaction t) {
        if (isForging.get()) {
            nextTransactions.add(t);
        } else {
            currentTransactions.add(t);
        }
    }

    public Block getLastBlock() {
        Block result = null;
        try {
            //log.debug("query : "+customTableService.getQuery("block", lastBlockPC));
            List<Map<String, Object>> res =
                crossStorageService.find(defaultRepo, cetCache.getCustomEntityTemplate("Block"), lastBlockPC);
            if (res.size() > 0) {
                result = CEIUtils.deserialize(res.get(0), Block.class);
                //log.debug("lastBlock number:{}",result.getBlockNumber());
            }
        } catch (Exception e) {
            LOG.error("getLastBlock:{}", e);
        }
        return result;
    }

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        //log.debug("execute forging");
        if (parentBlock == null) {
            //log.debug("retreive last block from chain");
            parentBlock = getLastBlock();
        }
        if (isForging.getAndSet(true)) {
            LOG.error("we are already forging");
            return;
        }
        if (currentTransactions.size() == 0) {
            //log.debug("no transaction to forge");
            blockHeight = parentBlock.getBlockNumber();
            isForging.set(false);
            return;
        } else {
            LOG.debug("forging {} transactions", currentTransactions.size());
            Map<String, Wallet> wallets = new HashMap<>();
            List<Transaction> orderedTransactions =
                currentTransactions.stream().sorted((t1, t2) -> (t1.getCreationDate().compareTo(t2.getCreationDate())))
                                   .collect(Collectors.toList());

            blockHeight = parentBlock.getBlockNumber() + 1;

            String transactionHashes = "";
            List<Transaction> invalidTransactions = new ArrayList<>();
            for (Transaction t : currentTransactions) {
                LOG.debug(" transaction date : {}", t.getCreationDate());
                if (!wallets.containsKey(t.getFromHexHash())) {
                    try {
                        Wallet originWallet = crossStorageApi.find(defaultRepo, t.getFromHexHash(), Wallet.class);
                        LOG.debug("add originWallet:{} {} to map", originWallet.getUuid(), originWallet.getBalance());
                        wallets.put(t.getFromHexHash(), originWallet);
                    } catch (Exception e) {
                        LOG.debug(" cannot find origin wallet, set blockNumber to INVALID");
                        t.setBlockNumber("INVALID");
                        try {
                            crossStorageApi.createOrUpdate(defaultRepo, t);
                        } catch (Exception ex) {
                            LOG.error("Failed to save transaction.", ex);
                        }
                        invalidTransactions.add(t);
                    }
                }
                if (t.getBlockNumber() == null) {
                    Wallet originWallet = wallets.get(t.getFromHexHash());
                    LOG.debug("originWallet 0x{} old balance:{}", t.getFromHexHash(), originWallet.getBalance());
                    BigInteger transacValue = new BigInteger(t.getValue());
                    if (new BigInteger(originWallet.getBalance()).compareTo(transacValue) >= 0) {
                        originWallet.setBalance(
                            "" + new BigInteger(originWallet.getBalance()).add(transacValue.negate()));
                        try {
                            Wallet destinationWallet =
                                crossStorageApi.find(defaultRepo, t.getToHexHash(), Wallet.class);
                            LOG.debug("destinationWallet 0x{} old balance:{}", t.getToHexHash(),
                                destinationWallet.getBalance());
                            destinationWallet.setBalance(
                                "" + new BigInteger(destinationWallet.getBalance()).add(transacValue));
                            crossStorageApi.createOrUpdate(defaultRepo, destinationWallet);
                            LOG.debug("destinationWallet 0x{} new balance:{}", t.getToHexHash(),
                                destinationWallet.getBalance());
                            transactionHashes += t.getHexHash();
                        } catch (Exception e) {
                            LOG.debug(" cannot find destination wallet, set blockNumber to INVALID");
                            t.setBlockNumber("INVALID");
                            try {
                                crossStorageApi.createOrUpdate(defaultRepo, t);
                            } catch (Exception ex) {
                                LOG.error("Failed to save transaction", ex);
                            }
                            invalidTransactions.add(t);
                        }
                    } else {
                        LOG.debug("insufficient balance, set blockNumber to INVALID");
                        t.setBlockNumber("INVALID");
                        try {
                            crossStorageApi.createOrUpdate(defaultRepo, t);
                        } catch (Exception ex) {
                            LOG.error("Failed to save transaction", ex);
                        }
                        invalidTransactions.add(t);
                    }
                }
            }
            currentTransactions.removeAll(invalidTransactions);
            Block block = new Block();
            block.setCreationDate(Instant.now());

            //FIXME parent should not be null
            block.setParentHash(parentBlock == null ? "" : parentBlock.getHash());
            block.setSize((long) currentTransactions.size());

            //FIXME hash parent hash
            block.setHash(Hash.sha3(transactionHashes).substring(2));

            block.setBlockNumber(blockHeight);
            try {
                crossStorageApi.createOrUpdate(defaultRepo, block);
                long i = 0;
                for (Transaction t : currentTransactions) {
                    Wallet originWallet = wallets.get(t.getFromHexHash());
                    LOG.debug("originWallet 0x{} new balance:{}", t.getFromHexHash(), originWallet.getBalance());
                    crossStorageApi.createOrUpdate(defaultRepo, originWallet);
                    t.setBlockHash(block.getHash());
                    t.setBlockNumber("" + block.getBlockNumber());
                    t.setTransactionIndex(i++);
                    crossStorageApi.createOrUpdate(defaultRepo, t);
                }

                parentBlock = block;
                currentTransactions = nextTransactions;
                nextTransactions = new ArrayList<>();
            } catch (Exception ex) {
                LOG.error("Failed to save block and transaction.", ex);
            }

            isForging.set(false);
        }
    }

}
