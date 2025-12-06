//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.compat;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import dan200.computercraft.api.ForgeComputerCraftAPI;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import fi.dea.mc.deafission.api.ReactorFuel;
import fi.dea.mc.deafission.api.ReactorMetrics;
import fi.dea.mc.deafission.common.data.machine.FissionReactorMachine;
import fi.dea.mc.deafission.core.components.ComponentTotals;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

public class FissionCcReactorPeripheral implements IPeripheral {
    private final FissionReactorMachine _reactor;

    public FissionCcReactorPeripheral(FissionReactorMachine reactor) {
        this._reactor = reactor;
    }

    public String getType() {
        return "fission_reactor";
    }

    public boolean equals(@Nullable IPeripheral iPeripheral) {
        boolean var10000;
        if (iPeripheral instanceof FissionCcReactorPeripheral rp) {
            if (rp._reactor == this._reactor) {
                var10000 = true;
                return var10000;
            }
        }

        var10000 = false;
        return var10000;
    }

    @LuaFunction(
        mainThread = false
    )
    public long getVersion() {
        return 1L;
    }

    @LuaFunction(
        mainThread = false
    )
    public @Nullable Map<String, Object> getMetrics() {
        ReactorMetrics m = this._reactor.getApi().getMetrics();
        if (m == null) {
            return null;
        } else {
            ComponentTotals c = m.Components();
            Map<String, Double> comps;
            if (c != null) {
                comps = new LinkedHashMap(3);
                comps.put("heat", c.heat());
                comps.put("throttle", c.throttle());
                comps.put("efficiency", c.efficiency());
            } else {
                comps = null;
            }

            LinkedHashMap<String, Object> d = new LinkedHashMap(9);
            d.put("coolantUse", m.CoolantUse());
            d.put("coolantPercent", m.CoolantPercent());
            d.put("fuelDamage", m.FuelDamage());
            d.put("cooling", m.Cooling());
            d.put("heating", m.Heating());
            d.put("processing", m.Processing());
            d.put("minHeat", m.MinHeat());
            d.put("maxHeat", m.MaxHeat());
            d.put("thermalMass", m.ThermalMass());
            d.put("throttle", m.Throttle());
            d.put("heatPre", m.HeatPre());
            d.put("heatPost", m.HeatPost());
            d.put("recipe", m.Recipe() != null ? m.Recipe().toString() : null);
            d.put("components", comps);
            d.put("mode", m.Mode().toString());
            return d;
        }
    }

    @LuaFunction(
        mainThread = false
    )
    @Nullable
    public List<Map<String, Object>> getFuels() {
        List<ReactorFuel> fuels = this._reactor.getApi().getFuels();
        if (fuels == null) {
            return null;
        } else {
            ArrayList<Map<String, Object>> list = new ArrayList(fuels.size());

            for(ReactorFuel f : fuels) {
                if (f == null) {
                    list.add((Object)null);
                } else {
                    LinkedHashMap<String, Object> elem = new LinkedHashMap(3);
                    elem.put("id", f.id());
                    elem.put("damage", f.damage());
                    elem.put("maxDamage", f.maxDamage());
                    elem.put("heat", f.heat());
                    elem.put("rods", f.rods());
                    list.add(elem);
                }
            }

            return list;
        }
    }

    public static void register() {
        ForgeComputerCraftAPI.registerPeripheralProvider(new Provider());
    }

    private static class Provider implements IPeripheralProvider {
        public LazyOptional<IPeripheral> getPeripheral(Level world, BlockPos pos, Direction side) {
            BlockEntity block = world.m_7702_(pos);
            if (block instanceof MetaMachineBlockEntity meta) {
                MetaMachine var7 = meta.getMetaMachine();
                if (var7 instanceof FissionReactorMachine reactor) {
                    return LazyOptional.of(() -> new FissionCcReactorPeripheral(reactor));
                } else {
                    return LazyOptional.empty();
                }
            } else {
                return LazyOptional.empty();
            }
        }
    }
}
