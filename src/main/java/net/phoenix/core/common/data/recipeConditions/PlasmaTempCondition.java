package net.phoenix.core.common.data.recipeConditions;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class PlasmaTempCondition extends RecipeCondition {

    private final Fluid requiredPlasma;

    // Codec for deserialization from JSON
    public static final Codec<PlasmaTempCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("fluid")
                    .forGetter(cond -> ForgeRegistries.FLUIDS.getKey(cond.requiredPlasma)),
            Codec.BOOL.optionalFieldOf("is_reverse", false).forGetter(cond -> cond.isReverse))
            .apply(instance, (fluidId, isReverse) -> new PlasmaTempCondition(isReverse,
                    ForgeRegistries.FLUIDS.getValue(fluidId))));

    // No-arg constructor required for registration
    public PlasmaTempCondition() {
        this(false, net.minecraft.world.level.material.Fluids.EMPTY);
    }

    // Convenience factory method
    public static PlasmaTempCondition of(Fluid plasma) {
        return new PlasmaTempCondition(plasma);
    }

    // Constructor with default isReverse = false
    public PlasmaTempCondition(Fluid requiredPlasma) {
        this(false, requiredPlasma);
    }

    // Full constructor
    public PlasmaTempCondition(boolean isReverse, Fluid requiredPlasma) {
        this.isReverse = isReverse;
        this.requiredPlasma = requiredPlasma;
    }

    public static RecipeConditionType<PlasmaTempCondition> TYPE;

    public static void register() {
        TYPE = GTRegistries.RECIPE_CONDITIONS.register("plasma_temp_condition",
                new RecipeConditionType<>(PlasmaTempCondition::new, PlasmaTempCondition.CODEC));
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        return recipeLogic.getMachine().getTraits().stream()
                .filter(trait -> trait instanceof NotifiableFluidTank)
                .map(trait -> (NotifiableFluidTank) trait)
                .anyMatch(tank -> {
                    for (int i = 0; i < tank.getTanks(); i++) {
                        FluidStack stack = tank.getFluidInTank(i);
                        if (!stack.isEmpty() && stack.getFluid().equals(requiredPlasma)) {
                            return true;
                        }
                    }
                    return false;
                });
    }

    @Override
    public RecipeConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public Component getTooltips() {
        return Component.literal("Requires plasma: " +
                ForgeRegistries.FLUIDS.getKey(requiredPlasma));
    }

    @Override
    public RecipeCondition createTemplate() {
        return new PlasmaTempCondition();
    }
}
