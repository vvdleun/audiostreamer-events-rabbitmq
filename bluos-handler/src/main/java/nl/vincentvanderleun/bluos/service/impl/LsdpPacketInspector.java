package nl.vincentvanderleun.bluos.service.impl;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

import nl.vincentvanderleun.bluos.model.LsdpMessageType;

/**
 * Thread-safe low-level LSDP packet inspector.
 * It validates the given packet and tries to determine what kind of LSDP v1 message the packet contains. 
 */
public class LsdpPacketInspector {
    private static final int EXPECTED_HEADER_SIZE = 6;  // Header length (1 byte) + Magic word (4 bytes) + Protocol version (1 byte)
    private static final int SUPPORTED_VERSION = 1;     // Only support LSDP protocol v1 packets
    private static final byte[] MAGIC_WORD = "LSDP".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    private static final int RAW_TYPE_QUERY_FOR_BROADCAST_RESPONSE = 'Q';
    private static final int RAW_TYPE_QUERY_FOR_UNICAST_RESPONSE = 'R';
    private static final int RAW_TYPE_ANNOUNCEMENT = 'A';
    private static final int RAW_TYPE_DELETE = 'D';

    public InspectedLsdpPacket inspectPacket(byte[] packet) {
        try {
            return inspectPacket(new ByteArrayInputStream(packet), packet);
        } catch(IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private InspectedLsdpPacket inspectPacket(ByteArrayInputStream byteArrayInputStream, byte[] rawPacketBytes) throws IOException {
        var dataInputStream = new DataInputStream(byteArrayInputStream);

        // Initialize result values to unknown packet type scenario
        boolean valid = false;
        LsdpMessageType messageType = LsdpMessageType.UNKNOWN;
        byte[] rawHeader = null;
        byte[] rawMessage = null;

        if(rawPacketBytes.length < EXPECTED_HEADER_SIZE) {
            return new InspectedLsdpPacket(valid, messageType, rawHeader, rawMessage);
        }

        // Parse header, the packet should at least contain those particular bytes
        int headerLength = dataInputStream.readUnsignedByte();
        byte[] magicWordBytes = dataInputStream.readNBytes(MAGIC_WORD.length);
        int protocolVersion = dataInputStream.readUnsignedByte();

        // See if we can support this packet
        valid = Arrays.equals(magicWordBytes, MAGIC_WORD) && protocolVersion == SUPPORTED_VERSION;
        if(!valid) {
            return new InspectedLsdpPacket(valid, messageType, rawHeader, rawMessage);
        }

        // All documented version 1 messages start with a message length byte

        if(rawPacketBytes.length < EXPECTED_HEADER_SIZE + 1) {
            return new InspectedLsdpPacket(valid, messageType, rawHeader, rawMessage);
        }

        // Followed by a message type byte

        int messageLength = dataInputStream.readUnsignedByte();
        if(messageLength == 0 || rawPacketBytes.length < EXPECTED_HEADER_SIZE + messageLength) {
            return new InspectedLsdpPacket(valid, messageType, rawHeader, rawMessage);
        }

        // Determine message type (hardcoded for now)
        // TODO delegate this logic to a converter

        valid = true;
        int messageTypeByte = dataInputStream.readUnsignedByte();
        switch(messageTypeByte) {
            case RAW_TYPE_ANNOUNCEMENT:
                messageType = LsdpMessageType.ANNOUNCE_MESSAGE;
                break;
            case RAW_TYPE_QUERY_FOR_BROADCAST_RESPONSE:
                messageType = LsdpMessageType.QUERY_FOR_BROADCAST_RESPONSE_MESSAGE;
                break;
            case RAW_TYPE_QUERY_FOR_UNICAST_RESPONSE:
                messageType = LsdpMessageType.QUERY_FOR_UNICAST_RESPONSE_MESSAGE;
                break;
            case RAW_TYPE_DELETE:
                messageType = LsdpMessageType.DELETE_MESSAGE;
                break;
        }

        rawHeader = Arrays.copyOfRange(rawPacketBytes, 0, EXPECTED_HEADER_SIZE);
        rawMessage = Arrays.copyOfRange(rawPacketBytes, EXPECTED_HEADER_SIZE, EXPECTED_HEADER_SIZE + messageLength);

        return new InspectedLsdpPacket(valid, messageType, rawHeader, rawMessage);
    }
}
