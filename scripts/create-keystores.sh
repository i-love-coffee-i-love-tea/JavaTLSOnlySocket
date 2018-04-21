#!/bin/sh

PASS=password

# Creating a client certificate for mutual authentication
# https://docs.oracle.com/cd/E19575-01/819-3669/bnbyi/index.html

# Create client keystore and keys
keytool -genkey -alias sslclient -keystore sslclientkeys.p12 -storetype PKCS12 -storepass $PASS -keyalg RSA

# Export client certificate
keytool -export -alias sslclient -keystore sslclientkeys.p12 -file sslclient.cer -storetype PKCS12 -storepass $PASS

# Create server keystore and keys
keytool -genkey -alias sslserver -keystore sslserverkeys.p12 -storetype PKCS12 -storepass $PASS

# Export server certificate
keytool -export -alias sslserver -keystore sslserverkeys.p12 -file sslserver.cer -storetype PKCS12 -storepass $PASS

# Import client certificate into server truststore
keytool -import -alias sslclient -keystore sslservertrust.p12 -file sslclient.cer -storetype PKCS12 -storepass $PASS

# Import server certificate into client truststore
keytool -import -alias sslserver -keystore sslclienttrust.p12 -file sslserver.cer -storetype PKCS12 -storepass $PASS

# To view your keystore or trust
#keytool -list -keystore sslclienttrust

