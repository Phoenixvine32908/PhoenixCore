package net.phoenix.core.api.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Lazy;
import net.phoenix.core.PhoenixAPI;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;

public interface IFissionModeratorType {

    /**
     * @return The Unique Name of the Fission Cooler
     */
    @NotNull
    String getName();

    /**
     * @return the
     */
    int getEUBoost();

    /**
     * This is used for the amount of boost to the reactor's recipe
     *
     * @return the fuel discount of the Fission Cooler
     */
    int getFuelDiscount();

    /**
     * @return the {@link Material} of the Fission Cooler if it has one, otherwise {@code GTMaterials.NULL}
     */
    Material getMaterial();

    /**
     * This is used for the
     *
     * @return the tier of the moderator
     */
    int getTier();

    /**
     * @return the {@link ResourceLocation} defining the base texture of the fission cooler
     */
    ResourceLocation getTexture();

    Lazy<IFissionModeratorType[]> ALL_FISSION_MODERATORS_SORTED = Lazy
            .of(() -> PhoenixAPI.FISSION_MODERATORS.keySet().stream()
                    .sorted(Comparator.comparingInt(IFissionModeratorType::getFuelDiscount))
                    .toArray(IFissionModeratorType[]::new));

    @Nullable
    static IFissionModeratorType getMinRequiredType(int requiredTemperature) {
        return Arrays.stream(ALL_FISSION_MODERATORS_SORTED.get())
                .filter(moderatorType -> moderatorType.getFuelDiscount() >= requiredTemperature)
                .findFirst().orElse(null);
    }
}
