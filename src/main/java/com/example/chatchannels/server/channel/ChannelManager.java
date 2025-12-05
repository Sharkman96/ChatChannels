package com.example.chatchannels.server.channel;

import com.example.chatchannels.api.ChannelDefinition;
import com.example.chatchannels.api.ChannelType;
import com.example.chatchannels.config.ChannelConfig;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ChannelManager {

    private static final ChannelManager INSTANCE = new ChannelManager();

    private final Map<String, ChannelDefinition> channels = new HashMap<>();
    private String defaultChannelId = "global";

    private ChannelManager() {
    }

    public static ChannelManager getInstance() {
        return INSTANCE;
    }

    public void loadFromConfig(ChannelConfig config) {
        channels.clear();
        if (config.channels != null) {
            for (ChannelDefinition def : config.channels) {
                if (def == null || def.getId() == null || def.getId().isEmpty()) {
                    continue;
                }
                channels.put(def.getId(), def);
                if (def.isDefault()) {
                    defaultChannelId = def.getId();
                }
            }
        }
        if (!channels.containsKey(defaultChannelId)) {
            // fallback: pick any GLOBAL or the first channel
            Optional<ChannelDefinition> global = channels.values().stream()
                    .filter(c -> c.getType() == ChannelType.GLOBAL)
                    .findFirst();
            defaultChannelId = global.map(ChannelDefinition::getId)
                    .orElseGet(() -> channels.keySet().stream().findFirst().orElse("global"));
        }
    }

    public ChannelDefinition getChannel(String id) {
        return channels.get(id);
    }

    public Collection<ChannelDefinition> getChannels() {
        return Collections.unmodifiableCollection(channels.values());
    }

    public String getDefaultChannelId() {
        return defaultChannelId;
    }
}
