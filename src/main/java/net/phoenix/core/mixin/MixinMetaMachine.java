package net.phoenix.core.mixin;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import net.minecraft.server.level.ServerLevel;
import net.phoenix.core.common.data.item.TeslaBinderItem;
import net.phoenix.core.saveddata.TeslaTeamEnergyData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MetaMachine.class, remap = false)
public abstract class MixinMetaMachine implements TeslaBinderItem.ITeslaFlowTracker {
    @Unique private long phoenix$lastEnergy = -1L;
    @Unique private long phoenix$currentFlow = 0L;
    @Unique private int phoenix$staleTicks = 0;

    @Override
    public long phoenixCore$getTeslaFlow() {
        return (phoenix$staleTicks > 40) ? 0L : this.phoenix$currentFlow;
    }



    @Inject(method = "serverTick", at = @At("HEAD"))
    private void phoenix$calculateFlow(CallbackInfo ci) {
        if ((Object) this instanceof TieredEnergyMachine tiered) {
            if (tiered.energyContainer == null) return;

            long current = tiered.energyContainer.getEnergyStored();
            if (phoenix$lastEnergy == -1L) {
                phoenix$lastEnergy = current;
                return;
            }

            long delta = current - phoenix$lastEnergy;
            if (delta != 0) {
                this.phoenix$currentFlow = delta;
                this.phoenix$staleTicks = 0;
            } else {
                this.phoenix$staleTicks++;
            }
            this.phoenix$lastEnergy = current;
        }
    }

    @Unique @Override
    public void phoenixCore$resetTeslaFlow() {
        this.phoenix$currentFlow = 0L;
        this.phoenix$staleTicks = 100;
    }
}