[![Build with Java 17](https://github.com/i-love-coffee-i-love-tea/JavaTLSOnlySocket/actions/workflows/java17.yml/badge.svg?branch=master)](https://github.com/i-love-coffee-i-love-tea/JavaTLSOnlySocket/actions/workflows/java17.yml)
[![Build with Java 21](https://github.com/i-love-coffee-i-love-tea/JavaTLSOnlySocket/actions/workflows/java21.yml/badge.svg?branch=master)](https://github.com/i-love-coffee-i-love-tea/JavaTLSOnlySocket/actions/workflows/java21.yml)

# JavaTLSOnlySocket

Example of an SSL TCP service and client with mutual SSL authentication.
Makes sure to connect using TLSv1.2 only.

```
 .------------------------.
 |       SSLCLient        | 
 *------------------------*
             |
         tcp/1162
             |
 .------------------------.
 |       SSLServer        |   <--- waiting for someone to say hi
 *------------------------*
```

When run the client will connect to the server and they will have a short chat, then the client exits.
The server exits after handling one connection.


## Goals 
 - Demonstrate how to create a secure SSL TCP connection 
 - Demonstrate certificate managemnt for client authentication using key tool


## Compiling

$ mvn package

## Manage certificates 

We will use the keytool to create SSL keys and for certificates for both server and clients.

The keys are wrapped in openssl compatible PKCS12 keystores.

For each client trust has to be setup by exporting and importing each others certificates into their peers keystore.

You can run the create-keystores.sh script to initialize example keystores and skip the next topics "Create the server key- and truststore" and
"Client key- and truststore setup" for testing.  
``` 
cd scripts
./create-keystores.sh
```

### Server key- and truststore setup

**Create server keystore and keys**
```
keytool -genkey -alias sslserver -keystore sslserverkeys.p12 -storetype PKCS12 -storepass $PASS
```
When the keytool asks for your name "What is your first and last name?", you have to enter the hostname of the server.
You can press enter for all other questions, for testing purposes.


**Export server certificate**
```
keytool -export -alias sslserver -keystore sslserverkeys.p12 -file sslserver.cer -storetype PKCS12 -storepass $PASS
```

### Client key- and truststore setup

The alias is just the name under which the keys will be stored. You can choose it freely, it only has to be unique.
It is good practice to use the client name, something, you remeber or can look up and associate with this client. 


**Create client keystore and keys**
```
keytool -genkey -alias sslclient -keystore sslclientkeys.p12 -storetype PKCS12 -storepass $PASS -keyalg RSA
```
When the keytool asks for your name "What is your first and last name?", you have to enter the hostname of the client.
You can press enter for all other questions, for testing purposes.

**Export client certificate**
```
keytool -export -alias sslclient -keystore sslclientkeys.p12 -file sslclient.cer -storetype PKCS12 -storepass $PASS
```

**Import client certificate into server truststore**
```
keytool -import -alias sslclient -keystore sslservertrust.p12 -file sslclient.cer -storetype PKCS12 -storepass $PASS
```
Answer "yes" to make the client trust the server certificate.

**Import server certificate into client truststore**
```
keytool -import -alias sslserver -keystore sslclienttrust.p12 -file sslserver.cer -storetype PKCS12 -storepass $PASS
```
Answer "yes" to make the server trust the client certificate.


## Test the connection

1. Start the ssl server

    ```
    $ cd scripts  
    $ ./tls-server.sh
    ```
    
2. In a different console, start the client and enter a string

    ```
    $ cd scripts      
    $ ./tls-client.sh
    ```
 
    Enter a string and press enter to send it to the server. It should echo it.



If the programs are run without arguments the server listens for client connections on localhost port 1162 by default
and the client by default connects to the same socket.

They both take a hostname or ip as first parameter and a port number as second parameter to change this behaviour. 


## Links

Talk: Java's SSLSocket How Bad APIs Compromise Security
https://www.youtube.com/watch?v=LaGG6dtDHpk
