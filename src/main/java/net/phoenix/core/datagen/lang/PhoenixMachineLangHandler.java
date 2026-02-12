package net.phoenix.core.datagen.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class PhoenixMachineLangHandler {

    public static void init(RegistrateLangProvider provider) {
        // Tesla & Laser Tech
        provider.add("emi_info.phoenixcore.required_shield", "Required Shield: %s");
        provider.add("emi_info.phoenixcore.shield_heal", "Shield Health Restored: +%s");
        provider.add("emi_info.phoenixcore.shield_damage", "Shield Damage Applied: -%s");

        provider.add("tooltip.phoenixcore.tesla_hatch.laser_input",
                "§bOptical Collimator§r: Concentrates energy into a coherent Tesla-Laser beam.");
        provider.add("tooltip.phoenixcore.tesla_hatch.laser_output",
                "§bPhotonic Receptor§r: Decodes high-frequency laser flux back into EU.");
        provider.add("tooltip.phoenixcore.tesla_hatch.input",
                "§bWireless Transmitter§r: Siphons energy into the Tesla Cloud.");
        provider.add("tooltip.phoenixcore.tesla_hatch.output",
                "§bWireless Receiver§r: Broadcasts energy from the Tesla Cloud.");
        provider.add("tooltip.phoenixcore.tesla_hatch.lore", "§6Nevvonian Core Tech: Frequency Locked.");

        provider.add("tech.phoenixcore.laser.input.low", "Tesla Optical Collimator");
        provider.add("tech.phoenixcore.laser.input.mid", "Tesla Optical Collimation Grid");
        provider.add("tech.phoenixcore.laser.input.high", "Tesla Phased Beam Matrix");

        provider.add("tech.phoenixcore.laser.output.low", "Tesla Photonic Coalescer");
        provider.add("tech.phoenixcore.laser.output.mid", "Tesla Photonic Coalescence Array");
        provider.add("tech.phoenixcore.laser.output.high", "Tesla Photonic Coalescence Matrix");

        // Source System
        provider.add("gui.phoenixcore.source_hatch.label.import", "Source Input Hatch");
        provider.add("gui.phoenixcore.source_hatch.label.export", "Source Output Hatch");
        provider.add("gui.phoenixcore.source_hatch.source", "Source Stored: %s");
        provider.add("phoenix.core.recipe.source_in", "Source Necessary: %s.");
        provider.add("phoenix.core.recipe.source_out", "Source Given: %s.");
        provider.add("tooltip.phoenixcore.source_hatch.consumption", "§cMax Source Consumption:§d %s");
        provider.add("tooltip.phoenixcore.source_hatch.capacity", "§cMax Source Capacity:§d %s");

        // Recipe Types
        provider.add("gtceu.recipe_type.phoenixcore.high_performance_breeder_reactor",
                "High-Performance Breeder Reactor");
        provider.add("gtceu.recipe_type.phoenixcore.honey_chamber", "Honey Chamber");
        provider.add("gtceu.recipe_type.phoenixcore.please", "Please Multiblock");
        provider.add("gtceu.recipe_type.phoenixcore.simulated_colony", "Simulated Colony");
        provider.add("gtceu.recipe_type.phoenixcore.comb_decanting", "Comb Decanter");
        provider.add("gtceu.recipe_type.phoenixcore.swarm_nurturing", "Swarm Nurturing Chamber");
        provider.add("gtceu.recipe_type.phoenixcore.apis_progenitor", "Apis Progenitor");

        // Jade Integration
        provider.add("config.jade.plugin_phoenixcore.source_hatch_info", "Source Stored: %s");
        provider.add("config.jade.plugin_phoenixcore.plasma_furnace_info", "High-Pressure Plasma Arc Furnace Info");
        provider.add("config.jade.plugin_phoenixcore.tesla_network_info", "Tesla Network Information");
        provider.add("jade.phoenixcore.plasma_boost_active", "Plasma Boost: %s Active");
        provider.add("jade.phoenixcore.no_plasma_boost", "No Plasma Catalyst");
        provider.add("jade.phoenixcore.tesla_stored", "Stored: ");
        provider.add("jade.phoenixcore.tesla_receiving", "Receiving: %s EU/t");
        provider.add("jade.phoenixcore.tesla_providing", "Providing: %s EU/t");

        // Multi-line Tooltips
        PhoenixLangHandler.multiLang(provider, "tooltip.phoenixcore.shield_stability_hatch", "Outputs shield stability",
                "as a redstone signal.");
    }
}
