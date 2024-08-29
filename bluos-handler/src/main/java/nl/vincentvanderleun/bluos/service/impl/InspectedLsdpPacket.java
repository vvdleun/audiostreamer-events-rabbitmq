package nl.vincentvanderleun.bluos.service.impl;

import nl.vincentvanderleun.bluos.model.LsdpMessageType;

public record InspectedLsdpPacket(
    // Could the inspected packet be parsed and validated as a valid LSDP v1 message?
    boolean valid,
    // Is the message a known type?
    LsdpMessageType messageType,
    // Raw signed byes of the header portion, extracted from the packet
    byte[] rawHeader,
    // Raw signed bytes of the message portion, extracted from the packet
    byte[] rawMessage
) {
    
}
