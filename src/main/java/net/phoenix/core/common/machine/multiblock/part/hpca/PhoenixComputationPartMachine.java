package net.phoenix.core.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IHPCAComputationProvider;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.hpca.HPCAComputationPartMachine;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.phoenix.core.configs.PhoenixConfigs;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A custom HPCA Computation Part for the Phoenix mod.
 * This part is designed to be a high-end component, offering
 * increased computation power (CWU) but also requiring more
 * energy and cooling.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PhoenixComputationPartMachine extends HPCAComputationPartMachine implements IHPCAComputationProvider {

    public PhoenixComputationPartMachine(IMachineBlockEntity holder) {
        super(holder, true);
    }

    @Override
    public ResourceTexture getComponentIcon() {
        if (isDamaged()) {

            return GuiTextures.HPCA_ICON_DAMAGED_COMPUTATION_COMPONENT;
        }

        return GuiTextures.HPCA_ICON_ADVANCED_COMPUTATION_COMPONENT;
    }

    @Override
    public int getUpkeepEUt() {
        return GTValues.VA[PhoenixConfigs.INSTANCE.features.PCUEutUpkeep];
    }

    @Override
    public int getMaxEUt() {
        return GTValues.VA[PhoenixConfigs.INSTANCE.features.PCUMaxEUt];
    }

    @Override
    public int getCWUPerTick() {
        if (isDamaged()) return PhoenixConfigs.INSTANCE.features.damagedPCUStrength;
        return PhoenixConfigs.INSTANCE.features.PCUStrength;
    }

    @Override
    public int getCoolingPerTick() {
        return PhoenixConfigs.INSTANCE.features.PCUCoolantUsed;
    }
}
