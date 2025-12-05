package com.example.chatchannels.client;

import com.example.chatchannels.api.ChannelDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ClientChannelManager {

    private static final ClientChannelManager INSTANCE = new ClientChannelManager();

    private final List<ChannelDefinition> channels = new ArrayList<>();
    private int version = 0;
    private String currentChannelId;
    private final Set<String> highlightedChannels = new HashSet<>();

    private static final int MAX_MESSAGES_PER_CHANNEL = 200;
    private final Map<String, List<Component>> messageHistory = new HashMap<>();

    private ClientChannelManager() {
    }

    public static ClientChannelManager getInstance() {
        return INSTANCE;
    }

    public synchronized void setChannels(List<ChannelDefinition> newChannels) {
        this.channels.clear();
        this.channels.addAll(newChannels);
        this.version++;
        this.highlightedChannels.clear();
    }

    public synchronized List<ChannelDefinition> getChannels() {
        return Collections.unmodifiableList(new ArrayList<>(channels));
    }

    public synchronized ChannelDefinition findChannelById(String id) {
        if (id == null) {
            return null;
        }
        for (ChannelDefinition def : channels) {
            if (id.equals(def.getId())) {
                return def;
            }
        }
        return null;
    }

    public synchronized int getVersion() {
        return version;
    }

    public synchronized String getCurrentChannelId() {
        return currentChannelId;
    }

    public synchronized void setCurrentChannelId(String currentChannelId) {
        this.currentChannelId = currentChannelId;
        if (currentChannelId != null) {
            highlightedChannels.remove(currentChannelId);
        }
    }

    public synchronized void markChannelHighlighted(String channelId) {
        if (channelId != null && !channelId.equals(currentChannelId)) {
            highlightedChannels.add(channelId);
        }
    }

    public synchronized boolean isChannelHighlighted(String channelId) {
        return channelId != null && highlightedChannels.contains(channelId);
    }

    public synchronized void addMessage(String channelId, Component line) {
        if (channelId == null) {
            channelId = "";
        }
        List<Component> list = messageHistory.computeIfAbsent(channelId, k -> new ArrayList<>());
        list.add(line);
        if (list.size() > MAX_MESSAGES_PER_CHANNEL) {
            list.remove(0);
        }
    }

    public void redrawCurrentChannelChat() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null) {
            return;
        }

        List<Component> messages;
        synchronized (this) {
            String channelId = currentChannelId;
            if (channelId == null) {
                channelId = "";
            }
            List<Component> list = messageHistory.get(channelId);
            if (list == null || list.isEmpty()) {
                messages = Collections.emptyList();
            } else {
                messages = new ArrayList<>(list);
            }
        }

        mc.gui.getChat().clearMessages(false);
        for (Component c : messages) {
            mc.gui.getChat().addMessage(c);
        }
    }
}
