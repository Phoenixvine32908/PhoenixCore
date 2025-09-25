package net.phoenix.core.api.capability;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerInteger;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.phoenix.core.api.recipe.lookup.MapSourceIngredient;

import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class SourceRecipeCapability extends RecipeCapability<Integer> {

    public static final SourceRecipeCapability CAP = new SourceRecipeCapability();

    protected SourceRecipeCapability() {
        super("source", 0x3BA7FFFF, true, 10, SerializerInteger.INSTANCE);
    }

    @Override
    public Integer copyInner(Integer content) {
        return content;
    }

    @Override
    public Integer copyWithModifier(Integer content, ContentModifier modifier) {
        return modifier.apply(content);
    }

    @Override
    public @Nullable List<AbstractMapIngredient> getDefaultMapIngredient(Object ingredient) {
        if (ingredient instanceof Integer source) {
            return MapSourceIngredient.convertToMapIngredient(source);
        }
        return null;
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
        // optional, fallback to parent
        return super.compressIngredients(ingredients);
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    @Override
    public void addXEIInfo(WidgetGroup group, int xOffset, GTRecipe recipe,
                           List<Content> contents, boolean perTick,
                           boolean isInput, MutableInt yOffset) {
        int source = contents.stream().map(Content::getContent).mapToInt(SourceRecipeCapability.CAP::of).sum();
        if (isInput) {
            group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                    LocalizationUtils.format("phoenixcore.recipe.source_in", source + (perTick ? "/t" : ""))));
        } else {
            group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                    LocalizationUtils.format("phoenixcore.recipe.source_out", source + (perTick ? "/t" : ""))));
        }
    }
}
