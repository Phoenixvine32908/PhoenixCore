//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import fi.dea.mc.deafission.FissionMod;

public class FissionFluids {
    public static Material FlibeCold;
    public static Material FlibeHot;

    public static void init() {
    }

    static {
        FlibeCold = (new Material.Builder(FissionMod.id("flibe"))).liquid((new FluidBuilder()).temperature(293).viscosity(2000).block().customStill()).flags(new MaterialFlag[]{MaterialFlags.DISABLE_DECOMPOSITION, MaterialFlags.NO_UNIFICATION}).formula("FLiBe").buildAndRegister();
        FlibeHot = (new Material.Builder(FissionMod.id("flibe_hot"))).liquid((new FluidBuilder()).temperature(3293).viscosity(500).luminosity(15).block().customStill()).flags(new MaterialFlag[]{MaterialFlags.DISABLE_DECOMPOSITION, MaterialFlags.NO_UNIFICATION}).formula("FLiBe").buildAndRegister();
    }
}
