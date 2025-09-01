package net.phoenix.core.common.machine.multiblock.electric.alchemical;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.core.common.data.PhoenixRecipeTypes;
import net.phoenix.core.common.machine.multiblock.CreativeEnergyMultiMachine;
import net.phoenix.core.common.registry.PhoenixRegistration;

public class AlchemicalImbuer {

    public static final MachineDefinition ALCHEMICAL_IMBUER = PhoenixRegistration.REGISTRATE
            .multiblock("alchemical_imbuer", CreativeEnergyMultiMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(PhoenixRecipeTypes.SOURCE_EXTRACTION, PhoenixRecipeTypes.SOURCE_IMBUMENT)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.CASING_TITANIUM_STABLE)
            .pattern(multiblockMachineDefinition -> FactoryBlockPattern.start()
                    .aisle("BBCCCBB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBCCCBB")
                    .aisle("BCDDDCB", "BBCCCBB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBCCCBB", "BCDDDCB")
                    .aisle("CDDDDDC", "BCEEECB", "BBFFFBB", "BBFFFBB", "BBFFFBB", "BCDDDCB", "CDGGGDC")
                    .aisle("CDDDDDC", "BCEEECB", "BBFEFBB", "BBFHFBB", "BBFEFBB", "BCDIDCB", "CDGJGDC")
                    .aisle("CDDDDDC", "BCEEECB", "BBFFFBB", "BBFFFBB", "BBFFFBB", "BCDDDCB", "CDGGGDC")
                    .aisle("BCDDDCB", "BBCCCBB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBCCCBB", "BCDDDCB")
                    .aisle("BBCKCBB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBCCCBB")
                    .where("B", Predicates.any())
                    .where("C",
                            Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
                                    .or(Predicates.autoAbilities(multiblockMachineDefinition.getRecipeTypes()))
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where("D",
                            Predicates.blocks(
                                    ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("ars_nouveau:sourcestone"))))
                    .where("E",
                            Predicates.blocks(ForgeRegistries.BLOCKS
                                    .getValue(ResourceLocation.parse("ars_nouveau:magebloom_block"))))
                    .where("F", Predicates.blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                    .where("G",
                            Predicates.blocks(
                                    ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("ars_nouveau:void_prism"))))
                    .where("H",
                            Predicates.blocks(ForgeRegistries.BLOCKS
                                    .getValue(ResourceLocation.parse("ars_nouveau:source_gem_block"))))
                    .where("I",
                            Predicates.blocks(
                                    ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("ars_nouveau:arcane_core"))))
                    .where("J",
                            Predicates.blocks(ForgeRegistries.BLOCKS
                                    .getValue(ResourceLocation.parse("ars_nouveau:agronomic_sourcelink"))))
                    .where("K", Predicates.controller(Predicates.blocks(multiblockMachineDefinition.get())))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .register();

    public static void init() {}
}
