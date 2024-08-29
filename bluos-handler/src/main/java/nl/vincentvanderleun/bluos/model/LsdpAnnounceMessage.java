package nl.vincentvanderleun.bluos.model;

import java.util.List;
import java.util.Map;

public record LsdpAnnounceMessage(
    int nodeIdLength,
    String nodeId,
    int addressLength,
    String address,
    int countAnnounceRecords,
    List<LsdpAnnounceRecord> announcementRecords) {
}
