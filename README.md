# PacketAPI

Modern, versioned access to the Minecraft protocol through **direct NMS** (`CraftPlayer.getHandle()`, `ServerPlayer.connection.send`). Menu/container packets live in [PacketUxUi](https://github.com/OPmasterLEO/PacketUxUi); sign input lives in [SignGUI](https://github.com/OPmasterLEO/SignGUI). Optional Netty intercept uses an **instance-scoped** injection path that refuses to silently overwrite another PacketAPI copy.

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/d8e7bc74da0d4ce29142f3ac5886a98e)](https://app.codacy.com/gh/OPmasterLEO/MasterPacketAPI/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)

## What it is

- Per-version paperweight adapters (1.16 → 26.2) sharing one NMS implementation when the packet layout does not break
- Typed `PacketWrap` around raw `net.minecraft.network.protocol.Packet` handles
- Custom Bukkit events (`PacketSendEvent`, `NmsPlayerJoinEvent`, `NmsSwingEvent`, `NmsUnknownEntityInteractEvent`) with NMS player handles
- Folia / Paper / Canvas region-safe schedulers
- Ownership-aware `PacketListenerBus.ensureInjected` (single inject entry point) that detects foreign shaded copies via pipeline markers and backs off instead of hijacking
- No duplication of PacketUxUi window click / open / set-slot / cursor / book packets

## Send a packet

```java
import net.opmasterleo.packet.PacketAPI;
import net.kyori.adventure.text.Component;

PacketAPI api = PacketAPI.get();
Object handle = api.adapter().players().handle(player); // ServerPlayer
api.packets().setHealth(player, 20f, 20, 5f);
api.packets().systemChat(player, Component.text("hello"), false);
```

Listen without touching Netty:

```java
import net.opmasterleo.packet.event.PacketSendEvent;
import net.opmasterleo.packet.event.NmsPlayerJoinEvent;

@EventHandler
public void onPacket(PacketSendEvent event) {
    if (event.getPacketName().contains("SetHealth")) {
        event.setCancelled(true);
    }
}

@EventHandler
public void onJoin(NmsPlayerJoinEvent event) {
    Object serverPlayer = event.getPlayerHandle();
}
```

Optional Netty filters (single entry point — never call low-level inject helpers):

```java
api.listeners().addOutbound(player, (viewer, packet) -> OutboundPacketResult.keep());
// Conflict / ownership probe for status/debug:
InjectionProbe.Result probe = api.detectInjectionConflicts(player);
String status = api.injectionStatus(player);
```

## Build / publish

```bash
./gradlew shadowJar
./gradlew publish          # Reposilite releases
./gradlew publishSnapshot  # Reposilite snapshots
```
