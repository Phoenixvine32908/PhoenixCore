package net.phoenix.core.api.capability;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.mojang.serialization.Codec;
import net.minecraft.client.resources.language.I18n;
// Only one import is needed
import net.phoenix.core.common.machine.multiblock.Shield;
import net.phoenix.core.common.machine.multiblock.Shield.ShieldTypes; // <-- **Crucial: Import the inner enum**
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;

// Use ShieldTypes as the generic type argument
public class ShieldRecipeCapability extends RecipeCapability<ShieldTypes> {

    public static ShieldRecipeCapability CAP = new ShieldRecipeCapability();

    protected ShieldRecipeCapability() {
        // Updated type: ShieldTypes.class is not allowed here as RecipeCapability uses the generic type T
        // We'll keep the name as "Shield" for the capability ID.
        super("Shield", 0xFF00FFFF, false, 11, SerializerShield.INSTANCE);
    }

    @Override
    // The generic type is ShieldTypes, so the argument and return type should be ShieldTypes
    public ShieldTypes copyInner(ShieldTypes content) {
        return content;
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    @Override
    public void addXEIInfo(WidgetGroup group, int xOffset, GTRecipe recipe, List<Content> contents, boolean perTick,
                           boolean isInput, MutableInt yOffset) {
        // Cast the content to the correct enum type: ShieldTypes
        ShieldTypes shieldType = (ShieldTypes) contents.get(0).getContent();
        group.addWidget(
                new LabelWidget(xOffset + 3, yOffset.addAndGet(10),
                        // Access the langKey directly from the enum instance
                        I18n.get("emi_info.phoenixcore.required_shield", I18n.get(shieldType.langKey))));

        // You might want to remove the super call if you only want to display your custom info
        // super.addXEIInfo(group, xOffset, recipe, contents, perTick, isInput, yOffset);
    }

    // Updated inner class to work with ShieldTypes
    private static class SerializerShield implements IContentSerializer<ShieldTypes> { // <-- Use ShieldTypes

        public static SerializerShield INSTANCE = new SerializerShield();

        // Codec must use the correct getter/field from the ShieldTypes enum
        public static Codec<ShieldTypes> CODEC = Codec.INT.xmap(ShieldTypes::getShieldFromKey, ShieldTypes::getKey);

        @Override
        public ShieldTypes of(Object o) { // <-- Use ShieldTypes
            if (!(o instanceof ShieldTypes shieldType)) {
                return null;
            }
            return shieldType;
        }

        @Override
        public ShieldTypes defaultValue() { // <-- Use ShieldTypes
            // Must return an actual enum instance, e.g., ShieldTypes.NORMAL
        return ShieldTypes.INACTIVE; // Assuming NORMAL is the default/fallback
        }

        @Override
        public Class<ShieldTypes> contentClass() { // <-- Use ShieldTypes
            return ShieldTypes.class;
        }

        @Override
        public Codec<ShieldTypes> codec() { // <-- Use ShieldTypes
            return CODEC;
        }
    }
}