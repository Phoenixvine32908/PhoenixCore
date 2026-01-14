package net.phoenix.core.utils;

import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.minecraft.server.level.ServerPlayer;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public final class TeamUtils {

    private TeamUtils() {}

    /**
     * Returns the FTB Team ID for the given player.
     * If the player is not in a team, falls back to the player's own UUID.
     */
    public static UUID getTeamIdOrPlayerFallback(ServerPlayer player) {
        return getTeam(player)
                .map(Team::getTeamId)
                .orElse(player.getUUID());
    }

    @Nullable
    public static UUID getTeamUUID(@Nullable MachineOwner owner) {
        if (owner == null) return null;

        // Extract the UUID from the MachineOwner
        UUID ownerUUID = owner.getUUID(); // adjust if your MachineOwner uses a different method
        if (ownerUUID == null) return null;

        // Look up the team via FTB Teams API
        return FTBTeamsAPI.api().getManager().getTeamForPlayerID(ownerUUID)
                .map(Team::getTeamId)
                .orElse(ownerUUID);
    }

    public static String getTeamName(UUID teamId) {
        var api = FTBTeamsAPI.api();
        var manager = api.getManager();

        return manager.getTeamByID(teamId)
                .map(team -> team.getName().getString()) // correct getter for FTB Teams 2
                .orElse(teamId.toString().substring(0, 8));
    }

    public static Optional<Team> getTeam(ServerPlayer player) {
        return FTBTeamsAPI.api()
                .getManager()
                .getTeamForPlayer(player);
    }
}
