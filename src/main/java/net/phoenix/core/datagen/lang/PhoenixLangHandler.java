package net.phoenix.core.datagen.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class PhoenixLangHandler {

    public static void init(RegistrateLangProvider provider) {
        provider.add("emi_info.phoenixcore.required_shield", "Required Shield: %s");
        provider.add("shield.phoenixcore.type.normal", "Normal");
        provider.add("shield.phoenixcore.type.inactive", "Inactive");
        provider.add("shield.phoenixcore.type.decayed", "Decayed");
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

        // Add these to PhoenixLangHandler#init
        provider.add("tooltip.phoenixcore.tesla_hatch.laser_input",
                "§bOptical Collimator§r: Concentrates energy into a coherent Tesla-Laser beam.");
        provider.add("tooltip.phoenixcore.tesla_hatch.laser_output",
                "§bPhotonic Receptor§r: Decodes high-frequency laser flux back into EU.");

// Laser-Specific Technical Names (Optional: for use in getLaserName logic)
        provider.add("tech.phoenixcore.laser.input.low", "Tesla Optical Collimator");
        provider.add("tech.phoenixcore.laser.input.mid", "Tesla Optical Collimation Grid");
        provider.add("tech.phoenixcore.laser.input.high", "Tesla Phased Beam Matrix");

        provider.add("tech.phoenixcore.laser.output.low", "Tesla Photonic Coalescer");
        provider.add("tech.phoenixcore.laser.output.mid", "Tesla Photonic Coalescence Array");
        provider.add("tech.phoenixcore.laser.output.high", "Tesla Photonic Coalescence Matrix");

        provider.add("jade.phoenixcore.fission_safe", "Status: SAFE");
        provider.add("jade.phoenixcore.fission_meltdown_timer", "Meltdown in %s seconds");
        provider.add("jade.phoenixcore.fission_no_coolant", "Coolant Tanks EMPTY");
        provider.add("jade.phoenixcore.fission_low_cooling", "Insufficient Cooling Power");
        provider.add("config.jade.plugin_phoenixcore.fission_machine_info", "Fission Machine Info");

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

        provider.add("block.phoenixcore.fission_moderator.shift", "Hold §eShift§r for moderator details");
        provider.add("block.phoenixcore.fission_moderator.info_header", "§7--- Moderator Information ---");
        provider.add("block.phoenixcore.fission_moderator.boost", "§aEU Output Boost: §f%s%%");
        provider.add("block.phoenixcore.fission_moderator.fuel_discount", "§bFuel Usage Reduction: §f%s%%");

        provider.add("phoenix.multiblock.pattern.info.multiple_coolers",
                "Accepts multiple Fission Cooler Blocks. Cooling power is additive.");
        provider.add("phoenix.multiblock.pattern.info.multiple_moderators",
                "Accepts multiple Fission Moderator Blocks. EU Boost/Fuel Discount is additive.");

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
        provider.add("material.phoenixcore.boron_carbide", "§5Boron Carbide");
        provider.add("material.phoenixcore.niobium_modified_silicon_carbide", "§5Niobium Modified Silicon Carbide");
        provider.add("material.phoenixcore.frost", "§bFrost");
        provider.add("material.phoenixcore.wax_melting_catalyst", "Wax Melting Catalyst");
        provider.add("material.phoenixcore.sugar_water", "Sugar Water");
        provider.add("material.phoenixcore.eighty_five_percent_pure_nevvonian_steel",
                "§6Eighty Five Percent Pure Nevvonian Steel");
        provider.add("material.phoenixcore.phoenix_enriched_naquadah",
                "§6Phoenix Enriched Naquadah");
        provider.add("material.phoenixcore.ignisium", "§4Ignisium");
        provider.add("material.phoenixcore.crystallized_fluxstone", "§dCrystallized Fluxstone");
        provider.add("material.phoenixcore.nevvonian_iron", "§7Nevvonian Iron");
        provider.add("material.phoenixcore.fluorite", "§aFluorite");
        provider.add("material.phoenixcore.polarity_flipped_bismuthite", "§bPolarity Flipped Bismuthite");
        provider.add("material.phoenixcore.voidglass_shard", "§5Voidglass Shard");
        provider.add("phoenixcore.tooltip.hyper_machine_1", "Each Coolant provides a boost:");
        provider.add("gtceu.recipe_type.phoenixcore.high_performance_breeder_reactor",
                "High-Performance Breeder Reactor");
        provider.add("gtceu.recipe_type.phoenixcore.advanced_pressurized_fission_reactor",
                "Advanced Pressurized Fission Reactor");
        provider.add("gtceu.recipe_type.phoenixcore.pressurized_fission_reactor", "Pressurized Fission Reactor");
        provider.add("gtceu.recipe_type.phoenixcore.heat_exchanging", "Heat Exchanger");
        provider.add("gtceu.recipe_type.phoenixcore.honey_chamber", "Honey Chamber");
        provider.add("gtceu.recipe_type.phoenixcore.please", "Please Multiblock");
        provider.add("gtceu.recipe_type.phoenixcore.simulated_colony", "Simulated Colony");
        provider.add("gtceu.recipe_type.phoenixcore.comb_decanting", "Comb Decanter");
        provider.add("gtceu.recipe_type.phoenixcore.swarm_nurturing", "Swarm Nurturing Chamber");
        provider.add("gtceu.recipe_type.phoenixcore.apis_progenitor", "Apis Progenitor");
        provider.add("block.monilabs.tesla_battery.tooltip_empty", "§7A hollow casing. Provides no storage.");
        provider.add("block.monilabs.tesla_battery.tooltip_filled", "§aCapacity: §f%s EU");
        provider.add("tooltip.phoenixcore.tesla_hatch.input",
                "§bWireless Transmitter§r: Siphons energy into the Tesla Cloud.");
        provider.add("tooltip.phoenixcore.tesla_hatch.output",
                "§bWireless Receiver§r: Broadcasts energy from the Tesla Cloud.");

        provider.add("block.phoenixcore.tesla_battery.tooltip_empty", "§7A hollow casing. Provides no storage.");
        provider.add("block.phoenixcore.tesla_battery.tooltip_filled", "§aCapacity: §f%s EU");

        provider.add("tooltip.phoenixcore.tesla_hatch.lore", "§6Nevvonian Core Tech: Frequency Locked.");

        // Tower UI Component
        provider.add("gtceu.multiblock.tesla.stored", "Network Power: %s / %s EU");
        provider.add("shield.phoenixcore.current_shield", "Shield Status: %s");
        provider.add("shield.phoenixcore.health", "Health: %s");
        provider.add("shield.phoenixcore.cooldown", "Cooldown: %s seconds");
        provider.add("jade.phoenixcore.shield_state", "Shield State: %s");
        provider.add("jade.phoenixcore.shield_health", "Health: %s");
        provider.add("config.jade.plugin_phoenixcore.plasma_furnace_info", "High-Pressure Plasma Arc Furnace Info");
        provider.add("jade.phoenixcore.plasma_boost_active", "Plasma Boost: %s Active");
        provider.add("jade.phoenixcore.plasma_boost_duration", "Duration Multiplier: %s");
        provider.add("jade.phoenixcore.no_plasma_boost", "No Plasma Catalyst");
        // Tesla Network Jade Keys
        provider.add("jade.phoenixcore.tesla_stored", "Stored: ");
        provider.add("config.jade.plugin_phoenixcore.tesla_network_info", "Tesla Network Information");

        // Binder UI & Additional Keys
        provider.add("item.phoenixcore.tesla_binder.linked", "§aLinked to: §f%s");
        provider.add("item.phoenixcore.tesla_binder.unlinked", "§cNot Linked");
        provider.add("item.phoenixcore.tesla_binder.frequency", "§7Frequency: §b%s");
        provider.add("jade.phoenixcore.tesla_team", "Network: %s");
        provider.add("jade.phoenixcore.tesla_receiving", "Receiving: %s EU/t");
        provider.add("jade.phoenixcore.tesla_providing", "Providing: %s EU/t");
        provider.add("jade.phoenixcore.tesla_active_connections", "Active Connections: %s");
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
