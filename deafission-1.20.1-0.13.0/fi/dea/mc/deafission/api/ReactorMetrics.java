//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.api;

import fi.dea.mc.deafission.common.data.machine.FissionReactorMachine;
import fi.dea.mc.deafission.core.components.ComponentTotals;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ReactorMetrics(double CoolantUse, double CoolantPercent, double FuelDamage, double Cooling, double Heating, double Processing, double MinHeat, double MaxHeat, double ThermalMass, double Throttle, double HeatPre, double HeatPost, @Nullable ResourceLocation Recipe, @Nullable ComponentTotals Components, FissionReactorMachine.OperatingMode Mode) {
}
