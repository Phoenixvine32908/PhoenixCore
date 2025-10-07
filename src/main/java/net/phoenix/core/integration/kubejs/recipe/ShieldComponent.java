package net.phoenix.core.integration.kubejs.recipe;

import net.phoenix.core.common.machine.multiblock.Shield;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;

public class ShieldComponent implements RecipeComponent<Shield.ShieldTypes> {

    @Override
    public Class<?> componentClass() {
        return Shield.ShieldTypes.class;
    }

    @Override
    public JsonElement write(RecipeJS recipeJS, Shield.ShieldTypes shieldType) {
        // Writes the enum key (integer) to the JSON
        return new JsonPrimitive(shieldType.key);
    }

    @Override
    public Shield.ShieldTypes read(RecipeJS recipeJS, Object o) {
        // Reads the object back from KubeJS or JSON
        if (o instanceof Number n) {
            // Assumes Shield.ShieldTypes has a static lookup method
            return Shield.ShieldTypes.getShieldFromKey(n.intValue());
        }
        if (o instanceof String s) {
            try {
                return Shield.ShieldTypes.valueOf(s.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        if (o instanceof Shield.ShieldTypes type) {
            return type;
        }
        return null;
    }
}
