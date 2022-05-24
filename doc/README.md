# Liquichain module

## Messaging

In a network of constrained user devices nodes often go offline and when online they only have few connection opened to other nodes. This is very different from network topology like the one used in IPFS or ethswarm that assume a quasi-stable set of online nodes each having hundreds of open connections.
In this context simple Kademlia routing is not effective and a better realtime network topology understanding is needed.
Instead of using telecel server to store all messaging in files, the server is used to monitor the online presence of users and offer help in routing.
When a user connects with the mobile app, it will announce itself to the telecel server.
This announce is an extension of the bittorrent tracker request protocol. It declares which topics the user is interested in.
A topic is any kind of resource that a group of user is using to communicate, it can be the mobile application itself, a chat channel, a group buying, etc… It is represented by a 20 bytes hash that is used as an info_hash in the announce request.
In case a node want to directly send a message to another node it will send an announce using a topic hash equal to the wallet address of the target node.
The server will respond to the announce with a list of nodes that are most adapted for routing.

Network awareness :
Let assume we have a network of 5 devices A,B,C,D,E with the following configuration

A – B
|     |
C – D – E

Namely A is connected to B and C, B is connected to A and D , D is connected to C,B and E.
Let suppose A connects first, then it will send an announce to the infohash X of the mobile app.
When B comes online it is aware that A is online and opens a connection to it, it then send a new announce with the info_hash equal to the address of A; the server is then aware that there is an active connection between A and B.
When C comes online It sees that A is online, establish a connection to it and notify the server. When D comes it connect to C and B then when E comes it connects to E
Messaging :


User A want to send a message to E. he will send an announce to the server using the address of E.
The server deduces from the current topology that there exist 2 paths to go from A to E, namely A-B-D-E and A-C-D-E. It then returns the address of B and C
A send the message to B, to be forwarded to E. B then send an announce to the server with the address E and get D as a response. B send the message to D, to be forwarded to E. 
Improvement 1: 
-	Instead of returning to A the address of B and C, the server could directly return the paths B-D-E and C-D-E. A could then send to B not only the target node E but also the path to follow : D-E, avoiding both B and D a 
Improvement 2:
-	In case A can make new connections, he could try to establish a new connection with E then D if not possible with E.


