package nl.vincentvanderleun.bluos.model;

import java.util.Map;

public record LsdpAnnounceRecord(
    BluOsDeviceClasses classIdentifier,
    int valuesCount,
    Map<String, String> values
) {
    
}
