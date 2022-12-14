package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;
import java.util.List;
import org.meveo.model.persistence.DBStorageType;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class TorrentAnnounce implements CustomEntity {

    public TorrentAnnounce() {
    }

    public TorrentAnnounce(String uuid) {
        this.uuid = uuid;
    }

    private String uuid;

    @JsonIgnore()
    private DBStorageType storages;

    private Instant anounceDate;

    private Instant lastAnnounceDate;

    private Long left;

    private String url;

    private Double longitude;

    private String status;

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

    public Instant getAnounceDate() {
        return anounceDate;
    }

    public void setAnounceDate(Instant anounceDate) {
        this.anounceDate = anounceDate;
    }

    public Instant getLastAnnounceDate() {
        return lastAnnounceDate;
    }

    public void setLastAnnounceDate(Instant lastAnnounceDate) {
        this.lastAnnounceDate = lastAnnounceDate;
    }

    public Long getLeft() {
        return left;
    }

    public void setLeft(Long left) {
        this.left = left;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override()
    public String getCetCode() {
        return "TorrentAnnounce";
    }
}
