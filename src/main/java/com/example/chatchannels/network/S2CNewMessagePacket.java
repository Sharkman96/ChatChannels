package com.example.chatchannels.network;

import com.example.chatchannels.api.ChannelDefinition;
import com.example.chatchannels.api.ChannelType;
import com.example.chatchannels.client.ClientChannelManager;
import com.example.chatchannels.network.C2SChannelCommandPacket;
import com.example.chatchannels.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Supplier;

public class S2CNewMessagePacket {

    private final String channelId;
    private final UUID senderUuid;
    private final String senderName;
    private final String message;
    private final String privilegeTag;
    private final int privilegeColor;

    public S2CNewMessagePacket(String channelId, UUID senderUuid, String senderName, String message, String privilegeTag, int privilegeColor) {
        this.channelId = channelId;
        this.senderUuid = senderUuid;
        this.senderName = senderName;
        this.message = message;
        this.privilegeTag = privilegeTag == null ? "" : privilegeTag;
        this.privilegeColor = privilegeColor;
    }

    public S2CNewMessagePacket(FriendlyByteBuf buf) {
        this.channelId = buf.readUtf(64);
        this.senderUuid = buf.readUUID();
        this.senderName = buf.readUtf(64);
        this.message = buf.readUtf(1024);
        this.privilegeTag = buf.readUtf(64);
        this.privilegeColor = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(channelId, 64);
        buf.writeUUID(senderUuid);
        buf.writeUtf(senderName, 64);
        buf.writeUtf(message, 1024);
        buf.writeUtf(privilegeTag, 64);
        buf.writeInt(privilegeColor);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> handleClient());
        ctx.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        ChannelDefinition def = ClientChannelManager.getInstance().findChannelById(channelId);

        String badge = channelId;
        int channelColor = 0xFFFFFF;
        if (def != null) {
            badge = getBadgeFor(def);
            channelColor = getChannelColor(def);
        }

        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        final int finalChannelColor = channelColor;

        MutableComponent line = Component.empty()
                .append(Component.literal("[" + badge + "] ")
                        .withStyle(style -> style.withColor(finalChannelColor)))
                .append(Component.literal("[" + time + "] ")
                        .withStyle(style -> style.withColor(0xAAAAAA)));

        if (!privilegeTag.isEmpty()) {
            int privColor = privilegeColor != 0 ? privilegeColor : 0xFFD700;
            // Тег привилегии рисуем отдельным цветом (по конфигу, либо золотистым по умолчанию)
            line = line.append(Component.literal("[" + privilegeTag + "] ")
                    .withStyle(style -> style.withColor(privColor)));
        }

        line = line.append(Component.literal("<" + senderName + "> ")
                .withStyle(style -> style.withColor(finalChannelColor)))
                .append(Component.literal(message)
                        .withStyle(style -> style.withColor(0xFFFFFF)));

        // Сохраняем сообщение в историю канала на клиенте
        ClientChannelManager mgr = ClientChannelManager.getInstance();
        mgr.addMessage(channelId, line);

        // Отображаем сообщение только в активном канале
        String currentId = mgr.getCurrentChannelId();
        boolean shouldDisplay = true;
        if (currentId != null && !currentId.isEmpty() && !channelId.equals(currentId)) {
            shouldDisplay = false;
        }

        if (shouldDisplay) {
            mc.gui.getChat().addMessage(line);
        }

        // Определяем, является ли канал приватным (по типу или по id "pm")
        boolean isPrivateChannel = (def != null && def.getType() == ChannelType.PRIVATE) || "pm".equals(channelId);

        // Подсветка вкладок и автопереключение на приватный канал
        if (!channelId.equals(currentId)) {
            if (isPrivateChannel) {
                // Для приватных каналов сразу переключаемся (отправляем JOIN на сервер)
                NetworkHandler.CHANNEL.sendToServer(new C2SChannelCommandPacket(C2SChannelCommandPacket.Action.JOIN, channelId, ""));
            } else {
                mgr.markChannelHighlighted(channelId);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private String getBadgeFor(ChannelDefinition def) {
        ChannelType type = def.getType();
        return switch (type) {
            case GLOBAL -> "G";
            case LOCAL -> "L";
            case PRIVATE -> "PM";
            case TRADE -> "T";
            case STAFF -> "S";
            case TEAM -> "TM";
            case CUSTOM -> "C";
        };
    }

    @OnlyIn(Dist.CLIENT)
    private int getChannelColor(ChannelDefinition def) {
        String hex = def.getColorHex();
        if (hex != null && hex.startsWith("#") && hex.length() == 7) {
            try {
                return Integer.parseInt(hex.substring(1), 16);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0xFFFFFF;
    }

    public String getChannelId() {
        return channelId;
    }

    public UUID getSenderUuid() {
        return senderUuid;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessage() {
        return message;
    }
}
