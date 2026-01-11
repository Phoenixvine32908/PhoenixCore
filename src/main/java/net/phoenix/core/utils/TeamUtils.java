package net.phoenix.core.utils;

import net.minecraft.server.level.ServerPlayer;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;

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
