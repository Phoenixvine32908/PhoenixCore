//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.core;

import fi.dea.mc.deafission.api.ReactorFuel;
import fi.dea.mc.deafission.core.components.ComponentTotals;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record ReactorCurveState(double Heat, @Unmodifiable List<@Nullable ReactorFuel> RecipeFuels, int ColdCoolantAmount, int HotCoolantAmount, int ColdCoolantTankSize, int HotCoolantTankSize, ComponentTotals components, @Nullable ReactorCoolantRecipe recipe) {
}
