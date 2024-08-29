package nl.vincentvanderleun.bluos.model.lsdp;

import java.util.List;

public record LsdpAnnounceMessage(
    int nodeIdLength,
    String nodeId,
    int addressLength,
    String address,
    int countAnnounceRecords,
    List<LsdpAnnounceRecord> announcementRecords) {
}
