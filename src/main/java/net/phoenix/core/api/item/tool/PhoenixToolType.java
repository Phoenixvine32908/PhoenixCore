package net.phoenix.core.api.item.tool;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.data.item.GTToolActions;
import com.gregtechceu.gtceu.common.item.tool.behavior.AOEConfigUIBehavior;
import com.gregtechceu.gtceu.common.item.tool.behavior.EntityDamageBehavior;
import com.gregtechceu.gtceu.common.item.tool.behavior.TorchPlaceBehavior;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.monster.Spider;
import net.phoenix.core.common.data.recipe.generated.PhoenixToolRecipeHelper;


public final class PhoenixToolType {


    public static final GTToolType SCREWDRIVER_MV = GTToolType.builder("mv_screwdriver")
            .idFormat("mv_%s_screwdriver")
            .toolTag(CustomTags.CRAFTING_SCREWDRIVERS)
            .toolTag(CustomTags.SCREWDRIVERS)
            .toolStats(b -> b.crafting().sneakBypassUse()
                    .attackDamage(-1.0F).attackSpeed(3.0F)
                    .behaviors(new EntityDamageBehavior(3.0F, Spider.class))
                    .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_MV))
            .sound(GTSoundEntries.SCREWDRIVER_TOOL)
            .electric(GTValues.MV)
            .toolClasses(GTToolType.SCREWDRIVER)
            .defaultActions(GTToolActions.DEFAULT_SCREWDRIVER_ACTIONS)
            .build();
    public static final GTToolType SCREWDRIVER_EV = GTToolType.builder("ev_screwdriver")
            .idFormat("ev_%s_screwdriver")
            .toolTag(CustomTags.CRAFTING_SCREWDRIVERS)
            .toolTag(CustomTags.SCREWDRIVERS)
            .toolStats(b -> b.crafting().sneakBypassUse()
                    .attackDamage(-1.0F).attackSpeed(3.0F)
                    .behaviors(new EntityDamageBehavior(3.0F, Spider.class))
                    .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_EV))
            .sound(GTSoundEntries.SCREWDRIVER_TOOL)
            .electric(GTValues.EV)
            .toolClasses(GTToolType.SCREWDRIVER)
            .defaultActions(GTToolActions.DEFAULT_SCREWDRIVER_ACTIONS)
            .build();
    public static final GTToolType DRILL_LUV = GTToolType.builder("luv_drill")
            .idFormat("luv_%s_drill")
            .toolTag(CustomTags.DRILLS)
            .toolTag(ItemTags.PICKAXES)
            .toolTag(ItemTags.SHOVELS)
            .toolTag(ItemTags.HOES)
            .toolTag(ItemTags.CLUSTER_MAX_HARVESTABLES)
            .harvestTag(BlockTags.MINEABLE_WITH_PICKAXE)
            .harvestTag(BlockTags.MINEABLE_WITH_SHOVEL)
            .harvestTag(BlockTags.MINEABLE_WITH_HOE)
            .toolStats(b -> b.blockBreaking().aoe(5, 5, 10) // <--- THIS IS THE KEY CHANGE
                    .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(8.0F)
                    .brokenStack(PhoenixToolHelper.SUPPLY_POWER_UNIT_LUV)
                    .behaviors(AOEConfigUIBehavior.INSTANCE, TorchPlaceBehavior.INSTANCE))
            .sound(GTSoundEntries.DRILL_TOOL, true)
            .electric(GTValues.LuV) // Use the next tier constant
            .toolClassNames("drill")
            .defaultActions(GTToolActions.DEFAULT_DRILL_ACTIONS)
            .build();
}
