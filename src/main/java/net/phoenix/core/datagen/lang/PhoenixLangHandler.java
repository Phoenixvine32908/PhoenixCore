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
        provider.add("item.gtceu.tool.ev_screwdriver", "%s Screwdriver (EV)");
        provider.add("item.gtceu.tool.luv_drill", "%s Drill (LuV}");
        provider.add("item.gtceu.tool.mv_screwdriver", "%s Screwdriver (MV)");
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
    }

    protected static String getSubKey(String key, int index) {
        return key + "." + index;
    }
}