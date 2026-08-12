package org.mastersmp.packet.nms;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AdapterLoaderTest {

    @Test
    void stripsBukkitSnapshotSuffix() {
        assertEquals("26.2", AdapterLoader.normalizeMinecraftVersion("26.2-R0.1-SNAPSHOT"));
        assertEquals("1.21.4", AdapterLoader.normalizeMinecraftVersion("1.21.4-R0.1-SNAPSHOT"));
    }

    @Test
    void stripsPaperBuildSuffix() {
        assertEquals("26.2", AdapterLoader.normalizeMinecraftVersion("26.2.build.12"));
    }

    @Test
    void mapsKnownVersions() {
        assertEquals("v1_16_R3", AdapterLoader.lookupBucket("1.16.5"));
        assertEquals("v1_21_R3", AdapterLoader.lookupBucket("1.21.4"));
        assertEquals("v26_2", AdapterLoader.lookupBucket("26.2"));
        assertEquals("v26_1", AdapterLoader.lookupBucket("26.1.2"));
    }

    @Test
    void unknownLegacyIsNull() {
        assertNull(AdapterLoader.lookupBucket("1.8.8"));
    }
}
