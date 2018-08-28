package com.valashko.xaapi.channel;

import java.io.IOException;
import java.net.*;

public class IncomingMulticastChannel {

    private static final int SOCKET_TIMEOUT = 1000; // milliseconds

    private int port;
    private MulticastSocket socket;

    public IncomingMulticastChannel(String group, int localPort) throws IOException {
        this.port = localPort;
        this.socket = new MulticastSocket(this.port);
        this.socket.setSoTimeout(SOCKET_TIMEOUT);
        // on OS X make sure to run with -Djava.net.preferIPv4Stack=true
        this.socket.joinGroup(InetAddress.getByName(group));
    }

    public byte[] receive() throws IOException {
        byte buffer[] = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        int responseLength = packet.getLength();
        byte response[] = new byte[responseLength];
        System.arraycopy(buffer, 0, response, 0, responseLength);
        return response;
    }
}
