package net.opmasterleo.packet;

import java.util.Objects;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.opmasterleo.packet.channel.InjectionProbe;
import net.opmasterleo.packet.channel.PacketChannels;
import net.opmasterleo.packet.channel.PacketListenerBus;
import net.opmasterleo.packet.component.Components;
import net.opmasterleo.packet.item.Items;
import net.opmasterleo.packet.menu.Menus;
import net.opmasterleo.packet.nms.AdapterLoader;
import net.opmasterleo.packet.nms.NmsAdapter;
import net.opmasterleo.packet.packet.Packets;
import net.opmasterleo.packet.schedule.Schedulers;
import net.opmasterleo.packet.world.Chunks;
import net.opmasterleo.packet.world.Dimensions;
import net.opmasterleo.packet.world.Entities;
import net.opmasterleo.packet.world.Levels;
import net.opmasterleo.packet.world.Worlds;

public final class PacketAPI {

    private static volatile PacketAPI instance;

    private final JavaPlugin plugin;
    private final NmsAdapter adapter;
    private final Schedulers schedulers;
    private final PacketChannels channels;
    private final PacketListenerBus listeners;
    private final Packets packets;
    private final Items items;
    private final Menus menus;
    private final Worlds worlds;
    private final Chunks chunks;
    private final Dimensions dimensions;
    private final Levels levels;
    private final Entities entities;
    private final Components components;

    private PacketAPI(JavaPlugin plugin, NmsAdapter adapter) {
        this.plugin = plugin;
        this.adapter = adapter;
        this.schedulers = new Schedulers(plugin);
        this.listeners = PacketListenerBus.create(adapter, schedulers, plugin.getName(), plugin.getLogger());
        this.channels = listeners.channels();
        this.packets = new Packets(adapter);
        this.items = new Items(adapter);
        this.menus = new Menus(adapter);
        this.worlds = new Worlds(adapter);
        this.chunks = new Chunks(adapter);
        this.dimensions = new Dimensions(adapter);
        this.levels = new Levels(adapter);
        this.entities = new Entities(adapter);
        this.components = new Components(adapter);
    }

    public static PacketAPI bootstrap(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        PacketAPI created = new PacketAPI(plugin, AdapterLoader.load());
        instance = created;
        return created;
    }

    public static PacketAPI get() {
        PacketAPI api = instance;
        if (api == null) {
            throw new IllegalStateException("PacketAPI is not bootstrapped");
        }
        return api;
    }

    public static boolean isReady() {
        return instance != null;
    }

    public void shutdown() {
        listeners.clear();
        schedulers.shutdown();
        if (instance == this) {
            instance = null;
        }
    }

    /**
     * Probe Netty injection ownership for a player (this instance / foreign / none).
     */
    public InjectionProbe.Result detectInjectionConflicts(Player player) {
        return listeners.detectConflicts(player);
    }

    /**
     * Status/debug summary of injection ownership for a player.
     */
    public String injectionStatus(Player player) {
        return listeners.describeInjection(player);
    }

    public JavaPlugin plugin() {
        return plugin;
    }

    public NmsAdapter adapter() {
        return adapter;
    }

    public Schedulers schedulers() {
        return schedulers;
    }

    public PacketChannels channels() {
        return channels;
    }

    public PacketListenerBus listeners() {
        return listeners;
    }

    public Packets packets() {
        return packets;
    }

    public Items items() {
        return items;
    }

    public Menus menus() {
        return menus;
    }

    public Worlds worlds() {
        return worlds;
    }

    public Chunks chunks() {
        return chunks;
    }

    public Dimensions dimensions() {
        return dimensions;
    }

    public Levels levels() {
        return levels;
    }

    public Entities entities() {
        return entities;
    }

    public Components components() {
        return components;
    }
}
