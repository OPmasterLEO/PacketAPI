# MasterPacketAPI

Modern multi-version NMS / packet abstraction for **Paper**, **Folia**, and **Canvas** (Minecraft **1.16+**).

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.16%2B-brightgreen.svg)](https://papermc.io/)
[![Folia](https://img.shields.io/badge/Folia-supported-blue.svg)](https://papermc.io/software/folia)
[![License: LGPL-3.0](https://img.shields.io/badge/License-LGPL%203.0-blue.svg)](LICENSE)
[![Build](https://img.shields.io/badge/build-Gradle-02303A.svg)](https://gradle.org/)
[![Maven](https://img.shields.io/badge/maven-repo.mastersmp.net-red.svg)](http://repo.mastersmp.net/)

`org.mastersmp.packet` owns version-fragile CraftBukkit / NMS packet, item, menu, and world bridges so consumer plugins stay on a version-agnostic API.

## Features

- Runtime NMS adapter selection (`v1_16_R*` → `v1_21_R*` → `v26_*`)
- Netty channel inject / uninject (inbound + outbound) with `KEEP` / `DROP` / `REPLACE`
- Packet send helpers (text displays, entity destroy, container slots, block events)
- Item, menu, world, and Adventure component bridges
- Folia-safe **Advanced** / **Fast** / **Direct** schedulers + dedicated async worker pools
- paperweight-userdev Mojang-mapped adapters for modern Paper

## Repository

```kotlin
repositories {
    maven("http://repo.mastersmp.net/releases") {
        isAllowInsecureProtocol = true
    }
    // snapshots:
    // maven("http://repo.mastersmp.net/snapshots") { isAllowInsecureProtocol = true }
}
```

## Dependency

### Plugin jar (shaded API + all NMS adapters)

```kotlin
dependencies {
    compileOnly("org.mastersmp.packet:masterpacketapi:1.0.0")
}
```

### API-only (compile against interfaces)

```kotlin
dependencies {
    compileOnly("org.mastersmp.packet:packetapi-api:1.0.0")
}
```

Install **MasterPacketAPI** on the server (soft/hard depend as needed). Consumers must not shade NMS adapters themselves.

`plugin.yml`:

```yaml
depend: [MasterPacketAPI]
# or
softdepend: [MasterPacketAPI]
```

## Quick start

```java
import org.mastersmp.packet.PacketAPI;
import org.mastersmp.packet.channel.OutboundPacketResult;
import org.mastersmp.packet.channel.PacketView;

public final class ExamplePlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        PacketAPI api = PacketAPI.get(); // bootstrapped by MasterPacketAPI at STARTUP

        api.listeners().addOutbound((viewer, packet) -> {
            if ("SystemChat".equals(packet.name())) {
                return OutboundPacketResult.drop();
            }
            return OutboundPacketResult.keep();
        });

        api.schedulers().advanced().runGlobal(() ->
                getLogger().info("adapter=" + api.adapter().bucketId())
        );
    }
}
```

### Channels

```java
api.channels().injectOutbound(player, "myplugin_out", handler);
api.channels().injectInbound(player, "myplugin_in", handler);
api.channels().uninject(player, "myplugin_out");
api.channels().runOnEventLoop(api.channels().channel(player), () -> { /* netty thread */ });
```

### Schedulers

```java
var s = api.schedulers();
s.advanced().runEntity(player, task);   // tracked Folia/Paper/Bukkit task
s.fast().coalesceGlobal(task);          // merge work into one tick flush
s.direct().executeAsync(task);          // fire-and-forget
s.workers().supply(() -> heavyCompute());
```

## Modules

| Module | Role |
|--------|------|
| `packetapi-api` / `api` | Version-agnostic public API |
| `nms-api` | Adapter interfaces + loader |
| `plugin` | Bootstrap `MasterPacketAPI` |
| `nms:v*` | paperweight / Spigot adapters (1.16 → 26.x) |

## Build

```bash
./gradlew build
./gradlew publish          # releases → http://repo.mastersmp.net/releases
./gradlew publishSnapshot  # snapshots → http://repo.mastersmp.net/snapshots
```

Reposilite credentials: `reposilite.user` / `reposilite.token` (Gradle props) or `REPOSILITE_USER` / `REPOSILITE_TOKEN`.

## License

LGPL-3.0 — see [LICENSE](LICENSE) and [COPYING](COPYING).
