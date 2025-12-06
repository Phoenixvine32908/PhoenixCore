//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.compat.emi;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import fi.dea.mc.deafission.FissionMod;
import fi.dea.mc.deafission.common.data.FissionMachines;
import fi.dea.mc.deafission.common.data.recipe.ComponentRecipe;
import fi.dea.mc.deafission.common.data.recipe.ComponentRecipe.Type;
import fi.dea.mc.deafission.core.components.EfficiencyComponent;
import fi.dea.mc.deafission.core.components.HeatComponent;
import fi.dea.mc.deafission.core.components.IReactorComponent;
import fi.dea.mc.deafission.core.components.ThrottleComponent;
import java.util.Objects;
import net.minecraft.world.level.ItemLike;

@EmiEntrypoint
public class FissionEmiPlugin implements EmiPlugin {
    public static final EmiRecipeCategory FISSION_COMPONENT;

    public void register(EmiRegistry emiRegistry) {
        emiRegistry.addCategory(FISSION_COMPONENT);
        emiRegistry.addWorkstation(FISSION_COMPONENT, EmiStack.of(FissionMachines.FissionReactorMk1.getItem().m_5456_()));
        emiRegistry.addWorkstation(FISSION_COMPONENT, EmiStack.of(FissionMachines.FissionReactorMk2.getItem().m_5456_()));

        for(ComponentRecipe r : emiRegistry.getRecipeManager().m_44013_(Type.INSTANCE)) {
            IReactorComponent c = r.component();
            if (c instanceof HeatComponent ch) {
                emiRegistry.addRecipe(new FissionEmiComponentRecipe(r.id(), ch));
            } else if (c instanceof ThrottleComponent th) {
                emiRegistry.addRecipe(new FissionEmiComponentRecipe(r.id(), th));
            } else {
                if (!(c instanceof EfficiencyComponent)) {
                    throw new IllegalArgumentException(c.getComponentType().toString());
                }

                EfficiencyComponent eh = (EfficiencyComponent)c;
                emiRegistry.addRecipe(new FissionEmiComponentRecipe(r.id(), eh));
            }
        }

    }

    static {
        FISSION_COMPONENT = new EmiRecipeCategory(FissionMod.id("fission_component"), EmiStack.of((ItemLike)Objects.requireNonNull(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.IncoloyMA956))));
    }
}
