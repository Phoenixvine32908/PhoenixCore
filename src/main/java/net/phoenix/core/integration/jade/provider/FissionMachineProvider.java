package net.phoenix.core.integration.jade.provider;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.core.phoenixcore;
import net.phoenix.core.common.machine.multiblock.FissionWorkableElectricMultiblockMachine;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class FissionMachineProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final ResourceLocation UID = phoenixcore.id("fission_machine_info");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof MetaMachineBlockEntity metaBE &&
                metaBE.getMetaMachine() instanceof FissionWorkableElectricMultiblockMachine)) {
            return;
        }

        CompoundTag data = accessor.getServerData();
        boolean hasMeltdownTimer = data.contains("meltdownTimerTicks");
        int ticks = data.getInt("meltdownTimerTicks");
        boolean hasCoolant = data.getBoolean("hasCoolant");
        boolean lowCooling = data.getBoolean("lowCooling");

        if (!hasMeltdownTimer || ticks < 0) {
            tooltip.add(Component.translatable("jade.phoenixcore.fission_safe")
                    .withStyle(s -> s.withColor(0x33FF33)));
            return;
        }

        int seconds = ticks / 20;
        tooltip.add(Component.translatable("jade.phoenixcore.fission_meltdown_timer", seconds)
                .withStyle(s -> s.withColor(0xFFAA00)));

        if (!hasCoolant) {
            tooltip.add(Component.translatable("jade.phoenixcore.fission_no_coolant")
                    .withStyle(s -> s.withColor(0xFF3333)));
        } else if (lowCooling) {
            tooltip.add(Component.translatable("jade.phoenixcore.fission_low_cooling")
                    .withStyle(s -> s.withColor(0xFF5555)));
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof MetaMachineBlockEntity metaBE &&
                metaBE.getMetaMachine() instanceof FissionWorkableElectricMultiblockMachine machine)) {
            return;
        }

        int secondsRemaining = machine.getMeltdownSecondsRemaining();

        tag.putInt("meltdownTimerTicks", secondsRemaining > 0 ? secondsRemaining * 20 : -1);
        tag.putBoolean("hasCoolant", machine.lastHasCoolant);
        tag.putBoolean("lowCooling", machine.lastProvidedCooling < machine.lastRequiredCooling);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
