package org.gobuki.net.ssl;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.security.cert.Certificate;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class SSLClient {

    BufferedReader in = null;
    PrintWriter out = null;

    public SSLClient() {

    }

    public void connectToServer(String serverAddress, int serverPort) {
        SSLSocketFactory sslSocketFactory =
                (SSLSocketFactory)SSLSocketFactory.getDefault();
        try {
            SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(serverAddress, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            printCertificates(socket);
            send(out,"Hi Server! How are you doing?");

            String responseLine;
            waitForServerInput: while ((responseLine = in.readLine()) != null) {

                System.out.println("Server: " + responseLine + "'");
                try {
                    TimeUnit.MILLISECONDS.sleep(3000);
                } catch (InterruptedException e) {
                    break waitForServerInput;
                }

                if (responseLine.contains("bored")) {
                    send(out, "Oh, excuse the interruption. Byeee!");
                    break waitForServerInput;
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public void send(PrintWriter out, String command) {
        out.println(command);
        System.out.println("Client: " + command);
    }

    public void printCertificates(SSLSocket sslSocket) {
        try {
            Certificate[] serverCerts = sslSocket.getSession().getPeerCertificates();

            System.out.println("Server Certificate chain contains "  + serverCerts.length + " certifcate(s)\n");
            for (int i = 0; i < serverCerts.length; i++) {
                System.out.println("Certificate " + (i + 1));
                System.out.println("Public Key:\n" + serverCerts[i].getPublicKey());
                System.out.println("Certificate Type:\n " + serverCerts[i].getType());
            }
        } catch (SSLPeerUnverifiedException e) {
            System.err.println("Error while dumping certificates : " + e.toString());
        }
    }

    public static void main(String[] args) {

        //System.setProperty("javax.net.debug", "all");
        System.setProperty("javax.net.ssl.keyStore", "sslclientkeys.p12");
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", "sslclienttrust.p12");
        System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");

        SSLClient client = new SSLClient();
        client.connectToServer("localhost", 1162);
    }
}