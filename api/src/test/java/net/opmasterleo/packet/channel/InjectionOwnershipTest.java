package net.opmasterleo.packet.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;

/**
 * Proves two independently "loaded" PacketAPI identities cannot silently hijack each other.
 */
class InjectionOwnershipTest {

    @Test
    void secondInstanceDoesNotSilentlyOverwriteFirstHandler() {
        UUID playerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("packet_handler", new ChannelInboundHandlerAdapter());

        InjectionIdentity first = new InjectionIdentity("DonutSMPCore", "aaa111");
        InjectionIdentity second = new InjectionIdentity("OtherPlugin", "bbb222");

        ChannelHandler handlerA = new ChannelDuplexHandler();
        List<String> warnings = new ArrayList<>();

        InjectionInstallResult r1 = InjectionInstaller.installNow(
                channel, first, playerId, handlerA, null, warnings::add);
        assertEquals(InjectionInstallResult.INSTALLED, r1);
        assertTrue(warnings.isEmpty());
        assertSame(handlerA, channel.pipeline().get(first.handlerName(playerId)));

        ChannelHandler handlerB = new ChannelDuplexHandler();
        InjectionInstallResult r2 = InjectionInstaller.installNow(
                channel, second, playerId, handlerB, null, warnings::add);

        assertEquals(InjectionInstallResult.CONFLICT_FOREIGN, r2, "foreign install must back off");
        assertFalse(warnings.isEmpty(), "conflict must be visible via warning callback");
        assertTrue(warnings.getFirst().contains("Another PacketAPI instance"));
        assertTrue(warnings.getFirst().contains("Refusing to overwrite"));

        // First instance still owns the pipeline — no silent eviction.
        assertSame(handlerA, channel.pipeline().get(first.handlerName(playerId)));
        assertNull(channel.pipeline().get(second.handlerName(playerId)));
        assertEquals(InjectionProbe.State.FOREIGN, InjectionProbe.probe(channel, second, playerId).state());
        assertEquals(InjectionProbe.State.OWN, InjectionProbe.probe(channel, first, playerId).state());
    }

    @Test
    void sameInstanceReinjectIsIdempotentAndReplacesOwnHandlerOnly() {
        UUID playerId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("packet_handler", new ChannelInboundHandlerAdapter());

        InjectionIdentity identity = new InjectionIdentity("PacketAPI", "same999");
        ChannelHandler firstHandler = new ChannelDuplexHandler();
        ChannelHandler secondHandler = new ChannelDuplexHandler();
        List<String> warnings = new ArrayList<>();

        assertEquals(
                InjectionInstallResult.INSTALLED,
                InjectionInstaller.installNow(channel, identity, playerId, firstHandler, null, warnings::add));
        assertEquals(
                InjectionInstallResult.REPLACED_OWN,
                InjectionInstaller.installNow(channel, identity, playerId, secondHandler, null, warnings::add));
        assertTrue(warnings.isEmpty());
        assertSame(secondHandler, channel.pipeline().get(identity.handlerName(playerId)));
        assertEquals(1, countPacketApiHandlers(channel, playerId));
    }

    @Test
    void uninjectAllOnlyRemovesThisIdentityHandlers() {
        UUID playerId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("packet_handler", new ChannelInboundHandlerAdapter());

        InjectionIdentity a = new InjectionIdentity("PluginA", "tokA");
        InjectionIdentity b = new InjectionIdentity("PluginB", "tokB");
        ChannelHandler handlerA = new ChannelDuplexHandler();
        ChannelHandler handlerB = new ChannelDuplexHandler();

        assertEquals(
                InjectionInstallResult.INSTALLED,
                InjectionInstaller.installNow(channel, a, playerId, handlerA, null, null));

        // Simulate a foreign handler already present under a different token (force-add).
        channel.pipeline().addBefore("packet_handler", b.handlerName(playerId), handlerB);
        channel.attr(InjectionMarkers.MARKER_ATTR).set(b.encodeMarker(b.handlerName(playerId), handlerB.getClass()));

        // Identity A removes only its own handlers.
        for (String name : ChannelOps.pipelineNames(channel)) {
            if (a.isOwnHandlerName(name)) {
                ChannelOps.remove(channel, name);
            }
        }
        String marker = channel.attr(InjectionMarkers.MARKER_ATTR).get();
        if (marker != null && marker.contains("token=" + a.instanceToken())) {
            channel.attr(InjectionMarkers.MARKER_ATTR).set(null);
        }

        assertNull(channel.pipeline().get(a.handlerName(playerId)));
        assertSame(handlerB, channel.pipeline().get(b.handlerName(playerId)), "foreign handler must survive");
        assertNotNull(channel.attr(InjectionMarkers.MARKER_ATTR).get());
    }

    @Test
    void detectConflictsReportsForeignVsOwnVsNone() {
        UUID playerId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("packet_handler", new ChannelInboundHandlerAdapter());

        InjectionIdentity a = new InjectionIdentity("A", "idA");
        InjectionIdentity b = new InjectionIdentity("B", "idB");

        assertEquals(InjectionProbe.State.NONE, InjectionProbe.probe(channel, a, playerId).state());

        InjectionInstaller.installNow(channel, a, playerId, new ChannelDuplexHandler(), null, null);
        assertEquals(InjectionProbe.State.OWN, InjectionProbe.probe(channel, a, playerId).state());
        assertEquals(InjectionProbe.State.FOREIGN, InjectionProbe.probe(channel, b, playerId).state());
    }

    @Test
    void shadedStylePlainFilterNameNoLongerCollidesAcrossTokens() {
        UUID playerId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        InjectionIdentity a = new InjectionIdentity("DonutSMPCore", "deadbeef");
        InjectionIdentity b = new InjectionIdentity("OtherPlugin", "cafebabe");
        // Old bug: both computed "packetapi_filter_" + uuid
        String legacy = "packetapi_filter_" + playerId;
        assertFalse(a.handlerName(playerId).equals(b.handlerName(playerId)));
        assertFalse(a.handlerName(playerId).equals(legacy));
        assertFalse(b.handlerName(playerId).equals(legacy));
        assertTrue(InjectionMarkers.looksLikePacketApiHandler(a.handlerName(playerId)));
        assertTrue(InjectionMarkers.looksLikePacketApiHandler(legacy));
    }

    @Test
    void legacyPlainFilterNameIsTreatedAsForeignAndNotOverwritten() {
        UUID playerId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("packet_handler", new ChannelInboundHandlerAdapter());

        String legacyName = "packetapi_filter_" + playerId;
        ChannelHandler legacyHandler = new ChannelDuplexHandler();
        channel.pipeline().addBefore("packet_handler", legacyName, legacyHandler);

        InjectionIdentity modern = new InjectionIdentity("PacketAPI", "modern01");
        List<String> warnings = new ArrayList<>();
        InjectionInstallResult result = InjectionInstaller.installNow(
                channel, modern, playerId, new ChannelDuplexHandler(), null, warnings::add);

        assertEquals(InjectionInstallResult.CONFLICT_FOREIGN, result);
        assertFalse(warnings.isEmpty());
        assertSame(legacyHandler, channel.pipeline().get(legacyName));
        assertNull(channel.pipeline().get(modern.handlerName(playerId)));
    }

    private static int countPacketApiHandlers(EmbeddedChannel channel, UUID playerId) {
        String suffix = InjectionMarkers.FILTER_SEGMENT + playerId;
        int count = 0;
        for (String name : channel.pipeline().names()) {
            if (InjectionMarkers.looksLikePacketApiHandler(name) && name.endsWith(suffix)) {
                count++;
            }
        }
        return count;
    }
}
