package net.phoenix.core.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.recipes.FinishedRecipe;
import net.phoenix.core.common.machine.PhoenixMachines;
import net.phoenix.core.configs.PhoenixConfigs;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.*;

public class PhoenixMachineRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        if (PhoenixConfigs.INSTANCE.features.creativeEnergyEnabled) {
            ASSEMBLY_LINE_RECIPES.recipeBuilder("dance")
                    .inputItems(GTMultiMachines.ACTIVE_TRANSFORMER)
                    .inputItems(TagPrefix.plate, GTMaterials.Neutronium, 32)
                    .inputItems(SENSOR.get(GTValues.UV), 8)
                    .inputItems(EMITTER.get(GTValues.UV), 8)
                    .inputItems(FIELD_GENERATOR.get(GTValues.UV), 4)
                    .inputItems(CustomTags.UHV_CIRCUITS, 2)
                    .inputItems(TagPrefix.pipeLargeFluid, GTMaterials.Neutronium, 4)
                    .inputItems(CABLE_QUAD.get(GTValues.UV), 8)
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 32))
                    .EUt(GTValues.VA[LV]).duration(40)
                    .duration(1200)
                    .outputItems(PhoenixMachines.DANCE)
                    .stationResearch(b -> b
                            .researchStack(GTMultiMachines.ACTIVE_TRANSFORMER.asStack()).CWUt(16))
                    .save(provider);
            PhoenixRecipeTypes.PLEASE.recipeBuilder("please")
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 32))
                    .duration(1200)
                    .inputFluids(Acetone.getFluid(GTValues.L * 32))
                    .save(provider);
            PhoenixRecipeTypes.PLEASE.recipeBuilder("modify")
                    .inputFluids(Neutronium.getFluid(1))
                    .duration(1200)
                    .save(provider);
        }
    }
}
