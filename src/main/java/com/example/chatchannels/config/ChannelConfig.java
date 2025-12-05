package com.example.chatchannels.config;

import com.example.chatchannels.api.ChannelDefinition;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ChannelConfig {

    @SerializedName("channels")
    public List<ChannelDefinition> channels = new ArrayList<>();

    @SerializedName("rate_limit")
    public RateLimit rateLimit = new RateLimit();

    public static class RateLimit {
        @SerializedName("messages")
        public int messages = 5;

        @SerializedName("per_seconds")
        public int perSeconds = 3;
    }
}
