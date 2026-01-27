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

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BasicPhoenixComputationPartMachine extends HPCAComputationPartMachine implements IHPCAComputationProvider {

    public BasicPhoenixComputationPartMachine(IMachineBlockEntity holder) {
        super(holder, false);
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
        return GTValues.VA[PhoenixConfigs.INSTANCE.features.basicPCUEutUpkeep];
    }

    @Override
    public int getMaxEUt() {
        return GTValues.VA[PhoenixConfigs.INSTANCE.features.basicPCUMaxEUt];
    }

    @Override
    public int getCWUPerTick() {
        if (isDamaged()) return PhoenixConfigs.INSTANCE.features.damagedBasicPCUStrength;
        return PhoenixConfigs.INSTANCE.features.BasicPCUStrength;
    }

    @Override
    public int getCoolingPerTick() {
        return PhoenixConfigs.INSTANCE.features.BasicPCUCoolantUsed;
    }
}
