package net.phoenix.core.api.item.tool;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.data.item.GTToolActions;
import com.gregtechceu.gtceu.common.item.tool.behavior.AOEConfigUIBehavior;
import com.gregtechceu.gtceu.common.item.tool.behavior.TorchPlaceBehavior;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;

/**
 * This class serves as a central hub for all custom GregTech tool types from the Phoenix mod.
 * It provides a clean and easy way to access and define these tool types.
 *
 * <p>By defining the tool types here, we separate the public API from the Mixin implementation,
 * improving code clarity and maintainability.
 */
public final class PhoenixToolType {

    /**
     * The custom LuV tier Electric Drill tool type.
     * This field is public and provides a clean, central reference for this tool type.
     *
     * <p>This field is initialized in the static block to ensure it's available as soon as the class is loaded.
     */
    public static final GTToolType DRILL_LuV = new GTToolType.Builder("drill_luv")
            .toolTag(ItemTags.CLUSTER_MAX_HARVESTABLES)
            .toolTag(ItemTags.HOES)
            .harvestTag(BlockTags.MINEABLE_WITH_PICKAXE)
            .harvestTag(BlockTags.MINEABLE_WITH_SHOVEL)
            .harvestTag(BlockTags.MINEABLE_WITH_HOE)
            .toolStats(b -> b.blockBreaking().aoe(4, 4, 8)
                    .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(7.0F)
                    .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_IV)
                    .behaviors(AOEConfigUIBehavior.INSTANCE, TorchPlaceBehavior.INSTANCE))
            .sound(GTSoundEntries.DRILL_TOOL, true)
            .electric(GTValues.LuV)
            .toolClassNames("drill")
            .defaultActions(GTToolActions.DEFAULT_DRILL_ACTIONS)
            .build();
}