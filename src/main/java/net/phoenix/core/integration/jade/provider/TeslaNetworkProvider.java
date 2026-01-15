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
            TeslaTowerMachine tower = null;

            if (metaBE.getMetaMachine() instanceof TeslaEnergyHatchPartMachine tHatch) {
                hatch = tHatch;
                team = hatch.getOwnerTeamUUID();
            } else if (metaBE.getMetaMachine() instanceof TeslaTowerMachine tTower) {
                tower = tTower;
                team = tower.getOwnerUUID();
            }

            if (team != null && accessor.getLevel() instanceof ServerLevel sl) {
                TeslaTeamEnergyData.TeamEnergy teamData = TeslaTeamEnergyData.get(sl).getOrCreate(team);
                tag.putUUID("TeslaTeam", team);
                tag.putString("TeamName", TeamUtils.getTeamName(team));
                tag.putString("Stored", FormattingUtil.formatNumbers(teamData.stored));
                tag.putString("Capacity", FormattingUtil.formatNumbers(teamData.capacity));

                // Add live hatch count for both Hatch and Tower views
                tag.putInt("ActiveHatches", teamData.getLiveHatchCount(sl.getGameTime()));

                if (hatch != null) {
                    tag.putLong("LocalTransfer", hatch.getLastTransferAmount());
                    tag.putBoolean("IsInputHatch", hatch.getIO() == IO.IN);
                }
            }
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains("TeslaTeam")) return;

        tooltip.add(Component.translatable("jade.phoenixcore.tesla_team", data.getString("TeamName"))
                .withStyle(ChatFormatting.AQUA));

        tooltip.add(Component.literal("Cloud: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(data.getString("Stored")).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" / ")
                        .append(Component.literal(data.getString("Capacity")).withStyle(ChatFormatting.GOLD))));

        // Show active connections count
        tooltip.add(Component.literal("Active Connections: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(data.getInt("ActiveHatches"))).withStyle(ChatFormatting.WHITE)));

        if (data.contains("LocalTransfer")) {
            long rate = data.getLong("LocalTransfer");
            boolean isInput = data.getBoolean("IsInputHatch");

            String label = isInput ? "Receiving: " : "Providing: ";
            ChatFormatting color = isInput ? ChatFormatting.RED : ChatFormatting.GREEN;

            tooltip.add(Component.literal(label).withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(FormattingUtil.formatNumbers(rate) + " EU/t").withStyle(color)));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
