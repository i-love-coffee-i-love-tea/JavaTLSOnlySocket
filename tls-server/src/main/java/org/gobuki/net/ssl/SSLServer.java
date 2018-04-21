package org.gobuki.net.ssl;

import java.io.*;
import java.net.ProtocolException;
import java.net.SocketOption;
import java.security.cert.Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 * https://stackoverflow.com/questions/3712366/choosing-ssl-client-certificate-in-java?noredirect=1&lq=1
 *
 * Creating a Client Certificate for Mutual Authentication
 * https://docs.oracle.com/cd/E19575-01/819-3669/bnbyi/index.html
 *
 */
public class SSLServer {

    public static final String REQUIRED_ENCRYPTION_PROTOCOL = "TLSv1.2";

    public enum SupportedClientCommands {
        HI, BYE;
    }

    SSLServerSocket sslServerSocket;

    public SSLServer() {

    }

    /**
     * Checks if a protocol is supported. If yes, it enables only this protocol for the sslServerSocket,
     * throws an exception otherwise.
     *
     * @param protocol
     * @throws ProtocolException if the protocol isn't found in the list of supported protocols and thus can not be enabled
     */
    private void requireProtocol(String protocol) throws ProtocolException {
        System.out.println("Server supports these encryption protocols:");
        boolean protocolIsSupported = false;
        for (String supportedProtocol : sslServerSocket.getSupportedProtocols()) {
            System.out.println("\t" + supportedProtocol);
            if (supportedProtocol.equalsIgnoreCase(protocol)) {
                protocolIsSupported = true;
                break;
            }
        }
        if (protocolIsSupported) {
            sslServerSocket.setEnabledProtocols(new String[]{protocol});
            System.out.println("Set '" + protocol + "' as only supported protocol.");
        } else {
            throw new ProtocolException("Required protocol not supported: " + protocol);
        }
    }

    public void startListening(int port) {

        SSLServerSocketFactory sslServerSocketFactory =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        dumpSupportedServerCiphers(sslServerSocketFactory);

        try {
            sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
            sslServerSocket.setNeedClientAuth(true);
            requireProtocol(REQUIRED_ENCRYPTION_PROTOCOL);

            SSLSocket socket = (SSLSocket) sslServerSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            //dumpSupportedSocketOptions(socket); throws NPE at getChannel()
            System.out.println("Peer principal: " + socket.getSession().getPeerPrincipal());
            System.out.println("Socket timeout: " + socket.getSoTimeout());

            printClientCertificateChain(socket);

            String clientInput;
            waitForClientInput: while ((clientInput = in.readLine()) != null) {
                try {
                    TimeUnit.MILLISECONDS.sleep(3000);
                } catch (InterruptedException e) {
                    break waitForClientInput;
                }

                if (clientInput.toUpperCase().contains(SupportedClientCommands.HI.name())) {
                    out.println("Heeey Client, my friend! Nice to see you. I'm quite bored, nobody is asking me anything.");
                } else if (clientInput.toUpperCase().contains(SupportedClientCommands.BYE.name())) {
                    break waitForClientInput;
                }
            }
            System.out.println("Connection closed");
        } catch (ProtocolException e) {
            System.err.println(e.getMessage());
            System.err.println("exiting");
            System.exit(2);
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }

    public void dumpSupportedServerCiphers(SSLServerSocketFactory socketFactory) {
        System.out.println("Server supported cipher suites:");
        for (String suite : socketFactory.getSupportedCipherSuites()) {
            System.out.println("\t" + suite);
        }
    }

    public void dumpSupportedSocketOptions(SSLSocket socket) {
        System.out.println("Supported socket options:");
        for (SocketOption socketOption : socket.getChannel().supportedOptions()) {
            System.out.println("\t" + socketOption.name() + ", type: " + socketOption.type());
        }
    }

    public void printClientCertificateChain(SSLSocket sslSocket) {
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
        System.setProperty("javax.net.ssl.keyStore", "sslserverkeys.p12");
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", "sslservertrust.p12");
        System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");

        SSLServer server = new SSLServer();
        server.startListening(1162);
    }
}
