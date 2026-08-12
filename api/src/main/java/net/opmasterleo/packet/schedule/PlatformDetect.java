package net.opmasterleo.packet.schedule;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class PlatformDetect {

    public enum Kind {
        FOLIA,
        CANVAS,
        PAPER,
        BUKKIT
    }

    private final Plugin plugin;
    private final Kind kind;

    public PlatformDetect(Plugin plugin) {
        this.plugin = plugin;
        this.kind = detect();
    }

    public Plugin plugin() {
        return plugin;
    }

    public Kind kind() {
        return kind;
    }

    public boolean isFolia() {
        return kind == Kind.FOLIA || kind == Kind.CANVAS;
    }

    public boolean isCanvas() {
        return kind == Kind.CANVAS;
    }

    public boolean isPaper() {
        return kind == Kind.PAPER || isFolia();
    }

    private static Kind detect() {
        if (classPresent("io.canvasmc.canvas.server.CanvasServer")
                || classPresent("io.canvasmc.canvas.Canvas")) {
            return Kind.CANVAS;
        }
        if (classPresent("io.papermc.paper.threadedregions.RegionizedServer")) {
            return Kind.FOLIA;
        }
        try {
            Bukkit.getServer().getClass().getMethod("getGlobalRegionScheduler");
            return Kind.PAPER;
        } catch (NoSuchMethodException ignored) {
            return Kind.BUKKIT;
        }
    }

    private static boolean classPresent(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            return false;
        }
    }
}
