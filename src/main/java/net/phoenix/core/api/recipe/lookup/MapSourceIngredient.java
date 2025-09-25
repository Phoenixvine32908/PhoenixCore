package net.phoenix.core.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import java.util.Collections;
import java.util.List;

public class MapSourceIngredient extends AbstractMapIngredient {

    public final int sourceAmount;

    public MapSourceIngredient(int sourceAmount) {
        this.sourceAmount = sourceAmount;
    }

    @Override
    protected int hash() {
        return MapSourceIngredient.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MapSourceIngredient;
    }

    @Override
    public String toString() {
        return "MapSourceIngredient{amount=" + sourceAmount + '}';
    }

    public static List<AbstractMapIngredient> convertToMapIngredient(int source) {
        return Collections.singletonList(new MapSourceIngredient(source));
    }
}
