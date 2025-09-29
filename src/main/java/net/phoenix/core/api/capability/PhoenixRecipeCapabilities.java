package net.phoenix.core.api.capability;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

public class PhoenixRecipeCapabilities {

    public static final SourceRecipeCapability SOURCE = SourceRecipeCapability.CAP;

    public static void init() {
        GTRegistries.RECIPE_CAPABILITIES.register(SOURCE.name, SOURCE);
    }
}
