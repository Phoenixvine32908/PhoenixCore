package net.phoenix.core.datagen.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class PhoenixLangHandler {

    public static void init(RegistrateLangProvider provider) {
        provider.add("phoenixcore.tooltip.hyper_machine_coolant1", "%s: %sx CWU/t");
        provider.add("phoenixcore.tooltip.hyper_machine_coolant2", "%s: %sx CWU/t");
        provider.add("phoenixcore.tooltip.hyper_machine_coolant_base", "%s: %sx CWU/t");
        provider.add("phoenixcore.tooltip.hyper_machine_coolant3", "%s: %sx CWU/t");
        provider.add("phoenixcore.tooltip.hyper_machine_purpose",
                "An upgraded HPCA that uses %s, %s, or %s to provide cooling");
        provider.add("phoenixcore.tooltip.requires_fluid", "Needs: %s");
        provider.add("tagprefix.nanites", "%s Nanites");
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
    }

    protected static String getSubKey(String key, int index) {
        return key + "." + index;
    }
}
