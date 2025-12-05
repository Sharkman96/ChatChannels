package com.example.chatchannels.config;

import com.example.chatchannels.ChatChannels;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Загрузка цветов тега привилегий из config/chatchannels_privileges.json.
 */
public final class PrivilegeColorConfigLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatChannels.MOD_ID + ":privileges_config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String CONFIG_FILE_NAME = "chatchannels_privileges.json";

    private static final int FALLBACK_COLOR = 0xFFD700; // золотистый по умолчанию

    private static int defaultColor = FALLBACK_COLOR;
    private static final Map<String, Integer> TAG_COLORS = new LinkedHashMap<>();

    private PrivilegeColorConfigLoader() {
    }

    public static void loadOrCreate() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path configPath = configDir.resolve(CONFIG_FILE_NAME);

        PrivilegeColorsConfig config;

        if (Files.notExists(configPath)) {
            config = createDefaultConfig();
            try {
                writeConfig(configPath, config);
            } catch (IOException e) {
                LOGGER.error("Failed to write default privilege colors config", e);
            }
        } else {
            try (BufferedReader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                config = GSON.fromJson(reader, PrivilegeColorsConfig.class);
                if (config == null) {
                    LOGGER.warn("Privilege colors config was empty, recreating defaults");
                    config = createDefaultConfig();
                }
            } catch (IOException e) {
                LOGGER.error("Failed to read privilege colors config, using defaults", e);
                config = createDefaultConfig();
            }
        }

        applyConfig(config);
    }

    private static void writeConfig(Path path, PrivilegeColorsConfig config) throws IOException {
        Files.createDirectories(path.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
        }
    }

    private static PrivilegeColorsConfig createDefaultConfig() {
        PrivilegeColorsConfig config = new PrivilegeColorsConfig();
        config.defaultColor = "#FFD700";
        config.tags.put("Игрок", "#AAAAAA");
        config.tags.put("VIP", "#55FFFF");
        config.tags.put("Модератор", "#55FF55");
        config.tags.put("Админ", "#FF5555");
        config.tags.put("OP", "#FF5555");
        return config;
    }

    private static void applyConfig(PrivilegeColorsConfig config) {
        TAG_COLORS.clear();
        defaultColor = parseColor(config.defaultColor, FALLBACK_COLOR);
        if (config.tags != null) {
            for (Map.Entry<String, String> e : config.tags.entrySet()) {
                int color = parseColor(e.getValue(), defaultColor);
                TAG_COLORS.put(e.getKey(), color);
            }
        }
        LOGGER.info("Loaded {} privilege color entries, default color #{},",
                TAG_COLORS.size(), String.format("%06X", defaultColor));
    }

    private static int parseColor(String hex, int fallback) {
        if (hex == null) {
            return fallback;
        }
        String s = hex.trim();
        if (s.startsWith("#")) {
            s = s.substring(1);
        }
        if (s.length() != 6) {
            return fallback;
        }
        try {
            return Integer.parseInt(s, 16);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * Получить цвет для тега привилегии. Если тег не найден, используется defaultColor.
     */
    public static int getColor(String tag) {
        if (tag == null || tag.isEmpty()) {
            return defaultColor;
        }
        return TAG_COLORS.getOrDefault(tag, defaultColor);
    }

    private static final class PrivilegeColorsConfig {
        String defaultColor = "#FFD700";
        Map<String, String> tags = new LinkedHashMap<>();
    }
}
