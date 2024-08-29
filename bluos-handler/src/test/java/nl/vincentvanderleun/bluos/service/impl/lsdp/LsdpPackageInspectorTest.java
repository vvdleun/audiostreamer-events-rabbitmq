package nl.vincentvanderleun.bluos.service.impl.lsdp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import nl.vincentvanderleun.bluos.model.lsdp.LsdpMessageType;

public class LsdpPackageInspectorTest {
    private final LsdpPacketInspector inspector = new LsdpPacketInspector();

    @ParameterizedTest
    @MethodSource("getProperMessageTypeByteAndExpectedMessageTypePairs")
    void shouldValidatePacket(byte messageTypeByte, LsdpMessageType expectedMessageType) {
        // TODO create packet builder for testing purposes
        byte[] packet = { 
            6,  // 0:  Length header
            76, // 1:  "L"
            83, // 2:  "S"
            68, // 3:  "D"
            80, // 4:  "P",
            1,  // 5:  version
            5,  // 6:  Message length
            0,  // 7:  <messageTypeByte>,
            1,  // 8:  Bogus message data 1 
            2,  // 9:  Bogus message data 2
            3   // 10: Bogus message data 3
        };
        packet[7] = messageTypeByte;

        InspectedLsdpPacket actual = inspector.inspectPacket(packet);

        byte[] expectedHeader = {
            6,  // 0: Length header
            76, // 1: "L"
            83, // 2: "S"
            68, // 3: "D"
            80, // 4: "P",
            1   // 5: version
        };

        byte[] expectedMessage = {
            5,  // 0: Message length
            0,  // 1: <messageTypeByte>
            1,  // 2: Bogus message data 1 
            2,  // 3: Bogus message data 2
            3   // 4: Bogus message data 3
        };
        expectedMessage[1] = messageTypeByte;

        assertTrue(actual.valid());
        assertEquals(expectedMessageType, actual.messageType());
        assertArrayEquals(expectedHeader, actual.rawHeader());
        assertArrayEquals(expectedMessage, actual.rawMessage());
    }

    private static Stream<Arguments> getProperMessageTypeByteAndExpectedMessageTypePairs() {
        return Stream.of(
            // Recognized message types

            // "Q"
            Arguments.of((byte)81, LsdpMessageType.QUERY_FOR_BROADCAST_RESPONSE_MESSAGE),
            // "R"
            Arguments.of((byte)82, LsdpMessageType.QUERY_FOR_UNICAST_RESPONSE_MESSAGE),
            // "A"
            Arguments.of((byte)65, LsdpMessageType.ANNOUNCE_MESSAGE),
            // "D"
            Arguments.of((byte)68, LsdpMessageType.DELETE_MESSAGE),

            // Unrecognized message types

            // "q" (lowercase, non-existing)
            Arguments.of((byte)113, LsdpMessageType.UNKNOWN),
            // 0
            Arguments.of((byte)0, LsdpMessageType.UNKNOWN)
        );
    }

    @Test
    void shouldValidatePacketThatHasLargerHeader() {
        byte[] packet = { 
            8,  // 0:  Length header
            76, // 1:  "L"
            83, // 2:  "S"
            68, // 3:  "D"
            80, // 4:  "P",
            1,  // 5:  version,
            13, // 6:  excessive header byte 1
            37, // 7:  excessive header byte 2
            5,  // 8:  Message length
            81, // 9:  "Q",
            1,  // 10: Bogus message data 1 
            2,  // 11: Bogus message data 2
            3   // 12: Bogus message data 3
        };

        InspectedLsdpPacket actual = inspector.inspectPacket(packet);

        byte[] expectedHeader = {
            8,  // 0: Length header
            76, // 1: "L"
            83, // 2: "S"
            68, // 3: "D"
            80, // 4: "P",
            1,  // 5: version,
            13, // 6: excessive header byte 1
            37  // 7: excessive header byte 2
        };

        byte[] expectedMessage = {
            5,  // 0: Message length
            81, // 1: "Q"
            1,  // 2: Bogus message data 1 
            2,  // 3: Bogus message data 2
            3   // 4: Bogus message data 3
        };

        assertTrue(actual.valid());
        assertEquals(LsdpMessageType.QUERY_FOR_BROADCAST_RESPONSE_MESSAGE, actual.messageType());
        assertArrayEquals(expectedHeader, actual.rawHeader());
        assertArrayEquals(expectedMessage, actual.rawMessage());
    }

    @Test
    void shouldNotValidatePacketBecauseHeaderIsTooSmall() {
        byte[] packet = { 
            5,  // 0: Length header
            76, // 1: "L"
            83, // 2: "S"
            68, // 3: "D"
            80, // 4: "P",
            // No version number byte!
            5,  // 5: Message length
            81, // 6: "Q",
            1,  // 7: Bogus message data 1 
            2,  // 8: Bogus message data 2
            3   // 9: Bogus message data 3
        };

        InspectedLsdpPacket actual = inspector.inspectPacket(packet);

        assertFalse(actual.valid());
        assertEquals(LsdpMessageType.UNKNOWN, actual.messageType());
        assertNull(actual.rawHeader());
        assertNull(actual.rawMessage());
    }

    @Test
    void shouldNotValidatePacketBecauseThereIsNoMessage() {
        byte[] packet = { 
            6,  // 0: Length header
            76, // 1: "L"
            83, // 2: "S"
            68, // 3: "D"
            80, // 4: "P",
            1,  // 5: version
            0,  // 6: Message length
            1,  // 7: Bogus data 1 
            2,  // 8: Bogus data 2
            3   // 9: Bogus data 3
        };

        InspectedLsdpPacket actual = inspector.inspectPacket(packet);

        assertFalse(actual.valid());
        assertEquals(LsdpMessageType.UNKNOWN, actual.messageType());
        assertNull(actual.rawHeader());
        assertNull(actual.rawMessage());
    }

    @Test
    void shouldNotValidatePacketBecauseThereIsNoMessagePart() {
        byte[] packet = { 
            6,  // 0: Length header
            76, // 1: "L"
            83, // 2: "S"
            68, // 3: "D"
            80, // 4: "P",
            1,  // 5: Version
        };

        InspectedLsdpPacket actual = inspector.inspectPacket(packet);

        assertFalse(actual.valid());
        assertEquals(LsdpMessageType.UNKNOWN, actual.messageType());
        assertNull(actual.rawHeader());
        assertNull(actual.rawMessage());
    }

    @Test
    void shouldNotProcessGarbageData() {
        byte[] packet = { 0 };

        InspectedLsdpPacket actual = inspector.inspectPacket(packet);

        assertFalse(actual.valid());
        assertEquals(LsdpMessageType.UNKNOWN, actual.messageType());
        assertNull(actual.rawHeader());
        assertNull(actual.rawMessage());
    }
}
