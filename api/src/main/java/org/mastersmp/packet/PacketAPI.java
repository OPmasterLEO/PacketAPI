package org.mastersmp.packet;

import java.util.Objects;

import org.bukkit.plugin.java.JavaPlugin;

import org.mastersmp.packet.channel.PacketChannels;
import org.mastersmp.packet.channel.PacketListenerBus;
import org.mastersmp.packet.component.Components;
import org.mastersmp.packet.item.Items;
import org.mastersmp.packet.menu.Menus;
import org.mastersmp.packet.nms.AdapterLoader;
import org.mastersmp.packet.nms.NmsAdapter;
import org.mastersmp.packet.packet.Packets;
import org.mastersmp.packet.schedule.Schedulers;
import org.mastersmp.packet.world.Chunks;
import org.mastersmp.packet.world.Dimensions;
import org.mastersmp.packet.world.Entities;
import org.mastersmp.packet.world.Levels;
import org.mastersmp.packet.world.Worlds;

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
        this.channels = new PacketChannels(adapter, schedulers);
        this.listeners = new PacketListenerBus(channels, adapter);
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
        channels.uninjectAll();
        schedulers.shutdown();
        if (instance == this) {
            instance = null;
        }
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
