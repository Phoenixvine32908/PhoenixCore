package net.phoenix.core.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IHPCACoolantProvider;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.hpca.HPCAComponentPartMachine;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.phoenix.core.configs.PhoenixConfigs;

import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PhoenixCoolerPartMachine extends HPCAComponentPartMachine implements IHPCACoolantProvider {

    @Getter
    private final boolean advanced;

    public PhoenixCoolerPartMachine(IMachineBlockEntity holder, boolean advanced) {
        super(holder);
        this.advanced = advanced;
    }

    @Override
    public ResourceTexture getComponentIcon() {
        return advanced ? GuiTextures.HPCA_ICON_ACTIVE_COOLER_COMPONENT : GuiTextures.HPCA_ICON_HEAT_SINK_COMPONENT;
    }

    @Override
    public int getUpkeepEUt() {
        return advanced ? GTValues.VA[PhoenixConfigs.INSTANCE.features.ActiveCoolerEutUpkeep] :
                GTValues.VA[PhoenixConfigs.INSTANCE.features.HeatSinkEutUpkeep];
    }

    @Override
    public boolean canBeDamaged() {
        return false;
    }

    @Override
    public int getCoolingAmount() {
        return advanced ? PhoenixConfigs.INSTANCE.features.ActiveCoolerStrength :
                PhoenixConfigs.INSTANCE.features.HeatSinkStrength;
    }

    @Override
    public boolean isActiveCooler() {
        return advanced;
    }

    @Override
    public int getMaxCoolantPerTick() {
        return advanced ? PhoenixConfigs.INSTANCE.features.ActiveCoolerCoolantUse : 0;
    }
}
