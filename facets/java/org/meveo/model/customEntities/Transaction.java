package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;
import java.io.Serializable;
import java.util.List;
import org.meveo.model.persistence.DBStorageType;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Transaction implements CustomEntity, Serializable {

    public Transaction() {
    }

    public Transaction(String uuid) {
        this.uuid = uuid;
    }

    private String uuid;

    @JsonIgnore()
    private DBStorageType storages;

    private String blockHash;

    private String data;

    private String nodeSignature;

    private String toUsername;

    private String initiator;

    private String fromHexHash;

    private Long transactionIndex;

    private Long filesize;

    private String rawData;

    private String operatorCode;

    private String type;

    private String latestVerifiableCredential;

    private String result;

    private String gasLimit;

    private String jsonHash;

    private String assetId;

    private String hexHash;

    private String signedHash;

    private String value;

    private String gasPrice;

    private Instant creationDate;

    private String nonce;

    private String r;

    private String toHexHash;

    private String filename;

    private String s;

    private String lastUpdateTransactionId;

    private String v;

    private String blockNumber;

    private String assetName;

    private String fromUsername;

    private String zipHash;

    @Override()
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public DBStorageType getStorages() {
        return storages;
    }

    public void setStorages(DBStorageType storages) {
        this.storages = storages;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getNodeSignature() {
        return nodeSignature;
    }

    public void setNodeSignature(String nodeSignature) {
        this.nodeSignature = nodeSignature;
    }

    public String getToUsername() {
        return toUsername;
    }

    public void setToUsername(String toUsername) {
        this.toUsername = toUsername;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public String getFromHexHash() {
        return fromHexHash;
    }

    public void setFromHexHash(String fromHexHash) {
        this.fromHexHash = fromHexHash;
    }

    public Long getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(Long transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public Long getFilesize() {
        return filesize;
    }

    public void setFilesize(Long filesize) {
        this.filesize = filesize;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public String getOperatorCode() {
        return operatorCode;
    }

    public void setOperatorCode(String operatorCode) {
        this.operatorCode = operatorCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLatestVerifiableCredential() {
        return latestVerifiableCredential;
    }

    public void setLatestVerifiableCredential(String latestVerifiableCredential) {
        this.latestVerifiableCredential = latestVerifiableCredential;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(String gasLimit) {
        this.gasLimit = gasLimit;
    }

    public String getJsonHash() {
        return jsonHash;
    }

    public void setJsonHash(String jsonHash) {
        this.jsonHash = jsonHash;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getHexHash() {
        return hexHash;
    }

    public void setHexHash(String hexHash) {
        this.hexHash = hexHash;
    }

    public String getSignedHash() {
        return signedHash;
    }

    public void setSignedHash(String signedHash) {
        this.signedHash = signedHash;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(String gasPrice) {
        this.gasPrice = gasPrice;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getToHexHash() {
        return toHexHash;
    }

    public void setToHexHash(String toHexHash) {
        this.toHexHash = toHexHash;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getLastUpdateTransactionId() {
        return lastUpdateTransactionId;
    }

    public void setLastUpdateTransactionId(String lastUpdateTransactionId) {
        this.lastUpdateTransactionId = lastUpdateTransactionId;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    public String getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public String getZipHash() {
        return zipHash;
    }

    public void setZipHash(String zipHash) {
        this.zipHash = zipHash;
    }

    @Override()
    public String getCetCode() {
        return "Transaction";
    }
}
