# liquichain module

## Websocket endpoint

A [websocket endpoint](https://github.com/meveo-org/meveo/tree/master/meveo-admin/ejbs/src/main/java/org/meveo/service/technicalservice/wsendpoint) allow clients to
register themself and send messages to other users.

Its address is `ws://dev.telecelplay.io/meveo/ws/liquichain`

### Registration

message :
```
{
"action":"register",
"account":"<wallet address of the client>"
}
```
this message bind the open websocket to the wallet address of the client

if some pending message are queued by the server for this wallet address, they are sent to the client

### Send message

WebSocket endpoint: ws://localhost:8080/meveo/ws/liquichain

smaple message:
```
{
    "message":"Hello! How are you?",
    "to":"<wallet address of the client>",
    "action":"message"
}
```
