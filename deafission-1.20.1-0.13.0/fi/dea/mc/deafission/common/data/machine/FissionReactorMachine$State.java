//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data.machine;

import fi.dea.mc.deafission.common.data.FissionGtRecipes;
import fi.dea.mc.deafission.common.data.machine.FissionReactorMachine.OperatingMode;
import fi.dea.mc.deafission.core.ReactorCoolantRecipe;
import fi.dea.mc.deafission.util.FractionAccumulator;
import java.util.stream.IntStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

class FissionReactorMachine$State {
    public double Heat = (double)0.0F;
    public FractionAccumulator ColdCoolantConversionFraction = new FractionAccumulator((double)0.0F);
    public FractionAccumulator[] HotCoolantConversionFractions = new FractionAccumulator[0];
    public FissionReactorMachine.OperatingMode Mode;
    public @Nullable ReactorCoolantRecipe CoolantRecipe;

    private FissionReactorMachine$State() {
        this.Mode = OperatingMode.DEFAULT;
    }

    public void save(CompoundTag tag) {
        tag.m_128347_("Heat", this.Heat);
        tag.m_128347_("ColdCoolantConversionFraction", this.ColdCoolantConversionFraction.getFraction());
        tag.m_128385_("HotCoolantConversionFractions", this._ser(this.HotCoolantConversionFractions));
        tag.m_128359_("Mode", this.Mode.m_7912_());
        if (this.CoolantRecipe != null) {
            tag.m_128359_("CoolantRecipe", this.CoolantRecipe.id().toString());
        }

    }

    public void load(CompoundTag tag) {
        this.Heat = tag.m_128459_("Heat");
        this.ColdCoolantConversionFraction = new FractionAccumulator(tag.m_128459_("ColdCoolantConversionFraction"));
        this.HotCoolantConversionFractions = this._deserFracts(tag.m_128465_("HotCoolantConversionFractions"));
        this.Mode = OperatingMode.fromSerializedName(tag.m_128461_("Mode"));
        ResourceLocation coolantRecipeId = ResourceLocation.parse(tag.m_128461_("CoolantRecipe"));
        this.CoolantRecipe = (ReactorCoolantRecipe)FissionGtRecipes.get_coolantRecipes().stream().filter((r) -> r.id().equals(coolantRecipeId)).findFirst().orElse((Object)null);
        this.resetAccu(this.CoolantRecipe != null ? this.CoolantRecipe.output().length : 0);
    }

    private int[] _ser(FractionAccumulator[] arr) {
        int[] ret = new int[arr.length];

        for(int i = 0; i < arr.length; ++i) {
            ret[i] = (int)(arr[i].getFraction() * (double)Integer.MAX_VALUE);
        }

        return ret;
    }

    private FractionAccumulator[] _deserFracts(int[] arr) {
        FractionAccumulator[] ret = new FractionAccumulator[arr.length];

        for(int i = 0; i < arr.length; ++i) {
            ret[i] = new FractionAccumulator((double)arr[i] / (double)Integer.MAX_VALUE);
        }

        return ret;
    }

    public void resetAccu(int outputFluids) {
        this.ColdCoolantConversionFraction = new FractionAccumulator((double)0.0F);
        this.HotCoolantConversionFractions = (FractionAccumulator[])IntStream.range(0, outputFluids).mapToObj((i) -> new FractionAccumulator((double)0.0F)).toArray((x$0) -> new FractionAccumulator[x$0]);
    }
}
