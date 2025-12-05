package com.example.chatchannels.server.log;

import com.example.chatchannels.ChatChannels;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ChatLogManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatChannels.MOD_ID + ":chatlog");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final BlockingQueue<String> QUEUE = new LinkedBlockingQueue<>();
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    private ChatLogManager() {
    }

    public static void init() {
        if (STARTED.compareAndSet(false, true)) {
            Thread t = new Thread(ChatLogManager::run, "ChatChannels-Logger");
            t.setDaemon(true);
            t.start();
        }
    }

    public static void log(String channelId, String senderName, String message) {
        if (!STARTED.get()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        String line = String.format("[%s] [%s] <%s> %s", TIMESTAMP.format(now), channelId, senderName, message);
        QUEUE.offer(line);
    }

    private static void run() {
        while (true) {
            try {
                String line = QUEUE.take();
                writeLine(line);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                LOGGER.error("Error while writing chat log", e);
            }
        }
    }

    private static void writeLine(String line) throws IOException {
        Path gameDir = FMLPaths.GAMEDIR.get();
        LocalDate today = LocalDate.now();
        Path logDir = gameDir.resolve("logs").resolve("chat");
        Files.createDirectories(logDir);
        Path logFile = logDir.resolve(DATE.format(today) + ".log");
        try (BufferedWriter writer = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8,
                Files.exists(logFile) ? java.nio.file.StandardOpenOption.APPEND : java.nio.file.StandardOpenOption.CREATE)) {
            writer.write(line);
            writer.newLine();
        }
    }
}
