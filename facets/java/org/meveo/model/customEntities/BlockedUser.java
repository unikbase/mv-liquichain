package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;
import java.util.List;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.customEntities.Wallet;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class BlockedUser implements CustomEntity {

    public BlockedUser() {
    }

    public BlockedUser(String uuid) {
        this.uuid = uuid;
    }

    private String uuid;

    @JsonIgnore()
    private DBStorageType storages;

    private Wallet targetWallet;

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

    public Wallet getTargetWallet() {
        return targetWallet;
    }

    public void setTargetWallet(Wallet targetWallet) {
        this.targetWallet = targetWallet;
    }

    @Override()
    public String getCetCode() {
        return "BlockedUser";
    }
}
