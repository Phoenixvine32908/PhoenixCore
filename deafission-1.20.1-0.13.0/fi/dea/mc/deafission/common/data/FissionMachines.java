//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import fi.dea.mc.deafission.FissionMod;
import fi.dea.mc.deafission.common.data.machine.FissionReactorMachine;
import fi.dea.mc.deafission.common.data.machine.ReactorFuelHolder;
import fi.dea.mc.deafission.common.data.machine.ReactorMaterialHolder;
import fi.dea.mc.deafission.common.data.machine.ReactorRedstonePort;
import fi.dea.mc.deafission.rendering.ItemCountProperty;
import fi.dea.mc.deafission.rendering.ReactorRenderer;
import fi.dea.mc.deafission.rendering.RodCountProperty;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.generators.BlockModelBuilder;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FissionMachines {
    public static final MachineDefinition ReactorFuelHolder;
    public static final MachineDefinition ReactorMaterialHolder;
    public static final MachineDefinition ReactorRedstonePort;
    public static final MultiblockMachineDefinition FissionReactorMk1;
    public static final MultiblockMachineDefinition FissionReactorMk2;

    public static void init() {
    }

    public static MachineBuilder.ModelInitializer createFuelHolderModel() {
        return (ctx, prov, builder) -> builder.forAllStatesModels((renderState) -> {
                Integer rods = (Integer)renderState.m_61143_(RodCountProperty.PROPERTY);
                BlockModelBuilder model = (BlockModelBuilder)prov.models().withExistingParent("block/machine/fuel_holder/" + rods, GTCEu.id("block/cube_2_layer/all"));
                model.texture("bot_all", FissionMod.id("block/machine/fuel_holder/0"));
                if (rods > 0) {
                    model.texture("top_all", FissionMod.id("block/machine/fuel_holder/" + rods));
                } else {
                    model.texture("top_all", GTModels.BLANK_TEXTURE);
                }

                return model;
            });
    }

    public static MachineBuilder.ModelInitializer createMaterialHolderModel() {
        return (ctx, prov, builder) -> builder.forAllStatesModels((renderState) -> {
                Integer items = (Integer)renderState.m_61143_(ItemCountProperty.PROPERTY);
                BlockModelBuilder model = (BlockModelBuilder)prov.models().withExistingParent("block/machine/material_holder/" + items, GTCEu.id("block/cube_2_layer/all"));
                model.texture("bot_all", FissionMod.id("block/machine/material_holder/0"));
                if (items > 0) {
                    model.texture("top_all", FissionMod.id("block/machine/material_holder/" + items));
                } else {
                    model.texture("top_all", GTModels.BLANK_TEXTURE);
                }

                return model;
            });
    }

    public static MachineBuilder.ModelInitializer createRedstonePortModel() {
        return (ctx, prov, builder) -> builder.forAllStatesModels((renderState) -> {
                BlockModelBuilder model = (BlockModelBuilder)prov.models().withExistingParent("block/machine/redstone_port/default", GTCEu.id("block/cube_2_layer/all"));
                model.texture("bot_all", FissionMod.id("block/machine/redstone_port/all"));
                model.texture("top_all", GTModels.BLANK_TEXTURE);
                return model;
            });
    }

    public static Vec3 getReactorCoreSize(MultiblockMachineDefinition def) {
        Vec3 var10000;
        switch (def.getId().toString()) {
            case "deafission:fission_reactor_mk1" -> var10000 = new Vec3(4.9, 5.9, 4.9);
            case "deafission:fission_reactor_mk2" -> var10000 = new Vec3(6.9, 5.9, 6.9);
            default -> var10000 = Vec3.f_82478_;
        }

        return var10000;
    }

    public static Vec3 getReactorCoreOffset(MultiblockMachineDefinition def) {
        Vec3 var10000;
        switch (def.getId().toString()) {
            case "deafission:fission_reactor_mk1" -> var10000 = new Vec3((double)2.0F, (double)3.0F, (double)0.0F);
            case "deafission:fission_reactor_mk2" -> var10000 = new Vec3((double)3.0F, (double)3.0F, (double)0.0F);
            default -> var10000 = Vec3.f_82478_;
        }

        return var10000;
    }

    static {
        ReactorFuelHolder = FissionMod.GR.machine("fuel_holder", ReactorFuelHolder::new).rotationState(RotationState.NONE).modelProperty(RodCountProperty.PROPERTY, 0).model(createFuelHolderModel()).blockProp((p) -> p.m_60971_((state, level, pos) -> false).m_60955_()).register();
        ReactorMaterialHolder = FissionMod.GR.machine("material_holder", ReactorMaterialHolder::new).rotationState(RotationState.NONE).modelProperty(ItemCountProperty.PROPERTY, 0).model(createMaterialHolderModel()).blockProp((p) -> p.m_60971_((state, level, pos) -> false).m_60955_()).register();
        ReactorRedstonePort = FissionMod.GR.machine("redstone_port", ReactorRedstonePort::new).rotationState(RotationState.NONE).model(createRedstonePortModel()).register();
        FissionReactorMk1 = FissionMod.GR.multiblock("fission_reactor_mk1", FissionReactorMachine::new).rotationState(RotationState.NON_Y_AXIS).allowFlip(false).recipeType(FissionGtRecipeTypes.ReactorCoolantRecipe).recipeType(FissionGtRecipeTypes.ReactorFuelRecipe).recipeType(FissionGtRecipeTypes.ReactorProcessingRecipe).appearanceBlock(GCYMBlocks.CASING_ATOMIC).model(GTMachineModels.createWorkableCasingMachineModel(ResourceLocation.parse("gtceu:block/casings/gcym/atomic_casing"), FissionMod.id("block/machine/fission_reactor_mk1")).andThen((m) -> m.addDynamicRenderer(ReactorRenderer::getInstance))).hasBER(true).pattern((def) -> FactoryBlockPattern.start(RelativeDirection.LEFT, RelativeDirection.BACK, RelativeDirection.UP).aisle(new String[]{"ccMcc", "chhhc", "chhhc", "chhhc", "ccccc"}).aisle(new String[]{"hcgch", "ci ic", "g f g", "ci ic", "hcgch"}).aisle(new String[]{"hcgch", "ci ic", "g f g", "ci ic", "hcgch"}).aisle(new String[]{"hcgch", "ci ic", "g f g", "ci ic", "hcgch"}).aisle(new String[]{"hcgch", "ci ic", "g f g", "ci ic", "hcgch"}).aisle(new String[]{"hcgch", "ci ic", "g f g", "ci ic", "hcgch"}).aisle(new String[]{"ccccc", "cGGGc", "cGGGc", "cGGGc", "ccccc"}).where('M', Predicates.controller(Predicates.blocks(new IMachineBlock[]{def.get()}))).where('h', Predicates.blocks(new Block[]{(Block)GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()})).where('c', Predicates.blocks(new Block[]{(Block)GCYMBlocks.CASING_ATOMIC.get()}).setMinGlobalLimited(63).or(Predicates.abilities(new PartAbility[]{PartAbility.IMPORT_ITEMS})).or(Predicates.abilities(new PartAbility[]{PartAbility.EXPORT_ITEMS})).or(Predicates.abilities(new PartAbility[]{PartAbility.IMPORT_FLUIDS})).or(Predicates.abilities(new PartAbility[]{PartAbility.EXPORT_FLUIDS})).or(Predicates.blocks(new IMachineBlock[]{ReactorRedstonePort.get()}))).where('g', Predicates.blocks(new Block[]{(Block)GTBlocks.CASING_LAMINATED_GLASS.get()})).where('G', Predicates.blocks(new Block[]{(Block)GTBlocks.CASING_LAMINATED_GLASS.get()}).setPreviewCount(9).or(Predicates.blocks(new Block[]{(Block)GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()}))).where('i', Predicates.blocks(new IMachineBlock[]{ReactorMaterialHolder.get()}).setPreviewCount(20).or(Predicates.air().setPreviewCount(0)).or(Predicates.blockTag(FissionTags.COMPONENT)).setPreviewCount(0)).where('f', Predicates.blocks(new IMachineBlock[]{ReactorFuelHolder.get()})).build()).register();
        FissionReactorMk2 = FissionMod.GR.multiblock("fission_reactor_mk2", FissionReactorMachine::new).rotationState(RotationState.NON_Y_AXIS).allowFlip(false).recipeType(FissionGtRecipeTypes.ReactorCoolantRecipe).recipeType(FissionGtRecipeTypes.ReactorFuelRecipe).recipeType(FissionGtRecipeTypes.ReactorProcessingRecipe).appearanceBlock(GCYMBlocks.CASING_ATOMIC).model(GTMachineModels.createWorkableCasingMachineModel(ResourceLocation.parse("gtceu:block/casings/gcym/atomic_casing"), FissionMod.id("block/machine/fission_reactor_mk1")).andThen((m) -> m.addDynamicRenderer(ReactorRenderer::getInstance))).hasBER(true).pattern((def) -> FactoryBlockPattern.start(RelativeDirection.LEFT, RelativeDirection.BACK, RelativeDirection.UP).aisle(new String[]{"cccMccc", "chhhhhc", "chhhhhc", "chhhhhc", "chhhhhc", "chhhhhc", "ccccccc"}).aisle(new String[]{"hcgggch", "c i i c", "gi f ig", "g f f g", "gi f ig", "c i i c", "hcgggch"}).aisle(new String[]{"hcgggch", "c i i c", "gi f ig", "g f f g", "gi f ig", "c i i c", "hcgggch"}).aisle(new String[]{"hcgggch", "c i i c", "gi f ig", "g f f g", "gi f ig", "c i i c", "hcgggch"}).aisle(new String[]{"hcgggch", "c i i c", "gi f ig", "g f f g", "gi f ig", "c i i c", "hcgggch"}).aisle(new String[]{"hcgggch", "c i i c", "gi f ig", "g f f g", "gi f ig", "c i i c", "hcgggch"}).aisle(new String[]{"ccccccc", "cGGGGGc", "cGGGGGc", "cGGGGGc", "cGGGGGc", "cGGGGGc", "ccccccc"}).where('M', Predicates.controller(Predicates.blocks(new IMachineBlock[]{def.get()}))).where('h', Predicates.blocks(new Block[]{(Block)GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()})).where('c', Predicates.blocks(new Block[]{(Block)GCYMBlocks.CASING_ATOMIC.get()}).setMinGlobalLimited(77).or(Predicates.abilities(new PartAbility[]{PartAbility.IMPORT_ITEMS})).or(Predicates.abilities(new PartAbility[]{PartAbility.EXPORT_ITEMS})).or(Predicates.abilities(new PartAbility[]{PartAbility.IMPORT_FLUIDS})).or(Predicates.abilities(new PartAbility[]{PartAbility.EXPORT_FLUIDS})).or(Predicates.blocks(new IMachineBlock[]{ReactorRedstonePort.get()}))).where('g', Predicates.blocks(new Block[]{(Block)GTBlocks.CASING_LAMINATED_GLASS.get()})).where('G', Predicates.blocks(new Block[]{(Block)GTBlocks.CASING_LAMINATED_GLASS.get()}).setPreviewCount(25).or(Predicates.blocks(new Block[]{(Block)GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()}))).where('i', Predicates.blocks(new IMachineBlock[]{ReactorMaterialHolder.get()}).setPreviewCount(20).or(Predicates.air().setPreviewCount(0)).or(Predicates.blockTag(FissionTags.COMPONENT)).setPreviewCount(0)).where('f', Predicates.blocks(new IMachineBlock[]{ReactorFuelHolder.get()})).build()).register();
    }
}
