package nl.vincentvanderleun.bluos.service.impl.lsdp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import nl.vincentvanderleun.bluos.model.lsdp.LsdpMessageType;

/**
 * This implements the poller that looks for UDP broadcasts on the LSDP port
 * and handles each incoming message. It keeps state whether it is supposed
 * to keep running. By calling its stop() method, it will set the running flag
 * to false and (eventually) stop running.
 */
public class LsdpUdpPollerRunnable implements Runnable {
    private static final int BUFFER_SIZE = 1024 * 256;          // 256 kilobytes
    private static final int ERROR_DELAY = 5000;                // 5 seconds delay after an exception is thrown
    private static final int SOCKET_TIMEOUT = 1000 * 60 * 2;    // 2 minute socket timeout

    private volatile boolean running = false;

    private final LsdpPacketInspector packetInspector = new LsdpPacketInspector();
    private final LsdpAnnounceMessageParser announcementMessageParser = new LsdpAnnounceMessageParser();

    public LsdpUdpPollerRunnable() {
    }

    @Override
    public void run() {
        if(running) {
            throw new IllegalStateException("Worker is already running");
        }

        running = true;

        try {
            final byte[] buffer = new byte[BUFFER_SIZE];
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    
        // Infinite loop 1: initialize socket and start polling loop
        while(running) {
                System.out.println("Setting up datagram socket...");
                try(DatagramSocket socket = new DatagramSocket(LsdpConstants.PORT)) {
                    socket.setSoTimeout(SOCKET_TIMEOUT);
                    System.out.println("Start polling...");
                    // Infinite loop #2: poll and process UDP broadcasts
                    while(running) {
                        System.out.println("Waiting for LSDP UDP broadcasts...");
                        try {
                            socket.receive(packet);

                            var inspectedPacket = packetInspector.inspectPacket(packet.getData());

                            handleInspectedPacket(inspectedPacket);

                            // Reset packet length to full buffers size
                            packet.setLength(buffer.length);
                        } catch(SocketTimeoutException ex) {
                            System.out.println("No traffic detected in " + SOCKET_TIMEOUT + " milliseconds");
                        } catch(Exception ex) {
                            System.out.println("Exception while waiting for incoming UDP traffic: running=" + running);
                            ex.printStackTrace();
                            sleep(ERROR_DELAY);
                        }
                    }
                }
            }
            System.out.println("Worker stopped because running=false...");
        } catch(Exception ex) {
            System.out.println("Exception while setting up socket: running=" + running);
            ex.printStackTrace();
        }
        System.out.println("Bye...");
    }

    private void handleInspectedPacket(InspectedLsdpPacket inspectedPacket) {
        switch(inspectedPacket.messageType()) {
            // TODO implement handling of DELETE messages
            case LsdpMessageType.ANNOUNCE_MESSAGE:
                var msg = announcementMessageParser.parse(inspectedPacket);
                System.out.println(msg);
                break;
            default:
                System.out.println("Ignored message: " + inspectedPacket.toString());
        }
    }

    // TODO Move to util class
    private void sleep(int milliseconds) {
        try {
            System.out.println("Waiting for " + ERROR_DELAY + " milliseconds");
            Thread.sleep(milliseconds);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
