package com.example.chatchannels.server.perm;

import net.minecraft.server.level.ServerPlayer;

public interface PermissionProvider {

    boolean hasPermission(ServerPlayer player, String permissionNode);
}
