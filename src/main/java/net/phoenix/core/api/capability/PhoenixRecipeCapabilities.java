package net.phoenix.core.api.capability;

import com.gregtechceu.gtceu.api.capability.recipe.BlockStateRecipeCapability;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

public class PhoenixRecipeCapabilities {

    public static final MicroverseRecipeCapability MICROVERSE = MicroverseRecipeCapability.CAP;
    public static final ShieldRecipeCapability SHIELDTYPES = ShieldRecipeCapability.CAP;

    public static void init() {
        GTRegistries.RECIPE_CAPABILITIES.register(MICROVERSE.name, MICROVERSE);
    GTRegistries.RECIPE_CAPABILITIES.register(SHIELDTYPES.name, SHIELDTYPES);
    }
}