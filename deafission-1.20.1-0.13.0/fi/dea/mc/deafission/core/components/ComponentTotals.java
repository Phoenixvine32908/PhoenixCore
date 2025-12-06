//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.core.components;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ComponentTotals(double heat, double efficiency, double throttle) {
    public static ComponentTotals Zero = new ComponentTotals((double)0.0F, (double)0.0F, (double)0.0F);

    public ComponentTotals add(@Nullable HeatComponent comp) {
        return comp == null ? this : new ComponentTotals(this.heat + comp.value(), this.efficiency, this.throttle);
    }

    public ComponentTotals add(@Nullable EfficiencyComponent comp) {
        return comp == null ? this : new ComponentTotals(this.heat, this.efficiency + comp.value(), this.throttle);
    }

    public ComponentTotals add(@Nullable ThrottleComponent comp) {
        return comp == null ? this : new ComponentTotals(this.heat, this.efficiency, this.throttle + comp.value());
    }

    public ComponentTotals add(IReactorComponent c) {
        if (c instanceof HeatComponent ch) {
            return this.add(ch);
        } else if (c instanceof EfficiencyComponent eh) {
            return this.add(eh);
        } else if (c instanceof ThrottleComponent th) {
            return this.add(th);
        } else {
            throw new IllegalArgumentException(c.getComponentType().toString());
        }
    }
}
