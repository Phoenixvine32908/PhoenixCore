package net.phoenix.core.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.phoenix.core.common.machine.multiblock.Shield.ShieldTypes;

import java.util.List;

public class MapShieldIngredient extends AbstractMapIngredient {

    public final ShieldTypes shield; // <-- Change type to ShieldTypes

    public MapShieldIngredient(ShieldTypes shield) { // <-- Change type to ShieldTypes
        this.shield = shield;
    }

    @Override
    protected int hash() {
        return shield.key; // shield.key refers to the key on the ShieldTypes enum
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MapShieldIngredient other)) {
            return false;
        }
        return other.shield == shield;
    }

    // <-- Change method argument type to ShieldTypes
    public static List<AbstractMapIngredient> from(ShieldTypes ingredient) {
        return List.of(new MapShieldIngredient(ingredient));
    }
}
