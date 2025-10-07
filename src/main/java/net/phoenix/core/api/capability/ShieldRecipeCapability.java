package net.phoenix.core.api.capability;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.resources.language.I18n;
import net.phoenix.core.common.machine.multiblock.Shield.ShieldTypes;

import com.mojang.serialization.Codec;
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
        // 1. Display Required Shield State (from the .input() call)
        // Only shows up if this is an INPUT requirement.
        if (isInput) {
            ShieldTypes shieldType = (ShieldTypes) contents.get(0).getContent();
            group.addWidget(
                    new LabelWidget(xOffset + 3, yOffset.addAndGet(10),
                            I18n.get("emi_info.phoenixcore.required_shield", I18n.get(shieldType.langKey))));
        }

        // 2. Display Shield Health Change (from the .addData() call)
        // The heal amount is stored in the generic recipe data map.
        if (recipe.data.contains("shield_health_change")) {
            int healthChange = recipe.data.getInt("shield_health_change");

            // This is the core logic: Display the amount being added (or subtracted)
            if (healthChange != 0) {
                // Determine color and language key based on positive (heal) or negative (damage)
                String langKey = healthChange > 0 ? "emi_info.phoenixcore.shield_heal" :
                        "emi_info.phoenixcore.shield_damage";
                String colorCode = healthChange > 0 ? "§a" : "§c"; // Green for heal, Red for damage
                int amount = Math.abs(healthChange);

                group.addWidget(
                        new LabelWidget(xOffset + 3, yOffset.addAndGet(10),
                                // Displays something like: "Shield Health Restored: +§a500"
                                I18n.get(langKey, colorCode + amount)));
            }
        }
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
