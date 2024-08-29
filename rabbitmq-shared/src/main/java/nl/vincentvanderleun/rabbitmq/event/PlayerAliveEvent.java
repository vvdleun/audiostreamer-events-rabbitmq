package nl.vincentvanderleun.rabbitmq.event;

import nl.vincentvanderleun.player.model.Player;

public record PlayerAliveEvent(
    // Player that is 'alive'
    Player player,
    // If "true", it was concluded the player was not alive at the previous check
    boolean firstSeen
) {
    
}
