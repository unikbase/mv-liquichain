package io.liquichain.communication;

import java.util.Map;

import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.websocket.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.meveo.service.technicalservice.wsendpoint.WebsocketServerEndpoint;

import javax.enterprise.context.ApplicationScoped;

public class LiquichainWSExchange extends Script {
  
    private static final Logger log = LoggerFactory.getLogger(LiquichainWSExchange.class);
  
    private ObjectMapper mapper = new ObjectMapper();
  
    private Session session;
	
    private WebsocketServerEndpoint websocketServerEndpoint = getCDIBean(WebsocketServerEndpoint.class);;
	@Override
	public void execute(Map<String, Object> parameters) throws BusinessException {
	  log.info("params:{}",parameters);
      session = (Session) parameters.get("WS_SESSION");
      String wsEvent = (String) parameters.get("WS_EVENT");
	  log.info("wsEvent:{}",wsEvent);
      switch (wsEvent) {
        case "open" : onOpen(parameters);
          break;
        case "message" : onMessage(parameters);
          break;
      }
	}
  
    public void onOpen(Map<String, Object> parameters) throws BusinessException {
      log.info("onOpen");
    }
  
    public void onMessage(Map<String, Object> parameters) throws BusinessException {
      String message  = (String) parameters.get("WS_MESSAGE");
      Map<String, Object> map = null;
      try{
      	 map = mapper.readValue(message, Map.class);
      } catch(Exception e){
        throw new BusinessException(e);
      }
      String action = (String)map.get("action");
      log.info("action: {}",action);
      switch(action){
           case("register"):
                register(map,parameters);
                break;
           case("message"):
                sendMessage(map,parameters);
                break;
      }
    }
	
    public void register(Map<String, Object> message,Map<String, Object> parameters) throws BusinessException {
      String account = (String)message.get("account");
      //TODO: verify signature
      session.getUserProperties().put("username", account);
    }
  
  
    public void sendMessage(Map<String, Object> message,Map<String, Object> parameters) throws BusinessException {
      String destination = (String)message.get("to");
      String txtMessage = (String)message.get("message");
      //TODO: verify signature
      log.info("sendMessage {} {}",destination,txtMessage);
      websocketServerEndpoint.sendMessage("liquichain", destination, txtMessage);
    }
}