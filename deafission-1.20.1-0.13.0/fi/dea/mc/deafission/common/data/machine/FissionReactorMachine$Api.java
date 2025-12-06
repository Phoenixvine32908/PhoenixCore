//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data.machine;

import fi.dea.mc.deafission.api.IReactorApi;
import fi.dea.mc.deafission.api.ReactorFuel;
import fi.dea.mc.deafission.api.ReactorMetrics;
import fi.dea.mc.deafission.common.data.items.FuelCellItem;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FissionReactorMachine$Api implements IReactorApi {
    private @Nullable ReactorMetrics _currentMetrics;
    private @Nullable List<@Nullable ReactorFuel> _currentFuels;

    public FissionReactorMachine$Api(FissionReactorMachine this$0) {
        this.this$0 = this$0;
    }

    void update() {
        this._updateMetrics();
        this._updateFuels();
    }

    void clear() {
        this._currentMetrics = null;
        this._currentFuels = null;
    }

    public @Nullable ReactorMetrics getMetrics() {
        return this._currentMetrics;
    }

    public @Nullable List<@Nullable ReactorFuel> getFuels() {
        return this._currentFuels;
    }

    private void _updateMetrics() {
        this._currentMetrics = new ReactorMetrics(this.this$0._metrics.LastCoolantUse, this.this$0._metrics.LastCoolingPercent, this.this$0._metrics.LastFuelDamage, this.this$0._metrics.LastCooling, this.this$0._metrics.LastHeating, this.this$0._metrics.LastProcessing, this.this$0._metrics.LastMinHeat, this.this$0._metrics.LastMaxHeat, this.this$0._metrics.LastMass, this.this$0._metrics.LastThrottle, this.this$0._metrics.LastHeat, this.this$0._state.Heat, this.this$0._metrics.LastRecipe, this.this$0._metrics.LastComponents, this.this$0._metrics.LastMode);
    }

    private void _updateFuels() {
        this._currentFuels = this.this$0._io._fuels.stream().map((h) -> h.getHeldItem()).map((h) -> h != null ? ((FuelCellItem)h.m_41720_()).getFuelStats(h) : null).toList();
    }

    public byte[] getFuelDurabilityBytes() {
        byte[] arr = new byte[this.this$0._io._fuels.size()];

        for(int i = 0; i < arr.length; ++i) {
            ItemStack stack = ((ReactorFuelHolder)this.this$0._io._fuels.get(i)).getHeldItem();
            if (stack != null) {
                int intValue = 1 + Math.round(254.0F - (float)stack.m_41773_() / (float)stack.m_41776_() * 254.0F);
                arr[i] = (byte)(intValue & 255);
            }
        }

        return arr;
    }

    public List<String> getMetricsStrings() {
        return this.this$0._metrics.render();
    }
}
