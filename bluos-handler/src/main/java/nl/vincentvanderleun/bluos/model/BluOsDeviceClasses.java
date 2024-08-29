package nl.vincentvanderleun.bluos.model;

import java.util.Arrays;
import java.util.Optional;

public enum BluOsDeviceClasses {
    // 16 bit value, big endian byte order: MSB, LSB
    BLUOS_PLAYER(0x00, 0x01),
    BLUOS_SERVER(0x00, 0x02),
    BLUOS_PLAYER_SECONDARY(0x00, 0x03),
    SOVI_MFG_RESERVED(0x00, 0x04),
    SOVI_KEYPAD(0x00, 0x05),
    BLUOS_PLAYER_PAIR_SLAVE(0x00, 0x06),
    REMOTE_WEB_APP(0x00, 0x07),
    BLUOS_HUB(0x00, 0x08),
    ALL_CLASSES(0xFF, 0xFF);

    public static Optional<BluOsDeviceClasses> getClassDeviceFor(int deviceIdClass) {
        return Arrays.stream(BluOsDeviceClasses.values())
            .filter(c -> deviceIdClass == c.deviceIdClass)
            .findFirst();
    }

    private BluOsDeviceClasses(int msb, int lsb) {
        this.deviceIdClass = toBigEndianUnsignedShort(msb, lsb);
    }

    private static int toBigEndianUnsignedShort(int msb, int lsb) {
        return ((msb & 0xff) << 8) + (lsb & 0xff);
    }

    private final int deviceIdClass;

    public int getDeviceIdClass() {
        return deviceIdClass;
    }
}
