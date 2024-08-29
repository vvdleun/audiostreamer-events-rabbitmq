package nl.vincentvanderleun.player.model;

import java.util.Map;

public record Player(
    String id,
    String name,
    String platformId,
    String ip,
    Map<String, String> attributes
) {
}
