package net.phoenix.core.integration.jade.provider;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.phoenix.core.common.machine.multiblock.electric.TeslaTowerMachine;
import net.phoenix.core.common.machine.multiblock.part.special.TeslaEnergyHatchPartMachine;
import net.phoenix.core.phoenixcore;
import net.phoenix.core.saveddata.TeslaTeamEnergyData;
import net.phoenix.core.utils.TeamUtils;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.UUID;

public class TeslaNetworkProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final ResourceLocation UID = phoenixcore.id("tesla_network_info");

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof MetaMachineBlockEntity metaBE) {
            UUID team = null;
            BlockPos pos = accessor.getPosition();
            long transferRate = 0;
            int mode = -1; // 0 = Providing (Uplink), 1 = Taking (Downlink/Machine)

            if (accessor.getLevel() instanceof ServerLevel sl) {
                // --- CROSS-DIMENSION FIX ---
                // We always pull data from the Overworld where the TeslaTeamEnergyData is centralized.
                MinecraftServer server = sl.getServer();
                ServerLevel overworld = server.getLevel(Level.OVERWORLD);
                if (overworld == null) return;

                TeslaTeamEnergyData data = TeslaTeamEnergyData.get(overworld);

                // 1. Identify Endpoint and Perspective
                if (metaBE.getMetaMachine() instanceof TeslaEnergyHatchPartMachine hatch) {
                    team = hatch.getOwnerTeamUUID();
                    if (team != null) {
                        mode = hatch.isUplink() ? 0 : 1;
                        TeslaTeamEnergyData.TeamEnergy teamData = data.getOrCreate(team);
                        transferRate = teamData.machineDisplayFlow.getOrDefault(pos, 0L);
                    }
                } else if (metaBE.getMetaMachine() instanceof TeslaTowerMachine tower) {
                    team = tower.getOwnerUUID();
                } else {
                    // Reverse lookup for Soul-Linked (Single) Machines
                    // This works across dimensions because we are checking the global data maps
                    for (var entry : data.getNetworksView().entrySet()) {
                        if (entry.getValue().soulLinkedMachines.contains(pos)) {
                            team = entry.getKey();
                            mode = 1;
                            transferRate = entry.getValue().machineDisplayFlow.getOrDefault(pos, 0L);
                            break;
                        }
                    }
                }

                // 2. Wrap Data for Client
                if (team != null) {
                    TeslaTeamEnergyData.TeamEnergy teamData = data.getOrCreate(team);
                    tag.putUUID("TeslaTeam", team);
                    tag.putString("TeamName", TeamUtils.getTeamName(team));
                    tag.putString("Stored", FormattingUtil.formatNumbers(teamData.stored));
                    tag.putString("Capacity", FormattingUtil.formatNumbers(teamData.capacity));
                    tag.putLong("LocalTransfer", transferRate);
                    tag.putInt("TransferMode", mode);

                    // Use sl.getGameTime() for local connection checks
                    tag.putInt("ActiveHatches", teamData.getLiveHatchCount(sl.getGameTime()));
                }
            }
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains("TeslaTeam")) return;

        // Header
        tooltip.add(Component.literal("Network: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(data.getString("TeamName")).withStyle(ChatFormatting.AQUA)));

        // Cloud Buffer Info
        String stored = data.getString("Stored");
        String capacity = data.getString("Capacity");
        tooltip.add(Component.literal("Cloud: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(stored).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" / " + capacity + " EU").withStyle(ChatFormatting.YELLOW)));

        // Connections Count
        tooltip.add(Component.literal("Connections: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(data.getInt("ActiveHatches")))
                        .withStyle(ChatFormatting.WHITE)));

        // I/O Information (Fixed color bleeding)
        if (data.contains("TransferMode") && data.getInt("TransferMode") != -1) {
            long rate = data.getLong("LocalTransfer");
            int mode = data.getInt("TransferMode");

            MutableComponent label = (mode == 0) ?
                    Component.literal("Providing: ") :
                    Component.literal("Taking: ");

            ChatFormatting color = (mode == 0) ? ChatFormatting.GREEN : ChatFormatting.RED;

            tooltip.add(label.withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(FormattingUtil.formatNumbers(rate) + " EU/t").withStyle(color)));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
