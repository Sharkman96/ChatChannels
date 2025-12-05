package com.example.chatchannels.client.ui;

import com.example.chatchannels.ChatChannels;
import com.example.chatchannels.api.ChannelDefinition;
import com.example.chatchannels.api.ChannelType;
import com.example.chatchannels.client.ClientChannelManager;
import com.example.chatchannels.network.C2SChannelCommandPacket;
import com.example.chatchannels.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ChatChannels.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ChatChannelsClientScreens {

    private ChatChannelsClientScreens() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof ChatScreen) {
            // Запросить актуальный список каналов при открытии чата
            NetworkHandler.CHANNEL.sendToServer(new C2SChannelCommandPacket(C2SChannelCommandPacket.Action.LIST, "", ""));
        }
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof ChatScreen chatScreen) {
            var guiGraphics = event.getGuiGraphics();
            renderChannelTabs(guiGraphics, chatScreen, event.getMouseX(), event.getMouseY());
        }
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        Screen screen = event.getScreen();
        if (screen instanceof ChatScreen chatScreen && event.getButton() == 0) { // левая кнопка
            boolean handled = handleTabsClick(chatScreen, (int) event.getMouseX(), (int) event.getMouseY());
            if (handled) {
                event.setCanceled(true);
            }
        }
    }

    private static void renderChannelTabs(net.minecraft.client.gui.GuiGraphics guiGraphics,
                                          ChatScreen screen,
                                          int mouseX,
                                          int mouseY) {
        List<ChannelDefinition> channels = new ArrayList<>(ClientChannelManager.getInstance().getChannels());
        if (channels.isEmpty()) {
            return;
        }

        channels.sort((a, b) -> {
            int orderA = getSortOrder(a);
            int orderB = getSortOrder(b);
            if (orderA != orderB) {
                return Integer.compare(orderA, orderB);
            }
            return a.getId().compareToIgnoreCase(b.getId());
        });

        int tabHeight = 12;
        int tabX = 4;
        int tabY = screen.height - 34; // чуть выше строки ввода, компактнее

        String currentId = ClientChannelManager.getInstance().getCurrentChannelId();
        var font = Minecraft.getInstance().font;

        for (ChannelDefinition def : channels) {
            String baseName = def.getName();
            if (baseName == null || baseName.isEmpty()) {
                baseName = def.getId();
            }

            String badge = getBadgeFor(def);
            String text = badge + " " + baseName;
            int textWidth = font.width(text);
            int tabWidth = textWidth + 8; // по ширине текста + паддинги

            if (tabX + tabWidth > screen.width - 4) {
                break;
            }

            boolean active = def.getId().equals(currentId);
            boolean hovered = mouseX >= tabX && mouseX <= tabX + tabWidth && mouseY >= tabY && mouseY <= tabY + tabHeight;

            boolean highlighted = !active && ClientChannelManager.getInstance().isChannelHighlighted(def.getId());

            int bgColor = active ? 0xCC505050 : 0xCC303030; // ARGB, чуть светлее
            if (hovered && !active) {
                bgColor = 0xCC404040;
            }
            if (highlighted && !active) {
                bgColor = 0xCC607040; // подсветка вкладки с новой активностью
            }

            guiGraphics.fill(tabX, tabY, tabX + tabWidth, tabY + tabHeight, bgColor);
            int color = getChannelColor(def, active);

            int textX = tabX + 4;
            int textY = tabY + (tabHeight - font.lineHeight) / 2;
            guiGraphics.drawString(font, text, textX, textY, color, false);

            if (highlighted) {
                int dotSize = 4;
                int dotPadding = 2;
                int dotRight = tabX + tabWidth - dotPadding;
                int dotLeft = dotRight - dotSize;
                int dotTop = tabY + dotPadding;
                int dotBottom = dotTop + dotSize;
                int dotColor = 0xFFFF4040; // ярко-красный индикатор
                guiGraphics.fill(dotLeft, dotTop, dotRight, dotBottom, dotColor);
            }

            tabX += tabWidth + 2;
        }
    }

    private static boolean handleTabsClick(ChatScreen screen, int mouseX, int mouseY) {
        List<ChannelDefinition> channels = new ArrayList<>(ClientChannelManager.getInstance().getChannels());
        if (channels.isEmpty()) {
            return false;
        }

        channels.sort((a, b) -> {
            int orderA = getSortOrder(a);
            int orderB = getSortOrder(b);
            if (orderA != orderB) {
                return Integer.compare(orderA, orderB);
            }
            return a.getId().compareToIgnoreCase(b.getId());
        });

        int tabHeight = 12;
        int tabX = 4;
        int tabY = screen.height - 34;

        var font = Minecraft.getInstance().font;

        for (ChannelDefinition def : channels) {
            String baseName = def.getName();
            if (baseName == null || baseName.isEmpty()) {
                baseName = def.getId();
            }
            String badge = getBadgeFor(def);
            String text = badge + " " + baseName;
            int textWidth = font.width(text);
            int tabWidth = textWidth + 8;

            if (tabX + tabWidth > screen.width - 4) {
                break;
            }

            if (mouseX >= tabX && mouseX <= tabX + tabWidth && mouseY >= tabY && mouseY <= tabY + tabHeight) {
                NetworkHandler.CHANNEL.sendToServer(new C2SChannelCommandPacket(C2SChannelCommandPacket.Action.JOIN, def.getId(), ""));
                return true;
            }

            tabX += tabWidth + 2;
        }

        return false;
    }

    private static int getSortOrder(ChannelDefinition def) {
        ChannelType type = def.getType();
        return switch (type) {
            case GLOBAL -> 0;
            case LOCAL -> 1;
            case PRIVATE -> 2;
            case TRADE -> 3;
            case TEAM -> 4;
            case STAFF -> 5;
            case CUSTOM -> 6;
        };
    }

    private static String getBadgeFor(ChannelDefinition def) {
        ChannelType type = def.getType();
        return switch (type) {
            case GLOBAL -> "G";
            case LOCAL -> "L";
            case PRIVATE -> "PM";
            case TRADE -> "T";
            case TEAM -> "TM";
            case STAFF -> "S";
            case CUSTOM -> "C";
        };
    }

    private static int getChannelColor(ChannelDefinition def, boolean active) {
        String hex = def.getColorHex();
        int base = 0xFFFFFF;
        if (hex != null && hex.startsWith("#") && hex.length() == 7) {
            try {
                base = Integer.parseInt(hex.substring(1), 16);
            } catch (NumberFormatException ignored) {
            }
        }
        if (!active) {
            base = ((base & 0xFEFEFE) >> 1) | 0x404040; // немного затемнить неактивные
        }
        return base;
    }
}
