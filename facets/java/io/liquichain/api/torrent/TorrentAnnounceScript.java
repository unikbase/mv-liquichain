package io.liquichain.api.torrent;

import org.meveo.model.crm.custom.*;
import javax.persistence.*;
import org.meveo.commons.utils.ReflectionUtils;
import java.lang.reflect.*;
import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;
import java.time.Instant;
import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.rest.technicalservice.impl.EndpointRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.utils.Strings;

import java.net.URLDecoder;

import org.meveo.model.customEntities.Wallet;
import org.meveo.model.customEntities.LiquichainApp;
import org.meveo.model.customEntities.TorrentAnnounce;
import org.meveo.model.storage.Repository;
import org.meveo.service.storage.RepositoryService;
import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.api.persistence.CrossStorageRequest;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.persistence.CEIUtils;
import org.meveo.api.exception.EntityDoesNotExistsException;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TorrentAnnounceScript extends Script {

  private static final Logger log = LoggerFactory.getLogger(TorrentAnnounceScript.class);

  private String peer_id;
  private String info_hash;
  private String port;
  private String downloaded;
  private String uploaded;
  private String left;
  private String event;
  private String compact;
  private String url;
  private String wallet_id;
  private String liveness;
  private String sign;
  private String latitude;
  private String longitude;

  private String result;

  private CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
  private RepositoryService repositoryService = getCDIBean(RepositoryService.class);
  private Repository defaultRepo = repositoryService.findDefaultRepository();

  private String projectId;

  public String getResult() {
    return result;
  }

  public void setPeer_id(String value) {
    if (value.startsWith("0x")) {
      peer_id = value.substring(2);
    } else {
      peer_id = value;
    }
  }

  public void setInfo_hash(String value) {
    if (value.startsWith("0x")) {
      info_hash = value.substring(2);
    } else {
      info_hash = value;
    }
  }

  public void setPort(String value) {
    port = value;
  }

  public void setDownloaded(String value) {
    downloaded = value;
  }

  public void setUploaded(String value) {
    uploaded = value;
  }

  public void setLeft(String value) {
    left = value;
  }

  public void setEvent(String value) {
    event = value;
  }

  public void setCompact(String value) {
    compact = value;
  }

  public void setUrl(String value) {
    url = value;
  }

  public void setWallet_id(String value) {
    wallet_id = value;
  }

  public void setLiveness(String value) {
    liveness = value;
  }

  public void setSign(String value) {
    sign = value;
  }

  public void setLatitude(String value) {
    latitude = value;
  }
  
  public void setLongitude(String value) {
    longitude = value;
  }

  private void parseQueryString(String qs) {
    String[] params = qs.split("&");
    for (String param : params) {
      if (param.startsWith("info_hash=")) {
        if (info_hash.length() != 40) {
          String infoHash = param.substring(10);
          try {
            infoHash = URLDecoder.decode(infoHash, "ISO-8859-1");
            info_hash = String.format("%x", new BigInteger(1, infoHash.getBytes()));
            log.info("info_hash={}", info_hash);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      if (param.startsWith("peer_id=")) {
        if (peer_id.length() != 40) {
          String peerId = param.substring(10);
          try {
            peerId = URLDecoder.decode(peerId, "ISO-8859-1");
            peer_id = String.format("%x", new BigInteger(1, peerId.getBytes()));
            log.info("peer_id={}", peer_id);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      if (param.startsWith("wallet_id=")) {
        if (wallet_id.length() != 40) {
          String walletId = param.substring(10);
          try {
            walletId = URLDecoder.decode(walletId, "ISO-8859-1");
            wallet_id = String.format("%x", new BigInteger(1, walletId.getBytes()));
            log.info("wallet_id={}", wallet_id);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  public static String unHex(String arg) {

    String str = "";
    for (int i = 0; i < arg.length(); i += 2) {
      String s = arg.substring(i, (i + 2));
      int decimal = Integer.parseInt(s, 16);
      str = str + (char) decimal;
    }
    return str;
  }

  private static Object getIdValue(Object object) {
    return ReflectionUtils.getAllFields(new ArrayList<>(), object.getClass()).stream()
        .filter(f -> f.getAnnotation(Id.class) != null).findFirst().map(f -> {
          try {
            f.setAccessible(true);
            return f.get(object);
          } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        }).orElse(null);
  }

  private static String getFieldForGetter(Class<?> clazz, Method getter) {
    String fieldName;
    if (getter.getName().startsWith("is")) {
      fieldName = getter.getName().substring(2);
    } else {
      fieldName = getter.getName().substring(3);
    }

    return ReflectionUtils.getAllFields(new ArrayList<>(), clazz).stream()
        .filter(f -> f.getName().toUpperCase().equals(fieldName.toUpperCase())).findFirst().map(Field::getName)
        .orElse(null);
  }

  @Override
  public void execute(Map<String, Object> parameters) throws BusinessException {
    // if wallet_id is set then peer_id is the device id, else the peer_id is the
    // wallet_id
    log.info(
        "Received announce: peer_id:{}, info_hash:{}, port:{}, downloaded:{}, uploaded:{}, left:{}, event:{}, compact:{}, wallet_id:{}, liveness:{},sign:{}",
        peer_id, info_hash, port, downloaded, uploaded, left, event, compact, wallet_id, liveness, sign);
    log.info("Received announce: {}", parameters);
    // fix the decoding of info_hash and peer_id using correct charset in case they
    // where not sent in 40 chars hexa
    EndpointRequest req = (EndpointRequest) parameters.get("request");
    boolean outputJson = "application/json".equals(req.getHeader("Accept"));
    log.info("announce output json: {}", outputJson);
    if ((info_hash.length() != 40) || (peer_id.length() != 40)) {
      parseQueryString(req.getQueryString());
    }
    if (wallet_id == null || (wallet_id.length() != 40)) {
      wallet_id = peer_id;
    }
    // lookup wallet with id walletId
    Wallet wallet = null;
    try {
      if (outputJson) {
        result = "{\"error\":\"cannot find wallet\"}";
      } else {
        result = "d14:failure reason27:cannot find wallet.e";
      }
      wallet = crossStorageApi.find(defaultRepo, Wallet.class).by("hexHash", wallet_id).getResult();
    } catch (Exception e) {
      throw new BusinessException(result);
    }
    log.info("found wallet {}", wallet);

    LiquichainApp app = null;
    Object cat = null;
    try {
      if (outputJson) {
        result = "{\"error\":\"cannot find info_hash app\"}";
      } else {
        result = "d14:failure reason26:cannot find info_hash app.e";
      }
      app = crossStorageApi.find(defaultRepo, LiquichainApp.class).by("hexCode", info_hash).getResult();
      log.info("found app {}", app.getName());
    } catch (Exception e) {
      // if the hash is not an app it could be a liquimart category
      // TODO: this processing should be generic and registered by user apps
      try {
        // retrieve the liquimart app
        app = crossStorageApi.find(defaultRepo, LiquichainApp.class).by("name", "Liquimart").getResult();
        log.info("found app {}", app);
        if (outputJson) {
          result = "{\"error\":\"cannot find info_hash category\"}";
        } else {
          result = "d14:failure reason26:cannot find info_hash cat.e";
        }
        cat = crossStorageApi.find(defaultRepo, Class.forName("org.meveo.model.customEntities.LiquimartProductCategory")).by("hash", info_hash).getResult();
        //log.info("found category {}", cat.getName());
      } catch (Exception ex) {
        throw new BusinessException(result);
      }
    }

    // retrieve ongoing announce
    TorrentAnnounce announce = null;
    CrossStorageRequest<TorrentAnnounce> csreq = crossStorageApi.find(defaultRepo, TorrentAnnounce.class).by("peerId",
        peer_id);
    if (cat != null) {
      csreq.by("infoHash", info_hash);
    }
    List<TorrentAnnounce> announces = csreq.by("application", app).by("status", "ONGOING").getResults();

    // events can be started,update,stopped,completed
    // TODO: if announce is too old then it should be closed and a new on created
    if (announces.size() > 0) {
      // TODO: handle case where there is more than one announce, the old ones should
      // be closed
      announce = announces.get(0);
    } else if (!"stopped".equals(event)) {
      announce = new TorrentAnnounce();
      announce.setPeerId(peer_id);
      announce.setApplication(app);
      announce.setInfoHash(info_hash);
      announce.setAnounceDate(Instant.now());
      announce.setWallet(wallet);
    } else {
      if (outputJson) {
        result = "{\"error\":\"cannot find announce to close\"}";
      } else {
        result = "d14:failure reason30:cannot find announce to close.e";
      }
      throw new BusinessException(result);
    }
    announce.setLastAnnounceDate(Instant.now());
    announce.setIp(req.getRemoteAddr());
    try {
      if (!Strings.isEmpty(port)) {
        announce.setPort(Long.parseLong(port));
      }
      if (!Strings.isEmpty(downloaded)) {
        announce.setDownloaded(Long.parseLong(downloaded));
      }
      if (!Strings.isEmpty(uploaded)) {
        announce.setUploaded(Long.parseLong(uploaded));
      }
      if (!Strings.isEmpty(left)) {
        announce.setLeft(Long.parseLong(left));
      }
      if (!Strings.isEmpty(liveness)) {
        announce.setLiveness(Double.parseDouble(liveness));
      }
      if (!Strings.isEmpty(latitude)) {
        announce.setLatitude(Double.parseDouble(latitude));
      }
      if (!Strings.isEmpty(longitude)) {
        announce.setLongitude(Double.parseDouble(longitude));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    announce.setUrl(url);
    if ("stopped".equalsIgnoreCase(event)) {
      announce.setStatus("CLOSED");
    } else {
      announce.setStatus("ONGOING");
    }
    try {
      log.info("cet code = {}", announce.getClass().getSimpleName());
      // CustomEntityInstance cei = CEIUtils.pojoToCei(announce);
      TorrentAnnounce pojo = announce;
      Map<String, Object> pojoAsMap;
      if (pojo instanceof Map) {
        pojoAsMap = (Map<String, Object>) pojo;
      } else {
        // Transform POJO into Map
        Map<String, Object> values = new HashMap<>();
        Stream.of(pojo.getClass().getMethods())
            .filter(m -> m.getName().startsWith("get") | m.getName().startsWith("is"))
            .filter(m -> m.getParameterCount() == 0).forEach(m -> {
              var key = getFieldForGetter(pojo.getClass(), m);
              try {
                if (key != null) {
                  var value = m.invoke(pojo);
                  values.put(key, value);
                }
              } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
              }
            });

        pojoAsMap = new HashMap<>();
        values.entrySet().stream().forEach(e -> {
          if (e.getValue() != null && e.getValue().getClass().getAnnotation(Entity.class) != null) {
            pojoAsMap.put(e.getKey(), getIdValue(e.getValue()));
          } else {
            pojoAsMap.put(e.getKey(), e.getValue());
          }
        });
      }

      log.info("pojoAsMap = {}", pojoAsMap);
      // JacksonUtil.convert(pojo, GenericTypeReferences.MAP_STRING_OBJECT);
      CustomEntityInstance cei = new CustomEntityInstance();
      cei.setUuid((String) pojoAsMap.get("uuid"));
      cei.setCetCode(pojo.getClass().getSimpleName());
      CustomFieldValues customFieldValues = new CustomFieldValues();
      pojoAsMap.forEach(customFieldValues::setValue);
      cei.setCfValues(customFieldValues);

      log.info("announceCEI = {}", cei);
      String uuid = crossStorageApi.createOrUpdate(defaultRepo, cei);
      log.info("announce instance {} created / updated", uuid);

      try {
        if (outputJson) {
          result = "{\"error\":\"cannot find peers\"}";
        } else {
          result = "d14:failure reason18:cannot find peers.e";
        }
        CrossStorageRequest<TorrentAnnounce> csreqlist =crossStorageApi.find(defaultRepo, TorrentAnnounce.class).by("status", "ONGOING")
        .by("application", app);
        if(cat!=null){
          csreqlist.by("infoHash", info_hash);
        }
        List<TorrentAnnounce> peers = csreqlist.getResults();
        if (peers.size() > 0) {
          if (outputJson) {
            result = "{\"interval\":900,\n\"peers\":[";
          } else {
            result = "d8:intervali900e5:peersl";
          }
          String sep = "";
          for (TorrentAnnounce peer : peers) {
            if(!peer.getPeerId().equals(peer_id)){
              if (outputJson) {
                result += sep + "\n";
                sep = ",";
                result += "{\"peer_id\":\"" + peer.getPeerId() + "\",\n";
                result += " \"ip\":\"" + peer.getIp() + "\",\n";
                result += " \"port\":" + peer.getPort() + ",\n";
                result += " \"uploaded\":" + peer.getUploaded() + ",\n";
                result += " \"latitude\":" + peer.getLatitude() + ",\n";
                result += " \"longitude\":" + peer.getLongitude() + ",\n";
                result += " \"liveness\":" + peer.getLiveness() + "}";
              } else {
                String peerid = unHex(peer.getPeerId());
                result += "d7:peer_id" + peerid.length() + ":" + peerid;
                result += "2:ip" + peer.getIp().length() + ":" + peer.getIp();
                result += "4:port" + (peer.getPort() + "").length() + ":i" + peer.getPort() + "ee";
              }
            }
          }
          if (outputJson) {
            result += "]}";
          } else {
            result += "e";
          }
        }
      } catch (Exception e) {
        throw new BusinessException(result);
      }
    } catch (Exception ex) {
      throw new BusinessException(ex);
    }
  }

}