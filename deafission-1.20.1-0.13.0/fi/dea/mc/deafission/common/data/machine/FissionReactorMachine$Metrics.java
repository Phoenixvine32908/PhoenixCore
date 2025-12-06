//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data.machine;

import fi.dea.mc.deafission.common.data.machine.FissionReactorMachine.OperatingMode;
import fi.dea.mc.deafission.core.components.ComponentTotals;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

class FissionReactorMachine$Metrics {
    public double LastCoolantUse;
    public double LastFuelDamage;
    public double LastCooling;
    public double LastCoolingPercent;
    public double LastHeating;
    public double LastProcessing;
    public double LastMinHeat;
    public double LastMaxHeat;
    public double LastMass;
    public double LastHeat;
    public double LastThrottle;
    public @Nullable ResourceLocation LastRecipe;
    public @Nullable ComponentTotals LastComponents;
    public FissionReactorMachine.OperatingMode LastMode;

    private FissionReactorMachine$Metrics() {
        this.LastMode = OperatingMode.DEFAULT;
    }

    public void reset() {
        this.LastCoolantUse = (double)0.0F;
        this.LastFuelDamage = (double)0.0F;
        this.LastCooling = (double)0.0F;
        this.LastCoolingPercent = (double)0.0F;
        this.LastHeating = (double)0.0F;
        this.LastProcessing = (double)0.0F;
        this.LastMinHeat = (double)0.0F;
        this.LastMaxHeat = (double)0.0F;
        this.LastMass = (double)0.0F;
        this.LastHeat = (double)0.0F;
        this.LastThrottle = (double)0.0F;
        this.LastRecipe = null;
        this.LastComponents = null;
        this.LastMode = OperatingMode.DEFAULT;
    }

    public List<String> render() {
        ArrayList<String> list = new ArrayList(7);
        list.add(String.format("Mode: %s", this.LastMode));
        if (this.LastComponents != null) {
            list.add(String.format("%s H %s Th %s Eff", this.LastComponents.heat(), this.LastComponents.throttle(), this.LastComponents.efficiency()));
        } else {
            list.add("INVALID");
        }

        String[] r = this.LastRecipe != null ? this.LastRecipe.toString().split("/") : null;
        list.add(String.format("Recipe: %s", r != null ? r[r.length - 1] : null));
        if (this.LastCoolingPercent != (double)1.0F) {
            long p = Math.round(this.LastCoolingPercent * (double)100.0F);
            list.add(String.format("Coolant: %.3f mB/t (had %s%%)", this.LastCoolantUse, p));
        } else {
            list.add(String.format("Coolant: %.3f mB/t", this.LastCoolantUse));
        }

        long p = Math.round(this.LastThrottle * (double)100.0F);
        list.add(String.format("Fuel: %.3f dmg @ %s%%", this.LastFuelDamage, p));
        list.add(String.format("Cooling: %.3f HU/t", this.LastCooling));
        list.add(String.format("Heating: %.3f HU/t", this.LastHeating));
        list.add(String.format("Processing: %.3f HU/t", this.LastProcessing));
        return list;
    }
}
