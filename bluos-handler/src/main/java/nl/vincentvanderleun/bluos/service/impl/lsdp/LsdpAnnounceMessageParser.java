package nl.vincentvanderleun.bluos.service.impl.lsdp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import nl.vincentvanderleun.bluos.model.lsdp.BluOsDeviceClasses;
import nl.vincentvanderleun.bluos.model.lsdp.LsdpAnnounceMessage;
import nl.vincentvanderleun.bluos.model.lsdp.LsdpAnnounceRecord;
import nl.vincentvanderleun.bluos.model.lsdp.LsdpMessageType;

public class LsdpAnnounceMessageParser {
    
    public LsdpAnnounceMessage parse(InspectedLsdpPacket packet) {
        try{
            if(packet.messageType() != LsdpMessageType.ANNOUNCE_MESSAGE) {
                throw new IllegalStateException("Not an announcement packet: " + packet.messageType());
            }
            
            var byteArrayInputStream = new ByteArrayInputStream(packet.rawMessage());

            return parse(byteArrayInputStream);            
        } catch(IOException ex) {
            throw new RuntimeException(ex);
        }    
    }

    private LsdpAnnounceMessage parse(ByteArrayInputStream byteArrayInputStream) throws IOException {
        var dataInputStream = new DataInputStream(byteArrayInputStream);

        // FIXME ignored for now
        int length = dataInputStream.readUnsignedByte();
        int messageType = dataInputStream.readUnsignedByte();
        
        int nodeIdLength = dataInputStream.readUnsignedByte();

        // Format node ID as a MAC-address ("00:0A:0B:CC...")
        int[] nodeIdUnsignedBytes = readUnsignedBytes(dataInputStream, nodeIdLength);
        String nodeId = Arrays.stream(nodeIdUnsignedBytes)
                .mapToObj(Integer::toHexString)
                .map(s -> 1 == s.length() ? "0" + s : s)
                .map(String::toUpperCase)
                .collect(Collectors.joining(":"));

        int addressLength = dataInputStream.readUnsignedByte();

        int[] addressUnsignedBytes = readUnsignedBytes(dataInputStream, addressLength);
        String address = Arrays.stream(addressUnsignedBytes)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining("."));

        int announcementRecordsCount = dataInputStream.readUnsignedByte();

        var announcementRecords = new ArrayList<LsdpAnnounceRecord>();
        for(int i = 0; i < announcementRecordsCount; i++) {
            LsdpAnnounceRecord announcementRecord = parseAnnouncementRecord(dataInputStream);
            announcementRecords.add(announcementRecord);
        }

        return new LsdpAnnounceMessage(
            nodeIdLength, nodeId, addressLength, address, announcementRecordsCount, announcementRecords);
    }

    private LsdpAnnounceRecord parseAnnouncementRecord(DataInputStream dataInputStream) throws IOException {
        // Big endian int
        int classIdUnsignedShort = dataInputStream.readUnsignedShort();
        BluOsDeviceClasses deviceIdClass = BluOsDeviceClasses.getClassDeviceFor(classIdUnsignedShort)
                .orElse(null);

        int textRecordsCount = dataInputStream.readUnsignedByte();

        var values = new HashMap<String, String>(textRecordsCount);
        for (var i = 0; i < textRecordsCount; i++) {
            int keyLength = dataInputStream.readUnsignedByte();
            String key = readAsciiCharacters(dataInputStream, keyLength);

            int valueLength = dataInputStream.readUnsignedByte();
            String value = readAsciiCharacters(dataInputStream, valueLength);

            values.put(key, value);
        }

        return new LsdpAnnounceRecord(deviceIdClass, textRecordsCount, values);
    }

    private String readAsciiCharacters(DataInputStream dataInputStream, int length) throws IOException {
        byte[] bytes = dataInputStream.readNBytes(length);
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    private int[] readUnsignedBytes(DataInputStream dataInputStream, int length) throws IOException {
        var result = new int[length];
        for(int i = 0; i < length; i++) {
            result[i] = dataInputStream.readUnsignedByte();
        }
        return result;
    }
}
