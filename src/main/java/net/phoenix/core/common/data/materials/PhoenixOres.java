package net.phoenix.core.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;

import net.phoenix.core.PhoenixCore;

public class PhoenixOres {

    // Material Declarations
    public static Material NEVVONIAN_IRON;
    public static Material FLUORITE;
    public static Material IGNISIUM;
    public static Material POLARITY_FLIPPED_BISMUTHITE;
    public static Material VOIDGLASS_SHARD;
    public static Material CRYSTALLIZED_FLUXSTONE;

    public static void register() {
        // nevvonian_iron: .ore().ingot().color(0x7A687F).iconSet("diamond")
        NEVVONIAN_IRON = new Material.Builder(
                PhoenixCore.id("nevvonian_iron"))
                .color(0x7A687F)
                .ingot() // Sets properties for ingot form
                .ore()    // Sets properties for ore form (includes dust)
                .iconSet(MaterialIconSet.DIAMOND) // Explicitly set icon set to DIAMOND
                .buildAndRegister();

        // fluorite: .gem().ore().color(0x0c9949).iconSet("diamond").components(...)
        FLUORITE = new Material.Builder(
                PhoenixCore.id("fluorite"))
                .color(0x0c9949)
                .gem()  // Sets properties for gem form
                .ore()  // Sets properties for ore form (includes dust)
                .iconSet(MaterialIconSet.DIAMOND)
                .buildAndRegister();

        // ignisium: .ore().dust().color(0xFF4500).iconSet("diamond")
        IGNISIUM = new Material.Builder(
                PhoenixCore.id("ignisium"))
                .color(0xFF4500)
                .dust() // Sets properties for dust form
                .ore()  // Sets properties for ore form
                .iconSet(MaterialIconSet.DIAMOND)
                .buildAndRegister();

        // polarity_flipped_bismuthite: .ore().dust().color(0xe4d6ff).iconSet("diamond").flags(DISABLE_DECOMPOSITION)
        POLARITY_FLIPPED_BISMUTHITE = new Material.Builder(
                PhoenixCore.id("polarity_flipped_bismuthite"))
                .color(0xe4d6ff)
                .dust()
                .ore()
                .iconSet(MaterialIconSet.DIAMOND)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION) // Custom flag applied at the end
                .buildAndRegister();

        // voidglass_shard: .ore().gem().color(0x6a00aa).iconSet("diamond").flags(DISABLE_DECOMPOSITION)
        VOIDGLASS_SHARD = new Material.Builder(
                PhoenixCore.id("voidglass_shard"))
                .color(0x6a00aa)
                .gem()
                .ore()
                .iconSet(MaterialIconSet.DIAMOND)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION) // Custom flag applied at the end
                .buildAndRegister();

        // crystallized_fluxstone: .ore().dust().color(0xd4bfff).iconSet("diamond")
        CRYSTALLIZED_FLUXSTONE = new Material.Builder(
                PhoenixCore.id("crystallized_fluxstone"))
                .color(0xd4bfff)
                .dust()
                .ore()
                .iconSet(MaterialIconSet.DIAMOND)
                .buildAndRegister();
    }
}
