package net.phoenix.core.common.block;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.block.IFilterType;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.phoenix.core.PhoenixCore;
import net.phoenix.core.configs.PhoenixConfigs;

import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import org.jetbrains.annotations.NotNull;

import static net.phoenix.core.common.registry.PhoenixRegistration.REGISTRATE;

@SuppressWarnings("unused")
public class PhoenixBlocks {

    public static void init() {}

    private static @NotNull BlockEntry<Block> registerSimpleBlock(String name, String id, String texture,
                                                                  NonNullBiFunction<Block, Item.Properties, ? extends BlockItem> func) {
        return REGISTRATE
                .block(id, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().cubeAll(ctx.getName(), PhoenixCore.id("block/" + texture))))
                .lang(name)
                .item(func)
                .build()
                .register();
    }

    public static final BlockEntry<CoilBlock> COIL_TRUE_HEAT_STABLE = createCoilBlock(
            PhoenixCoilBlock.CoilType.COIL_TRUE_HEAT_STABLE);

    public static BlockEntry<Block> PHOENIX_ENRICHED_TRITANIUM_CASING = registerSimpleBlock(
            "§6Extremely Heat-Stable Casing", "phoenix_enriched_tritanium_casing",
            "phoenix_enriched_tritanium_casing", BlockItem::new);
    public static BlockEntry<Block> PHOENIX_GAZE_PANEL = registerSimpleBlock(
            "§cPhoenix Gaze Panel", "phoenix_gaze_panel",
            "phoenix_gaze_panel", BlockItem::new);
    public static BlockEntry<Block> TRUE_PHOENIX_INFUSED_CASING = registerSimpleBlock(
            "§cTrue Phoenix Infused Casing", "true_phoenix_infused_casing",
            "true_phoenix_infused_casing", BlockItem::new);
    public static BlockEntry<Block> AKASHIC_ZERONIUM_CASING = registerSimpleBlock(
            "§5Akashic Zeronium Casing", "akashic_zeronium_casing",
            "akashic_zeronium_casing", BlockItem::new);
    public static BlockEntry<Block> PERFECTED_LOGIC = registerSimpleBlock(
            "§5Perfected Logic Casing", "perfected_logic",
            "perfected_logic", BlockItem::new);
    public static BlockEntry<Block> PHOENIX_ENRICHED_NEUTRONIUM_CASING = registerSimpleBlock(
            "§5Phoenix Enriched Neutronium Casing", "phoenix_enriched_neutronium_casing",
            "phoenix_enriched_neutronium_casing", BlockItem::new);
    public static BlockEntry<Block> AKASHIC_COIL_BLOCK = registerSimpleBlock(
            "§5Computation Coil", "akashic_coil_block",
            "akashic_coil_block", BlockItem::new);
    public static BlockEntry<Block> SPACE_TIME_COOLED_ETERNITY_CASING = registerSimpleBlock(
            "§5Space Time Cooled Eternity Casing", "space_time_cooled_eternity_casing",
            "space_time_cooled_eternity_casing", BlockItem::new);
    public static BlockEntry<Block> TWISTED_COMPUTER_CASING = registerSimpleBlock(
            "§5Twisted Computer Casing", "twisted_computer_casing",
            "twisted_computer_casing", BlockItem::new);
    static {
        if (PhoenixConfigs.INSTANCE.features.blazingCleanroomEnabled) {
            final BlockEntry<Block> BLAZING_CLEANROOM_FILTER_CASING = createCleanroomFilters(
                    BlazingFilterType.FILTER_CASING_BLAZING);
        }
    }

    private static BlockEntry<Block> createCleanroomFilters(IFilterType filterType) {
        var filterBlock = REGISTRATE.block(filterType.getSerializedName(), Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(properties -> properties.strength(2.0f, 8.0f).sound(SoundType.METAL)
                        .isValidSpawn((blockState, blockGetter, blockPos, entityType) -> false))
                .blockstate(GTModels.createCleanroomFilterModel(filterType))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, CustomTags.TOOL_TIERS[1])
                .item(BlockItem::new)
                .build()
                .register();
        GTCEuAPI.CLEANROOM_FILTERS.put(filterType, filterBlock);
        return filterBlock;
    }

    private static BlockEntry<CoilBlock> createCoilBlock(ICoilType coilType) {
        var coilBlock = REGISTRATE
                .block("%s_coil_block".formatted(coilType.getName()), p -> new CoilBlock(p, coilType))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(GTModels.createCoilModel(coilType))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
        GTCEuAPI.HEATING_COILS.put(coilType, coilBlock);
        return coilBlock;
    }
}
