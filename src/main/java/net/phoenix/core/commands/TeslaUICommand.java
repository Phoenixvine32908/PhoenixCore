package net.phoenix.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix.core.saveddata.TeslaTeamEnergyData;

import java.util.UUID;

@Mod.EventBusSubscriber
public class TeslaUICommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("tesla_energy_debug")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> run(ctx.getSource()))
        );
    }

    private static int run(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        TeslaTeamEnergyData data = TeslaTeamEnergyData.get(level);

        source.sendSuccess(() -> Component.literal("=== Tesla Team Energy Data ==="), false);

        var networks = data.getNetworksView();

        if (networks.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No networks found."), false);
            return 1;
        }

        for (var entry : networks.entrySet()) {
            UUID team = entry.getKey();
            var e = entry.getValue();

            boolean online = data.isOnline(team);

            source.sendSuccess(() ->
                    Component.literal("Team: " + team + " (" + (online ? "ONLINE" : "OFFLINE") + ")"), false);

            source.sendSuccess(() ->
                    Component.literal("  Stored: " + e.stored), false);

            source.sendSuccess(() ->
                    Component.literal("  Capacity: " + e.capacity), false);

            source.sendSuccess(() ->
                    Component.literal("  Hatches: " + data.getHatchCount(team)), false);
        }

        return 1;
    }
}
