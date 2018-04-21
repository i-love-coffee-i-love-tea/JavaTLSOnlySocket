#!/bin/sh

#java  -Djavax.net.ssl.keyStore=keystore.p12 -Djavax.net.ssl.keyStorePassword=changeit -Djavax.net.ssl.keyStoreType=PKCS12 -jar ../trap-relay-daemon-ssl/target/trap-relay-daemon-ssl-1.0-SNAPSHOT-jar-with-dependencies.jar
java -jar ../tls-server/target/tls-server-1.0-SNAPSHOT-jar-with-dependencies.jar

