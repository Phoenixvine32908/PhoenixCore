package net.phoenix.core.integration.jade.provider;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
            TeslaEnergyHatchPartMachine hatch = null;

            if (metaBE.getMetaMachine() instanceof TeslaEnergyHatchPartMachine tHatch) {
                hatch = tHatch;
                team = hatch.getOwnerTeamUUID();
            } else if (metaBE.getMetaMachine() instanceof TeslaTowerMachine tTower) {
                team = tTower.getOwnerUUID();
            }

            if (team != null && accessor.getLevel() instanceof ServerLevel sl) {
                TeslaTeamEnergyData data = TeslaTeamEnergyData.get(sl);
                TeslaTeamEnergyData.TeamEnergy teamData = data.getOrCreate(team);

                tag.putUUID("TeslaTeam", team);
                tag.putString("TeamName", TeamUtils.getTeamName(team));
                tag.putString("Stored", FormattingUtil.formatNumbers(teamData.stored));
                tag.putString("Capacity", FormattingUtil.formatNumbers(teamData.capacity));
                tag.putInt("ActiveHatches", teamData.getLiveHatchCount(sl.getGameTime()));

                if (hatch != null) {
                    // Logic fix: Ensure we capture transfer even if it's currently 'pushing'
                    long transfer = hatch.getLastTransferAmount();

                    // If the hatch reports 0 but we know the team data is moving energy,
                    // we pull the value directly from the team's live map as a fallback
                    if (transfer == 0) {
                        transfer = teamData.energyInput.getOrDefault(hatch.getPos(), java.math.BigInteger.ZERO)
                                .longValue();
                        if (transfer == 0) {
                            transfer = teamData.energyOutput.getOrDefault(hatch.getPos(), java.math.BigInteger.ZERO)
                                    .longValue();
                        }
                    }

                    tag.putLong("LocalTransfer", transfer);
                    tag.putBoolean("IsInputHatch", hatch.getIO() == IO.IN);
                }
            }
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains("TeslaTeam")) return;

        // Header
        tooltip.add(Component.translatable("jade.phoenixcore.tesla_team", data.getString("TeamName"))
                .withStyle(ChatFormatting.AQUA));

        // Cloud Storage
        tooltip.add(Component.literal("Cloud: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(data.getString("Stored")).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" / ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(data.getString("Capacity") + " EU").withStyle(ChatFormatting.YELLOW)));

        // Connections
        tooltip.add(Component.literal("Active Connections: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(data.getInt("ActiveHatches")))
                        .withStyle(ChatFormatting.WHITE)));

        // Transfer Rate Logic
        if (data.contains("LocalTransfer")) {
            long rate = data.getLong("LocalTransfer");
            boolean isInput = data.getBoolean("IsInputHatch");

            // Corrected meaning:
            // IO.IN = pulling from tower = providing to machine
            // IO.OUT = pushing to tower = charging network
            boolean isChargingNetwork = !isInput;

            String label = isChargingNetwork ? "Charging Network: " : "Providing to Machine: ";
            ChatFormatting color = isChargingNetwork ? ChatFormatting.GREEN : ChatFormatting.RED;

            tooltip.add(Component.literal(label).withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(FormattingUtil.formatNumbers(rate) + " EU/t").withStyle(color)));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
