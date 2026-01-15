package net.phoenix.core.integration.jade.provider;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
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
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains("TeslaTeam")) return;

        String teamName = data.getString("TeamName");
        String stored = data.getString("Stored");
        String capacity = data.getString("Capacity");

        tooltip.add(Component.translatable("jade.phoenixcore.tesla_team", teamName).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("jade.phoenixcore.tesla_stored")
                .append(Component.literal(stored).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" / ")
                        .append(Component.literal(capacity).withStyle(ChatFormatting.GOLD))));

        if (data.contains("InputPerSec")) {
            tooltip.add(Component.literal("In: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(data.getLong("InputPerSec") + " EU/s").withStyle(ChatFormatting.GREEN)));
            tooltip.add(Component.literal("Out: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(data.getLong("OutputPerSec") + " EU/s").withStyle(ChatFormatting.RED)));
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof MetaMachineBlockEntity metaBE) {
            UUID teamUUID = null;
            long in = 0, out = 0;

            if (metaBE.getMetaMachine() instanceof TeslaTowerMachine tower) {
                teamUUID = tower.getOwnerUUID();
                in = tower.getInputPerSec();
                out = tower.getOutputPerSec();
            } else if (metaBE.getMetaMachine() instanceof TeslaEnergyHatchPartMachine hatch) {
                teamUUID = hatch.getOwnerTeamUUID();
            }

            if (teamUUID != null && accessor.getLevel() instanceof ServerLevel sl) {
                TeslaTeamEnergyData.TeamEnergy teamData = TeslaTeamEnergyData.get(sl).getOrCreate(teamUUID);
                tag.putUUID("TeslaTeam", teamUUID);
                tag.putString("TeamName", TeamUtils.getTeamName(teamUUID));
                tag.putString("Stored", FormattingUtil.formatNumbers(teamData.stored));
                tag.putString("Capacity", FormattingUtil.formatNumbers(teamData.capacity));
                tag.putLong("InputPerSec", in);
                tag.putLong("OutputPerSec", out);
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}