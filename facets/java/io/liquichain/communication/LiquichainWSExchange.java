package io.liquichain.communication;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.websocket.Session;

import org.meveo.admin.exception.BusinessException;
import org.meveo.service.script.Script;
import org.meveo.service.technicalservice.wsendpoint.WebsocketServerEndpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiquichainWSExchange extends Script {
    private static final Logger log = LoggerFactory.getLogger(LiquichainWSExchange.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final WebsocketServerEndpoint websocketServerEndpoint = getCDIBean(WebsocketServerEndpoint.class);
    //    private final ScriptInstanceService scriptInstanceService = getCDIBean(ScriptInstanceService.class);
    //    private final ScriptInterface messageCreatorScript = scriptInstanceService.getExecutionEngine(
    //            CreateMessageInConversation.class.getName(), null);
    private final CreateMessageInConversation messageCreator = new CreateMessageInConversation();

    private Session session;

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        log.debug("params:{}", parameters);
        session = (Session) parameters.get("WS_SESSION");
        String wsEvent = (String) parameters.get("WS_EVENT");
        log.debug("wsEvent:{}", wsEvent);
        switch (wsEvent) {
        case "open":
            onOpen(parameters);
            break;
        case "message":
            onMessage(parameters);
            break;
        }
    }

    public void onOpen(Map<String, Object> parameters) throws BusinessException {
        session = (Session) parameters.get("WS_SESSION");
        String userName = (String) session.getUserProperties().get("username");
        websocketServerEndpoint.consumeUserMessages(session, "liquichain_" + userName);
        log.debug("onOpen");
    }

    public void onMessage(Map<String, Object> parameters) throws BusinessException {
        String message = (String) parameters.get("WS_MESSAGE");
        Map<String, Object> map;
        try {
            map = mapper.readValue(message, Map.class);
        } catch (Exception e) {
            throw new BusinessException(e);
        }
        String action = (String) map.get("action");
        log.debug("action: {}", action);
        switch (action) {
        case ("register"):
            register(map, parameters);
            break;
        case ("message"):
            sendMessage(map, parameters);
            break;
        }
    }

    public void register(Map<String, Object> message, Map<String, Object> parameters) throws BusinessException {
        String account = (String) message.get("account");
        session.getUserProperties().put("username", account);
        websocketServerEndpoint.consumeUserMessages(session, "liquichain_" + account);

        //== send confirmation message back
        log.debug("sending registration confirmation message");
        sendConfirmationMessage(message, parameters);
    }

    private void sendConfirmationMessage(Map<String, Object> message, Map<String, Object> parameters)
            throws BusinessException {
        String account = (String) message.get("account");
        String confirmationMsg = "{ \"status\" : \"success\" , \"action\" : \"register\"}";
        message.put("message", confirmationMsg);
        message.put("to", account);
        message.put("persistMessage", false);
        sendMessage(message, parameters);
    }

    public void sendMessage(Map<String, Object> message, Map<String, Object> parameters) throws BusinessException {
        String destination = (String) message.get("to");
        String txtMessage = (String) message.get("message");

        Boolean shouldPersist = (Boolean) message.get("persistMessage");
        boolean persistMessage = shouldPersist == null || shouldPersist;
        if (persistMessage) {
            Pattern nonTextMsgRegex =
                    Pattern.compile("(\\{\\s*\"webrtc\"\\s*:\\s*true|\\{\\s*\"action\"\\s*:\\s*\"\\s*chat\\s*\")");
            persistMessage = !nonTextMsgRegex.matcher(txtMessage).lookingAt();
        }
        log.debug("sendMessage {} {}", destination, txtMessage);
        websocketServerEndpoint.sendMessage("liquichain", destination, txtMessage, persistMessage);

        if (message.get("conversationId") != null) {
            messageCreator.setSenderWalletId(session.getUserProperties().get("username").toString());
            messageCreator.setChatConversationId(message.get("conversationId").toString());
            messageCreator.setMessage(message.get("message").toString());
            messageCreator.execute(new HashMap<>());
        }

    }
}
