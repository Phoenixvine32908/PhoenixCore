//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.compat;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.placeholder.MultiLineComponent;
import com.gregtechceu.gtceu.api.placeholder.Placeholder;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderContext;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderHandler;
import com.gregtechceu.gtceu.api.placeholder.exceptions.NotSupportedException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.PlaceholderException;
import fi.dea.mc.deafission.api.IReactorApi;
import fi.dea.mc.deafission.api.ReactorMetrics;
import fi.dea.mc.deafission.common.data.machine.FissionReactorMachine;
import java.util.List;
import java.util.function.Function;
import net.minecraft.network.chat.Component;

public class FissionGtPlaceholders {
    public static void init() {
        addMetric("fissionCoolantUse", (m) -> new MultiLineComponent(List.of(Component.m_237113_(String.valueOf(m != null ? m.CoolantUse() : (double)0.0F)))));
        addMetric("fissionCoolantPercent", (m) -> new MultiLineComponent(List.of(Component.m_237113_(String.valueOf(m != null ? m.CoolantPercent() : (double)0.0F)))));
        addMetric("fissionFuelDamage", (m) -> new MultiLineComponent(List.of(Component.m_237113_(String.valueOf(m != null ? m.FuelDamage() : (double)0.0F)))));
        addMetric("fissionCooling", (m) -> new MultiLineComponent(List.of(Component.m_237113_(String.valueOf(m != null ? m.Cooling() : (double)0.0F)))));
        addMetric("fissionHeating", (m) -> new MultiLineComponent(List.of(Component.m_237113_(String.valueOf(m != null ? m.Heating() : (double)0.0F)))));
        addMetric("fissionProcessing", (m) -> new MultiLineComponent(List.of(Component.m_237113_(String.valueOf(m != null ? m.Processing() : (double)0.0F)))));
        addMetric("fissionMinHeat", (m) -> new MultiLineComponent(List.of(Component.m_237113_(String.valueOf(m != null ? m.MinHeat() : (double)0.0F)))));
        addMetric("fissionMaxHeat", (m) -> new MultiLineComponent(List.of(Component.m_237113_(String.valueOf(m != null ? m.MaxHeat() : (double)0.0F)))));
        addMetric("fissionThermalMass", (m) -> new MultiLineComponent(List.of(Component.m_237113_(String.valueOf(m != null ? m.ThermalMass() : (double)-1.0F)))));
        addMetric("fissionThrottle", (m) -> new MultiLineComponent(List.of(Component.m_237113_(String.valueOf(m != null ? m.Throttle() : (double)0.0F)))));
        addMetric("fissionHeat", (m) -> new MultiLineComponent(List.of(Component.m_237113_(String.valueOf(m != null ? m.HeatPost() : (double)0.0F)))));
        addMetric("fissionRecipe", (m) -> new MultiLineComponent(List.of(Component.m_237113_(m != null && m.Recipe() != null ? m.Recipe().toString() : ""))));
        addMetric("fissionMode", (m) -> new MultiLineComponent(List.of(Component.m_237113_(String.valueOf(m != null ? m.Mode().toString() : "N/A")))));
    }

    private static void addApi(String id, final Function<IReactorApi, MultiLineComponent> impl) {
        PlaceholderHandler.addPlaceholder(new Placeholder(id) {
            public MultiLineComponent apply(PlaceholderContext ctx, List<MultiLineComponent> args) throws PlaceholderException {
                MetaMachine var4 = MetaMachine.getMachine(ctx.level(), ctx.pos());
                if (var4 instanceof FissionReactorMachine reactor) {
                    IReactorApi var5 = reactor.getApi();
                    return (MultiLineComponent)impl.apply(var5);
                } else {
                    throw new NotSupportedException();
                }
            }
        });
    }

    private static void addMetric(String id, final Function<ReactorMetrics, MultiLineComponent> impl) {
        PlaceholderHandler.addPlaceholder(new Placeholder(id) {
            public MultiLineComponent apply(PlaceholderContext ctx, List<MultiLineComponent> args) throws PlaceholderException {
                MetaMachine var4 = MetaMachine.getMachine(ctx.level(), ctx.pos());
                if (var4 instanceof FissionReactorMachine reactor) {
                    IReactorApi var5 = reactor.getApi();
                    return (MultiLineComponent)impl.apply(var5.getMetrics());
                } else {
                    throw new NotSupportedException();
                }
            }
        });
    }
}
