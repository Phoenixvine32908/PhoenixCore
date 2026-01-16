package net.phoenix.core.commands;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix.core.saveddata.TeslaTeamEnergyData;

import com.mojang.brigadier.CommandDispatcher;

import java.util.UUID;

@Mod.EventBusSubscriber
public class TeslaDebugCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("tesla_energy_debug")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> run(ctx.getSource())));
    }

    private static int run(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        TeslaTeamEnergyData data = TeslaTeamEnergyData.get(level);

        source.sendSuccess(() -> Component.literal("=== Tesla Network Debugger ===").withStyle(ChatFormatting.GOLD,
                ChatFormatting.BOLD), false);

        var networks = data.getNetworksView();

        if (networks.isEmpty()) {
            source.sendSuccess(
                    () -> Component.literal("No active networks found in SavedData.").withStyle(ChatFormatting.RED),
                    false);
            return 1;
        }

        for (var entry : networks.entrySet()) {
            UUID team = entry.getKey();
            TeslaTeamEnergyData.TeamEnergy teamData = entry.getValue();
            boolean online = data.isOnline(team);

            // Team Header
            MutableComponent teamInfo = Component.literal("Network: ")
                    .append(Component.literal(team.toString().substring(0, 8)).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" Status: ").withStyle(ChatFormatting.WHITE))
                    .append(online ? Component.literal("ONLINE").withStyle(ChatFormatting.GREEN) :
                            Component.literal("OFFLINE").withStyle(ChatFormatting.RED));

            source.sendSuccess(() -> teamInfo, false);

            // Energy Stats
            source.sendSuccess(() -> Component.literal("  Power: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(teamData.stored.toString()).withStyle(ChatFormatting.YELLOW))
                    .append(" / ")
                    .append(Component.literal(teamData.capacity.toString()).withStyle(ChatFormatting.GOLD))
                    .append(" EU"), false);

            // Detailed Hatch Breakdown
            var hatches = data.getHatches(team);
            source.sendSuccess(() -> Component.literal("  Hatch Details (Count: " + hatches.size() + "):")
                    .withStyle(ChatFormatting.GRAY), false);

            if (hatches.isEmpty()) {
                source.sendSuccess(() -> Component.literal("    None registered.").withStyle(ChatFormatting.DARK_GRAY),
                        false);
            } else {
                for (TeslaTeamEnergyData.HatchInfo hatch : hatches) {
                    boolean isOut = hatch.isPhysicalOutput;
                    MutableComponent hatchLine = Component.literal("    - ")
                            .append(Component.literal(isOut ? "[OUT] " : "[IN] ")
                                    .withStyle(isOut ? ChatFormatting.RED : ChatFormatting.GREEN))
                            .append(Component
                                    .literal(hatch.pos.getX() + ", " + hatch.pos.getY() + ", " + hatch.pos.getZ())
                                    .withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(" | Buf: ").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(hatch.buffered.toString()).withStyle(ChatFormatting.DARK_AQUA));

                    source.sendSuccess(() -> hatchLine, false);
                }
            }
            source.sendSuccess(() -> Component.literal("--------------------------------"), false);
        }

        return 1;
    }
}
