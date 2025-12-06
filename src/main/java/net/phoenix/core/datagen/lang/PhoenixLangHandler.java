package net.phoenix.core.datagen.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class PhoenixLangHandler {

    public static void init(RegistrateLangProvider provider) {
        // --- Custom Capability/Recipe Viewer Keys (New) ---
        provider.add("emi_info.phoenixcore.required_shield", "Required Shield: %s");
        provider.add("shield.phoenixcore.type.normal", "Normal");
        provider.add("shield.phoenixcore.type.inactive", "Inactive");
        provider.add("shield.phoenixcore.type.decayed", "Decayed");
        // --------------------------------------------------
        provider.add("emi_info.phoenixcore.shield_heal", "Shield Health Restored: +%s");
        provider.add("emi_info.phoenixcore.shield_damage", "Shield Damage Applied: -%s");
        provider.add("phoenix.fission.coolant_required", "§7Required Coolant: §f%s");
        provider.add("phoenix.fission.not_formed", "Structure not formed!");
        provider.add("phoenix.fission.moderator", "Moderator: %s");
        provider.add("phoenix.fission.moderator_boost", "EU Boost: %s%%");
        provider.add("phoenix.fission.moderator_fuel_discount", "Fuel Discount: %s%%");
        provider.add("phoenix.fission.cooler", "Cooler: %s");
        provider.add("phoenix.fission.coolant", "Coolant: %s");
        provider.add("phoenix.fission.cooling_power", "§7Cooling Power: %s");
        provider.add("phoenix.fission.coolant_rate", "Coolant Rate: %s mB/t");
        provider.add("phoenix.fission.meltdown_in", "MELTDOWN in: %s seconds");
        provider.add("phoenix.fission.safe", "Status: SAFE");
        provider.add("phoenix.fission.summary", "Cooling Provided: %s / %s");
        provider.add("emi_info.phoenixcore.required_cooling", "Required Cooling: %s");

        provider.add("phoenix.fission.status.safe_idle", "SAFE (Idle)");
        provider.add("phoenix.fission.status.safe_working", "SAFE (Working)");
        provider.add("phoenix.fission.status.danger_timer", "DANGER: Meltdown in %s seconds!");
        provider.add("phoenix.fission.status.no_coolant", "Coolant Tanks EMPTY");
        provider.add("phoenix.fission.status.low_cooling", "Insufficient Cooling Power");
        provider.add("phoenix.fission.coolant_status.ok", "Coolant Status: OK");
        provider.add("phoenix.fission.coolant_status.empty", "Coolant Status: EMPTY");
        provider.add("tooltip.phoenixcore.crystal_rose.generic", "A crystalline flower of immense power.");
        provider.add("tooltip.phoenixcore.crystal_rose.made_from", "Forged from %s.");
        provider.add("tooltip.phoenixcore.nanites.generic", "Microscopic machines swarming with potential.");
        provider.add("tooltip.phoenixcore.nanites.made_from", "Constructed from %s.");

        provider.add("block.phoenixcore.fission_cooler.shift", "Hold §eShift§r for cooler details");
        provider.add("block.phoenixcore.fission_cooler.info_header", "§7--- Cooler Information ---");
        provider.add("block.phoenixcore.fission_cooler.temperature", "§cOperating Temperature: §f%s K");
        provider.add("block.phoenixcore.fission_cooler.required_coolant", "§bRequired Coolant: §f%s");
        provider.add("block.phoenixcore.fission_cooler.flow_rate", "§aFlow Rate Multiplier: §f%s×");

        provider.add("block.phoenixcore.fission_moderator.shift", "Hold §eShift§r for moderator details");
        provider.add("block.phoenixcore.fission_moderator.info_header", "§7--- Moderator Information ---");
        provider.add("block.phoenixcore.fission_moderator.boost", "§aEU Output Boost: §f%s×");
        provider.add("block.phoenixcore.fission_moderator.fuel_discount", "§bFuel Usage Reduction: §f%s×");

        provider.add("phoenix.multiblock.pattern.error.single_cooler_type",
                "Only one type of cooler may be used in this multiblock!");
        provider.add("phoenix.multiblock.pattern.error.single_moderator_type",
                "Only one type of moderator may be used in this multiblock!");

        provider.add("config.jade.plugin_phoenixcore.source_hatch_info", "Source Stored: %s");
        provider.add("gui.phoenixcore.source_hatch.source", "Source Stored: %s");
        provider.add("gui.phoenixcore.source_hatch.label.import", "Source Input Hatch");
        provider.add("gui.phoenixcore.source_hatch.label.export", "Source Output Hatch");
        provider.add("tooltip.phoenixcore.source_hatch.consumption", "§cMax Source Consumption§f:§6 %s");
        provider.add("tooltip.phoenixcore.source_hatch.capacity", "§cMax Source capacity§f:§6 %s");
        provider.add("phoenixcore.recipe.source_in", "Source Input: %s");
        provider.add("phoenixcore.recipe.source_out", "Source Output: %s");
        provider.add("phoenixcore.jade.source_hatch_info", "Current Source in Hatch: %s.");
        provider.add("phoenixcore.tooltip.hyper_machine_coolant1", "%s: %sx CWU/t");
        provider.add("phoenixcore.tooltip.hyper_machine_coolant2", "%s: %sx CWU/t");
        provider.add("phoenixcore.tooltip.hyper_machine_coolant_base", "%s: %sx CWU/t");
        provider.add("phoenixcore.tooltip.hyper_machine_coolant3", "%s: %sx CWU/t");
        provider.add("phoenixcore.tooltip.hyper_machine_purpose",
                "An upgraded HPCA that uses %s, %s, or %s to provide cooling");
        provider.add("phoenixcore.tooltip.requires_fluid", "Needs: %s");
        provider.add("tagprefix.nanites", "%s Nanites");
        provider.add("tagprefix.crystal_rose", "%s Crystal Rose");
        provider.add("item.gtceu.tool.ev_screwdriver", "%s Electric Screwdriver (EV)");
        provider.add("item.gtceu.tool.mv_screwdriver", "%s Electric Screwdriver (MV)");
        provider.add("item.gtceu.tool.luv_screwdriver", "%s Electric Screwdriver (LuV)");
        provider.add("item.gtceu.tool.zpm_screwdriver", "%s Electric Screwdriver (ZPM)");
        provider.add("item.gtceu.tool.mv_chainsaw", "%s Chainsaw (MV)");
        provider.add("item.gtceu.tool.hv_chainsaw", "%s Chainsaw (HV)");
        provider.add("item.gtceu.tool.ev_chainsaw", "%s Chainsaw (EV)");
        provider.add("item.gtceu.tool.iv_chainsaw", "%s Chainsaw (IV)");
        provider.add("item.gtceu.tool.luv_chainsaw", "%s Chainsaw (LuV)");
        provider.add("item.gtceu.tool.zpm_chainsaw", "%s Chainsaw (ZPM)");

        provider.add("item.gtceu.tool.mv_buzzsaw", "%s Buzzsaw (MV)");
        provider.add("item.gtceu.tool.hv_buzzsaw", "%s Buzzsaw (HV)");
        provider.add("item.gtceu.tool.ev_buzzsaw", "%s Buzzsaw (EV)");
        provider.add("item.gtceu.tool.iv_buzzsaw", "%s Buzzsaw (IV)");
        provider.add("item.gtceu.tool.luv_buzzsaw", "%s Buzzsaw (LuV)");
        provider.add("item.gtceu.tool.zpm_buzzsaw", "%s Buzzsaw (ZPM)");

        provider.add("item.gtceu.tool.mv_wrench", "%s Wrench (MV)");
        provider.add("item.gtceu.tool.ev_wrench", "%s Wrench (EV)");
        provider.add("item.gtceu.tool.luv_wrench", "%s Wrench (LuV)");
        provider.add("item.gtceu.tool.zpm_wrench", "%s Wrench (ZPM)");

        provider.add("item.gtceu.tool.mv_wire_cutters", "%s Wire Cutters (MV)");
        provider.add("item.gtceu.tool.ev_wire_cutters", "%s Wire Cutters (EV)");
        provider.add("item.gtceu.tool.luv_wire_cutters", "%s Wire Cutters (LuV)");
        provider.add("item.gtceu.tool.zpm_wire_cutters", "%s Wire Cutters (ZPM)");

        provider.add("item.gtceu.tool.luv_drill", "%s Drill (LuV)");
        provider.add("item.gtceu.tool.zpm_drill", "%s Drill (ZPM)");
        provider.add("material.phoenixcore.phoenix_enriched_tritanium", "§cPhoenix Enriched Tritanium");
        provider.add("material.phoenixcore.extremely_modified_space_grade_steel",
                "§cExtremely Modified Space Grade Steel");
        provider.add("material.phoenixcore.quantum_coolant",
                "§bQuantum Coolant");
        provider.add("material.phoenixcore.eighty_five_percent_pure_nevvonian_steel",
                "§6Eighty Five Percent Pure Nevvonian Steel");
        provider.add("material.phoenixcore.phoenix_enriched_naquadah",
                "§6Phoenix Enriched Naquadah");
        provider.add("phoenixcore.tooltip.hyper_machine_1", "Each Coolant provides a boost:");

        provider.add("shield.phoenixcore.current_shield", "Shield Status: %s"); // %s will be "Normal", "Inactive", etc.
        provider.add("shield.phoenixcore.health", "Health: %s"); // %s will be shield health value
        provider.add("shield.phoenixcore.cooldown", "Cooldown: %s seconds");
        provider.add("jade.phoenixcore.shield_state", "Shield State: %s");
        // --- NEW KEYS ADDED BELOW ---
        provider.add("jade.phoenixcore.shield_health", "Health: %s");
        provider.add("config.jade.plugin_phoenixcore.plasma_furnace_info", "High-Pressure Plasma Arc Furnace Info");
        provider.add("jade.phoenixcore.shield_cooldown", "Cooldown: %s seconds");
        // --------------------------------------
        provider.add("jade.phoenixcore.plasma_boost_active", "Plasma Boost: %s Active");
        provider.add("jade.phoenixcore.plasma_boost_duration", "Duration Multiplier: %s");
        provider.add("jade.phoenixcore.no_plasma_boost", "No Plasma Catalyst");
        multiLang(provider, "tooltip.phoenixcore.shield_stability_hatch",
                "Outputs shield stability",
                "as a redstone signal.");
        multiLang(provider, "gtceu.placeholder_info.shieldStability",
                "Returns the stability of the shield.",
                "Note that not having a shield projected may result in nonsense values of integrity.",
                "Usage:",
                "  {shieldStability} -> shield integrity: (integrity, in percent)");
    }

    protected static void multiLang(RegistrateLangProvider provider, String key, String... values) {
        for (var i = 0; i < values.length; i++) {
            provider.add(getSubKey(key, i), values[i]);
        }
    }

    protected static String getSubKey(String key, int index) {
        return key + "." + index;
    }
}
