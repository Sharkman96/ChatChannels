package com.example.chatchannels.server.perm;

import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;

public final class PermissionManager {

    private PermissionManager() {
    }

    public static boolean hasPermission(ServerPlayer player, String permissionNode) {
        if (player == null) {
            return false;
        }
        if (permissionNode == null || permissionNode.isEmpty()) {
            return true;
        }

        if (checkLuckPerms(player, permissionNode)) {
            return true;
        }

        if (checkFtbranks(player, permissionNode)) {
            return true;
        }

        return player.hasPermissions(4);
    }

    private static boolean checkLuckPerms(ServerPlayer player, String permissionNode) {
        try {
            Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Method getApi = providerClass.getMethod("get");
            Object api = getApi.invoke(null);

            Class<?> luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
            Method getPlayerAdapter = luckPermsClass.getMethod("getPlayerAdapter", Class.class);
            Object adapter = getPlayerAdapter.invoke(api, ServerPlayer.class);

            Class<?> playerAdapterClass = Class.forName("net.luckperms.api.platform.PlayerAdapter");
            Method getPermissionData = playerAdapterClass.getMethod("getPermissionData", Object.class);
            Object permissionData = getPermissionData.invoke(adapter, player);

            Class<?> cachedPermissionDataClass = Class.forName("net.luckperms.api.cacheddata.CachedPermissionData");
            Method checkPermission = cachedPermissionDataClass.getMethod("checkPermission", String.class);
            Object tristate = checkPermission.invoke(permissionData, permissionNode);

            Class<?> tristateClass = Class.forName("net.luckperms.api.util.Tristate");
            Method asBoolean = tristateClass.getMethod("asBoolean");
            return (Boolean) asBoolean.invoke(tristate);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean checkFtbranks(ServerPlayer player, String permissionNode) {
        try {
            Class<?> apiClass = Class.forName("dev.ftb.mods.ftbranks.api.FTBRanksAPI");
            Method managerMethod = apiClass.getMethod("manager");
            Object manager = managerMethod.invoke(null);
            if (manager == null) {
                return false;
            }

            Class<?> managerClass = Class.forName("dev.ftb.mods.ftbranks.api.RankManager");
            Method getRanks = managerClass.getMethod("getRanks", ServerPlayer.class);
            @SuppressWarnings("unchecked")
            java.util.List<?> ranks = (java.util.List<?>) getRanks.invoke(manager, player);
            if (ranks == null || ranks.isEmpty()) {
                return false;
            }

            Class<?> rankClass = Class.forName("dev.ftb.mods.ftbranks.api.Rank");
            Class<?> permValueClass = Class.forName("dev.ftb.mods.ftbranks.api.PermissionValue");
            Method getPermission = rankClass.getMethod("getPermission", String.class);
            Method asBooleanOrFalse = permValueClass.getMethod("asBooleanOrFalse");

            for (Object rank : ranks) {
                Object permValue = getPermission.invoke(rank, permissionNode);
                if (permValue == null) {
                    continue;
                }
                boolean allowed = (Boolean) asBooleanOrFalse.invoke(permValue);
                if (allowed) {
                    return true;
                }
            }

            return false;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
