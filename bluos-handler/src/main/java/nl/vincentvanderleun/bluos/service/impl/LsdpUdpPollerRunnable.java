package nl.vincentvanderleun.bluos.service.impl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

import nl.vincentvanderleun.bluos.model.LsdpMessageType;

public class LsdpUdpPollerRunnable implements Runnable {
    private static final int BUFFER_SIZE = 1024 * 256;  // 256 kilobytes
    private static final int ERROR_DELAY = 5000;        // 5 seconds

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
        // Infinite loop 1: initialize socket and start polling loop
        try {
            while(running) {
                System.out.println("Setting up datagram socket...");
                DatagramSocket socket = new DatagramSocket(LsdpConstants.PORT);
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                // Infinite loop #2: process UDP broadcasts
                System.out.println("Start polling...");
                while(running) {
                    System.out.println("Waiting for LSDP UDP broadcasts...");
                    try {
                        socket.receive(packet);

                        var inspectedPacket = packetInspector.inspectPacket(packet.getData());

                        handleInspectedPacket(inspectedPacket);

                        // Reset packet length to full buffers size
                        packet.setLength(buffer.length);
                    } catch(Exception ex) {
                        System.out.println("Exception while waiting for incoming UDP traffic: running=" + running);
                        ex.printStackTrace();
                        sleep(ERROR_DELAY);
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
            case LsdpMessageType.ANNOUNCE_MESSAGE:
                var msg = announcementMessageParser.parse(inspectedPacket);
                System.out.println(msg);
                break;
            default:
                System.out.println("Ignored message: " + inspectedPacket.toString());
        }
    }

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
