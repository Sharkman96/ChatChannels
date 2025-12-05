package com.example.chatchannels.server.perm;

import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Формирование тега привилегий игрока.
 * Порядок приоритета:
 * 1) LuckPerms (prefix)
 * 2) FTB Ranks (ftbranks.name_format)
 * 3) OP (если hasPermissions(4))
 */
public final class ChatPrivilegeFormatter {

    private ChatPrivilegeFormatter() {
    }

    public static String getPrivilegeTag(ServerPlayer player) {
        if (player == null) {
            return "";
        }

        String tag = tryLuckPerms(player);
        if (!tag.isEmpty()) {
            return tag;
        }

        tag = tryFtbranks(player);
        if (!tag.isEmpty()) {
            return tag;
        }

        if (player.hasPermissions(4)) {
            return "OP";
        }

        return "";
    }

    private static String tryLuckPerms(ServerPlayer player) {
        try {
            Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Method getApi = providerClass.getMethod("get");
            Object api = getApi.invoke(null);

            Class<?> luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
            Method getUserManager = luckPermsClass.getMethod("getUserManager");
            Object userManager = getUserManager.invoke(api);

            Class<?> userManagerClass = Class.forName("net.luckperms.api.model.user.UserManager");
            Method getUser = userManagerClass.getMethod("getUser", java.util.UUID.class);
            Object user = getUser.invoke(userManager, player.getUUID());
            if (user == null) {
                return "";
            }

            Class<?> userClass = Class.forName("net.luckperms.api.model.user.User");
            Method getCachedData = userClass.getMethod("getCachedData");
            Object cachedData = getCachedData.invoke(user);

            Class<?> cachedDataClass = Class.forName("net.luckperms.api.cacheddata.CachedDataManager");
            Method getMetaData = cachedDataClass.getMethod("getMetaData");
            Object meta = getMetaData.invoke(cachedData);

            Class<?> metaClass = Class.forName("net.luckperms.api.cacheddata.CachedMetaData");
            Method getPrefix = metaClass.getMethod("getPrefix");
            String prefix = (String) getPrefix.invoke(meta);
            if (prefix == null || prefix.isEmpty()) {
                return "";
            }

            return normalizeTag(prefix);
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static String tryFtbranks(ServerPlayer player) {
        try {
            Class<?> apiClass = Class.forName("dev.ftb.mods.ftbranks.api.FTBRanksAPI");
            Method managerMethod = apiClass.getMethod("manager");
            Object manager = managerMethod.invoke(null);
            if (manager == null) {
                return "";
            }

            Class<?> managerClass = Class.forName("dev.ftb.mods.ftbranks.api.RankManager");
            Method getRanks = managerClass.getMethod("getRanks", ServerPlayer.class);
            @SuppressWarnings("unchecked")
            List<?> ranks = (List<?>) getRanks.invoke(manager, player);
            if (ranks == null || ranks.isEmpty()) {
                return "";
            }

            Class<?> rankClass = Class.forName("dev.ftb.mods.ftbranks.api.Rank");
            Class<?> permValueClass = Class.forName("dev.ftb.mods.ftbranks.api.PermissionValue");
            Method getPermission = rankClass.getMethod("getPermission", String.class);
            Method asString = permValueClass.getMethod("asString");

            // Берём первую подходящую роль (список уже учитывает активность и приоритеты)
            for (Object rank : ranks) {
                Object permValue = getPermission.invoke(rank, "ftbranks.name_format");
                if (permValue == null) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Optional<String> opt = (Optional<String>) asString.invoke(permValue);
                if (opt.isEmpty()) {
                    continue;
                }
                String format = opt.get();
                if (format == null || format.isEmpty()) {
                    continue;
                }
                return normalizeTag(format);
            }

            return "";
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static String normalizeTag(String raw) {
        if (raw == null) {
            return "";
        }
        // Убираем цветовые коды &a, &b и т.п.
        String s = raw.replaceAll("(?i)&[0-9A-FK-OR]", "");
        // Убираем плейсхолдер имени
        s = s.replace("{name}", "");
        s = s.trim();

        // Если строка вида [VIP] - убираем внешние скобки
        if (s.startsWith("[") && s.endsWith("]") && s.length() > 2) {
            s = s.substring(1, s.length() - 1).trim();
        }

        return s;
    }
}
