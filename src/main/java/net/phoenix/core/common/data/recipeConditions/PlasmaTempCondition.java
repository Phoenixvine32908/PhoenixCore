package net.phoenix.core.common.data.recipeConditions;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.core.common.data.PhoenixMaterialRegistry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

@NoArgsConstructor
public class PlasmaTempCondition extends RecipeCondition {

    public static final Codec<PlasmaTempCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition
            .isReverse(instance).and(
                    Codec.STRING.fieldOf("fluidString").forGetter(PlasmaTempCondition::getFluidString))
            .and(
                    Codec.STRING.fieldOf("displayName").forGetter(PlasmaTempCondition::getDisplayName))
            .apply(instance, PlasmaTempCondition::new));

    @Getter
    private @NotNull String fluidString = "";
    @Getter
    private @NotNull String displayName = "Unknown Plasma";

    private @Nullable Fluid cachedFluid = null;

    public PlasmaTempCondition(boolean isReverse, @NotNull String fluidString, @NotNull String displayName) {
        super(isReverse);
        this.fluidString = fluidString;
        this.displayName = displayName;
    }

    public static PlasmaTempCondition of(@NotNull Fluid fluid) {
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
        if (id == null) throw new IllegalArgumentException("Fluid has no registry ID: " + fluid);

        Material material = PhoenixMaterialRegistry.getMaterial(fluid);
        String name = material != null ? I18n.get(material.getDefaultTranslation()) : id.getPath().replace('_', ' ');

        return new PlasmaTempCondition(false, id.toString(), name);
    }

    public static PlasmaTempCondition of(@NotNull Material material) {
        Fluid fluid = material.getFluid();
        if (fluid == null || fluid == Fluids.EMPTY) {
            throw new IllegalArgumentException("Material " + material + " does not have a valid plasma fluid.");
        }
        return of(fluid);
    }

    public static PlasmaTempCondition of(@NotNull String fluidId) {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidId));
        Material material = fluid != null ? PhoenixMaterialRegistry.getMaterial(fluid) : null;

        String name = material != null ? I18n.get(material.getDefaultTranslation()) :
                new ResourceLocation(fluidId).getPath().replace('_', ' ');

        return new PlasmaTempCondition(false, fluidId, name);
    }

    public Fluid getFluid() {
        if (cachedFluid == null) {
            cachedFluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidString));
        }
        return cachedFluid;
    }

    @Override
    public Component getTooltips() {
        Fluid fluid = getFluid();
        if (fluid != null) {
            Material material = PhoenixMaterialRegistry.getMaterial(fluid);

            String localizedName = material != null ? I18n.get(material.getDefaultTranslation()) :
                    new ResourceLocation(fluidString).getPath().replace('_', ' ');

            // Capitalize first letters if fallback name is used
            if (material == null) {
                localizedName = Arrays.stream(localizedName.split(" "))
                        .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                        .collect(Collectors.joining(" "));
            }

            int color = material != null ? material.getMaterialARGB() : 0xFFFFFF;

            Component nameComponent = Component.literal(localizedName)
                    .withStyle(style -> style.withColor(TextColor.fromRgb(color)));

            return Component.translatable("phoenixcore.tooltip.requires_plasma", nameComponent);
        }

        return Component.literal("Requires an unknown plasma fluid.");
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Fluid requiredFluid = getFluid();
        if (requiredFluid == null) return false;

        return recipeLogic.getMachine().getTraits().stream()
                .filter(trait -> trait instanceof NotifiableFluidTank)
                .map(trait -> (NotifiableFluidTank) trait)
                .anyMatch(tank -> {
                    for (int i = 0; i < tank.getTanks(); i++) {
                        FluidStack stack = tank.getFluidInTank(i);
                        if (!stack.isEmpty() && stack.getFluid().equals(requiredFluid)) {
                            return true;
                        }
                    }
                    return false;
                });
    }

    @Override
    public RecipeCondition createTemplate() {
        return new PlasmaTempCondition();
    }

    @Override
    public RecipeConditionType<?> getType() {
        if (TYPE == null) {
            throw new IllegalStateException("PlasmaTempCondition.TYPE not registered yet!");
        }
        return TYPE;
    }

    public static RecipeConditionType<PlasmaTempCondition> TYPE;

    public static void register() {
        TYPE = GTRegistries.RECIPE_CONDITIONS.register("plasma_temp_condition",
                new RecipeConditionType<>(PlasmaTempCondition::new, PlasmaTempCondition.CODEC));
    }
}
