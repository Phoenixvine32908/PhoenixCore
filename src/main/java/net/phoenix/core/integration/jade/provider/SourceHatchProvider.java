package net.phoenix.core.integration.jade.provider;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.core.common.machine.multiblock.part.special.SourceHatchPartMachine;
import net.phoenix.core.phoenixcore;

import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class SourceHatchProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().contains("sourceStored")) {
            int storedSource = blockAccessor.getServerData().getInt("sourceStored");
            int maxCapacity = blockAccessor.getServerData().getInt("sourceCapacity");
            Component sourceText = Component.translatable("phoenixcore.jade.source_hatch_info",
                    Component.literal(String.valueOf(storedSource)).withStyle(ChatFormatting.AQUA),
                    Component.literal(String.valueOf(maxCapacity)).withStyle(ChatFormatting.AQUA));
            iTooltip.add(sourceText);
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof MetaMachineBlockEntity blockEntity) {
            if (blockEntity.getMetaMachine() instanceof SourceHatchPartMachine sourceHatch) {
                // Corrected: The variable type must be ISourceTile
                ISourceTile sourceContainer = sourceHatch.getSourceContainer();

                compoundTag.putInt("sourceStored", sourceContainer.getSource());
                compoundTag.putInt("sourceCapacity", sourceContainer.getMaxSource());
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return phoenixcore.id("source_hatch_info");
    }
}
