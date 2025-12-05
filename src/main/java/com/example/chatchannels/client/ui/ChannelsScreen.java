package com.example.chatchannels.client.ui;

import com.example.chatchannels.api.ChannelDefinition;
import com.example.chatchannels.client.ClientChannelManager;
import com.example.chatchannels.network.C2SChannelCommandPacket;
import com.example.chatchannels.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public class ChannelsScreen extends Screen {

    private final Screen parent;
    private int lastVersion = -1;

    public ChannelsScreen(Screen parent) {
        super(Component.literal("Channels"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        if (this.minecraft == null) {
            return;
        }

        // Запрос списка каналов у сервера
        NetworkHandler.CHANNEL.sendToServer(new C2SChannelCommandPacket(C2SChannelCommandPacket.Action.LIST, "", ""));

        this.lastVersion = -1;
        rebuildButtons();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.minecraft == null) {
            return;
        }
        int version = ClientChannelManager.getInstance().getVersion();
        if (version != lastVersion) {
            lastVersion = version;
            rebuildButtons();
        }
    }

    private void rebuildButtons() {
        this.clearWidgets();

        int y = 40;
        int x = this.width / 2 - 100;

        for (ChannelDefinition def : ClientChannelManager.getInstance().getChannels()) {
            String typeLabel = def.getType().name().toLowerCase(Locale.ROOT);
            String label = def.getName() + " (" + typeLabel + ")";

            int color = 0xFFFFFF;
            String hex = def.getColorHex();
            if (hex != null && hex.startsWith("#") && hex.length() == 7) {
                try {
                    color = Integer.parseInt(hex.substring(1), 16);
                } catch (NumberFormatException ignored) {
                }
            }

            final int finalColor = color;
            Component text = Component.literal(label).withStyle(style -> style.withColor(finalColor));

            Button btn = Button.builder(text, b -> {
                        NetworkHandler.CHANNEL.sendToServer(new C2SChannelCommandPacket(C2SChannelCommandPacket.Action.JOIN, def.getId(), ""));
                        Minecraft.getInstance().setScreen(parent);
                    })
                    .pos(x, y)
                    .size(200, 20)
                    .build();
            this.addRenderableWidget(btn);
            y += 24;
        }

        // Кнопка назад
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> Minecraft.getInstance().setScreen(parent))
                .pos(this.width / 2 - 40, this.height - 30)
                .size(80, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        String currentId = ClientChannelManager.getInstance().getCurrentChannelId();
        if (currentId != null && !currentId.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, "Current: " + currentId, this.width / 2, 32, 0xAAAAAA);
        }
    }
}
