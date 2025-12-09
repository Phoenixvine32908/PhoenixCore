package net.phoenix.core.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.recipes.FinishedRecipe;
import net.phoenix.core.common.data.recipeConditions.FluidInHatchCondition;
import net.phoenix.core.common.machine.PhoenixMachines;
import net.phoenix.core.configs.PhoenixConfigs;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.ADVANCED_COMPUTER_CASING;
import static com.gregtechceu.gtceu.common.data.GTBlocks.OPTICAL_PIPES;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static com.gregtechceu.gtceu.common.data.machines.GTResearchMachines.*;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.*;
import static net.phoenix.core.api.capability.PhoenixRecipeCapabilities.SHIELDTYPES;
import static net.phoenix.core.common.machine.PhoenixResearchMachines.ADVANCED_PHOENIX_COMPUTATION_COMPONENT;
import static net.phoenix.core.common.machine.PhoenixResearchMachines.PHOENIX_COMPUTATION_COMPONENT;
import static net.phoenix.core.common.machine.multiblock.Shield.ShieldTypes.INACTIVE;
import static net.phoenix.core.common.machine.multiblock.Shield.ShieldTypes.NORMAL;

public class PhoenixMachineRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        if (PhoenixConfigs.INSTANCE.features.creativeEnergyEnabled) {
            ASSEMBLY_LINE_RECIPES.recipeBuilder("dance")
                    .inputItems(GTMultiMachines.ACTIVE_TRANSFORMER)
                    .inputItems(TagPrefix.plate, Neutronium, 32)
                    .inputItems(SENSOR.get(UV), 8)
                    .inputItems(EMITTER.get(UV), 8)
                    .addCondition(FluidInHatchCondition.of("phoenixcore:quantum_coolant_plasma"))
                    .inputItems(FIELD_GENERATOR.get(UV), 4)
                    .inputItems(CustomTags.UHV_CIRCUITS, 2)
                    .inputItems(TagPrefix.pipeLargeFluid, Neutronium, 4)
                    .inputItems(CABLE_QUAD.get(UV), 8)
                    .inputFluids(SolderingAlloy.getFluid(L * 32))
                    .EUt(VA[LV]).duration(400)
                    .duration(1200)
                    .outputItems(PhoenixMachines.DANCE)
                    .stationResearch(b -> b
                            .researchStack(GTMultiMachines.ACTIVE_TRANSFORMER.asStack()).CWUt(16))
                    .save(provider);
        }
        if (PhoenixConfigs.INSTANCE.features.recipesEnabled) {
            if (ConfigHolder.INSTANCE.machines.highTierContent) {
                ASSEMBLY_LINE_RECIPES.recipeBuilder("high_performance_computing_array")
                        .inputItems(DATA_BANK)
                        .inputItems(CustomTags.ZPM_CIRCUITS, 4)
                        .inputItems(FIELD_GENERATOR_LuV, 8)
                        .inputItems(TOOL_DATA_ORB)
                        .inputItems(COVER_SCREEN)
                        .inputItems(wireGtDouble, UraniumRhodiumDinaquadide, 64)
                        .inputItems(OPTICAL_PIPES[0].asStack(16))
                        .inputFluids(SolderingAlloy, L * 8)
                        .inputFluids(VanadiumGallium, L * 8)
                        .inputFluids(PCBCoolant, 4000)
                        .outputItems(HIGH_PERFORMANCE_COMPUTING_ARRAY)
                        .scannerResearch(b -> b
                                .researchStack(COVER_SCREEN.asStack())
                                .duration(2400)
                                .EUt(VA[IV]))
                        .duration(1200).EUt(100000)
                        .addMaterialInfo(true, true).save(provider);
                ASSEMBLER_RECIPES.recipeBuilder("hpca_computation_component")
                        .inputItems(HPCA_EMPTY_COMPONENT)
                        .inputItems(CustomTags.ZPM_CIRCUITS, 4)
                        .inputItems(FIELD_GENERATOR_LuV)
                        .outputItems(HPCA_COMPUTATION_COMPONENT)
                        .inputFluids(PCBCoolant, 1000)
                        .cleanroom(CleanroomType.CLEANROOM)
                        .duration(200).EUt(VA[LuV])
                        .addMaterialInfo(true).save(provider);

                ASSEMBLER_RECIPES.recipeBuilder("hpca_advanced_computation_component")
                        .inputItems(PHOENIX_COMPUTATION_COMPONENT)
                        .inputItems(CustomTags.UEV_CIRCUITS, 4)
                        .inputItems(FIELD_GENERATOR_UEV)
                        .outputItems(ADVANCED_PHOENIX_COMPUTATION_COMPONENT)
                        .inputFluids(PCBCoolant, 10000)
                        .cleanroom(CleanroomType.CLEANROOM)
                        .duration(200).EUt(VA[UEV])
                        .addMaterialInfo(true).save(provider);
                ASSEMBLER_RECIPES.recipeBuilder("hpca_heat_sink_component")
                        .inputItems(HPCA_EMPTY_COMPONENT)
                        .inputItems(plate, Aluminium, 32)
                        .inputItems(screw, StainlessSteel, 8)
                        .outputItems(HPCA_HEAT_SINK_COMPONENT)
                        .inputFluids(PCBCoolant, 1000)
                        .cleanroom(CleanroomType.CLEANROOM)
                        .duration(200).EUt(VA[IV])
                        .addMaterialInfo(true).save(provider);

                ASSEMBLER_RECIPES.recipeBuilder("hpca_active_cooler_component")
                        .inputItems(ADVANCED_COMPUTER_CASING.asStack())
                        .inputItems(plate, Aluminium, 16)
                        .inputItems(pipeTinyFluid, StainlessSteel, 16)
                        .inputItems(screw, StainlessSteel, 8)
                        .outputItems(HPCA_ACTIVE_COOLER_COMPONENT)
                        .inputFluids(PCBCoolant, 1000)
                        .cleanroom(CleanroomType.CLEANROOM)
                        .duration(200).EUt(VA[IV])
                        .addMaterialInfo(true).save(provider);
            }
        }
        PhoenixRecipeTypes.PLEASE.recipeBuilder("plasma_furnace_shield_activation")

                // CRITICAL INPUT: Requires the machine to be in the INACTIVE state (Key 2).
                // The NotifiableShieldContainer blocks the recipe if the machine state doesn't match this input.
                .input(SHIELDTYPES, INACTIVE)

                // --- Material Costs (Example, adjust tier/cost as needed) ---
                .inputItems(TagPrefix.plate, Neutronium, 16)
                .inputItems(FIELD_GENERATOR.get(UV), 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(L * 32))

                // --- State Change Data Tags (Read by HighPressurePlasmaArcFurnaceMachine.java in afterWorking) ---
                // 1. Flag for the machine to recognize this is a state-change recipe
                .addData("shield_activation", true)
                // 2. Target state key: Shield.ShieldTypes.NORMAL has key 1
                .addData("updated_shield_key", NORMAL.key)

                // --- Recipe Duration & Energy ---
                .EUt(VA[UV]) // Ultra-Voltage (1M EUt/t)
                .duration(1000) // 50 seconds
                .save(provider);

        PhoenixRecipeTypes.PLEASE.recipeBuilder("please")
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 32))
                .duration(1200)
                .EUt(VA[LV]).duration(400)
                .inputFluids(Acetone.getFluid(GTValues.L * 32))
                .save(provider);
        PhoenixRecipeTypes.PLEASE.recipeBuilder("modify")
                .inputFluids(Neutronium.getFluid(1))
                .duration(1200)
                .EUt(VA[LV]).duration(400)
                .save(provider);
        PhoenixRecipeTypes.PLEASE.recipeBuilder("plasma_test")
                .inputFluids(Argon.getFluid(FluidStorageKeys.PLASMA, 100))
                .inputFluids(Water.getFluid(L * 16))
                .addCondition(FluidInHatchCondition.of("phoenixcore:quantum_coolant_plasma"))
                .duration(600)
                .EUt(VA[LV]).duration(400)
                .EUt(ZPM * 2)
                .outputItems(PhoenixMachines.DANCE)
                .save(provider);
        PhoenixRecipeTypes.HIGH_PERFORMANCE_BREEDER_REACTOR_RECIPES.recipeBuilder("honey_chamber_test")
                .inputFluids(Water.getFluid(16))
                .duration(600)
                .EUt(-LV).duration(400)
                .addData("required_cooling", 5000)
                .outputFluids(SodiumPotassium.getFluid(16))
                .save(provider);
        PhoenixRecipeTypes.HIGH_PERFORMANCE_BREEDER_REACTOR_RECIPES.recipeBuilder("honey_chamber_tet")
                .inputFluids(Acetone.getFluid(16))
                .duration(600)
                .EUt(-LV).duration(400)
                .addData("required_cooling", 12000)
                .outputFluids(SodiumPotassium.getFluid(16))
                .save(provider);
    }
}
