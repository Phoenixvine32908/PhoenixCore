package net.phoenix.core.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DisableBeeRecipes {

    public static void main(String[] args) {
        // Use the manually provided list of bee names.
        List<String> beeNames = Arrays.asList(
                "ashy_mining_bee",
                "blue_banded_bee",
                "green_carpenter",
                "yellow_carpenter",
                "chocolate_mining_bee",
                "collector",
                "hoarder",
                "farmer",
                "creebee",
                "cubee",
                "digger_bee",
                "leafcutter_bee",
                "mason_bee",
                "neon_cuckoo_bee",
                "nomad_bee",
                "resin_bee",
                "pitchblende_bee",
                "lumber",
                "quarry",
                "rancher",
                "copper_bee",
                "reed_bee",
                "sweat_bee",
                "bumble_bee",
                "ghostly_bee",
                "lepodolite_bee",
                "exp_bee",
                "arcane_bee",
                "cinnabar_bee",
                "topaz_bee",
                "amethyst_bee",
                "blazing_bee",
                "prismarine_bee",
                "sculk_bee",
                "realgar_bee",
                "rune_bee",
                "pyrope_bee",
                "sponge_bee",
                "zinc_bee",
                "tin_bee",
                "baz_bee",
                "sussy_bee",
                "diamond_bee",
                "awakened_bee",
                "iron_bee",
                "fluorite_bee",
                "warped_shroombee",
                "brown_shroombee",
                "scheelite_bee",
                "frosty_bee",
                "rubee",
                "red_shroombee",
                "sapphire_bee",
                "stibnite_bee",
                "opal_bee",
                "withered_bee",
                "cheezy_bee",
                "lapis_bee",
                "electrotine_bee",
                "constantan_bee",
                "redstone_bee",
                "sugarbag_bee",
                "skele_bee",
                "zombee",
                "silky_bee",
                "copper_bee",
                "biz_bee",
                "niter_bee",
                "slimy_bee",
                "coal_bee",
                "illmenite_bee",
                "silicon_bee",
                "galena_bee",
                "menril_bee",
                "crystalite_bee",
                "sodalite_bee",
                "water_bee",
                "gold_bee",
                "obsidan_bee",
                "cobaltite_bee",
                "bauxite_bee",
                "bitzbee",
                "salty_bee",
                "rocked_bee",
                "steamy_bee",
                "supa_bee",
                "pepto_beesmol",
                "desh_bee",
                "crimson_shroombee",
                "silver_bee",
                "bee_of_infinity",
                "amber_bee",
                "tungstate_bee",
                "emerald_bee",
                "tricalcium_phosphate_bee",
                "spatial_bee",
                "arcanus_bee",
                "magmatic_bee",
                "nickel_bee",
                "fluix_bee",
                "malachite_bee",
                "lead_bee",
                "invar_bee",
                "sticky_resin_bee",
                "thorium_bee",
                "graphite_bee",
                "sphlearite_bee",
                "ancient_bee",
                "ender_bee",
                "apatie_bee",
                "acidic_bee",
                "chromite_bee",
                "pyrolusite_bee",
                "platinum_bee",
                "bismuth_bee",
                "glowing_bee",
                "bastnasite_bee",
                "tetrahedrite_bee",
                "titanium_bee",
                "sulfur_bee",
                "oilsands_bee",
                "cobalt_bee",
                "wannabee",
                "tantalite_bee",
                "barite_bee",
                "vanadium_magnetite_bee",
                "draconic_bee",
                "pyrochlore_bee",
                "molybdenum_bee",
                "voidglass_shard_bee",
                "crystallized_fluxstone_bee",
                "ignisium_bee",
                "sky_steel_bee",
                "radioactive_bee",
                "neutronium_bee",
                "osmium_bee",
                "palladium_bee",
                "deorum_bee",
                "tungsten_bee",
                "calorite_bee",
                "iridium_bee",
                "sheldonite_bee",
                "neodymium_bee",
                "ostrum_bee",
                "chaos_bee",
                "draconium_bee",
                "stellarite_bee",
                "oily_bee",
                "naquadah_bee",
                "entropy_bee",
                "polarity_flipped_bismuthite_bee",
                "dye_bee");

        // 2. Define the base directory where the new JSON files will be created.
        // This is the path you provided.
        String baseDirectory = "C:\\Users\\conno\\curseforge\\minecraft\\Instances\\Phoenix Forge Technologies\\kubejs\\assets\\productivebees\\recipes\\bee_breeding\\";

        // 3. Define the template JSON content.
        String jsonContent = "{}";

        // Ensure the directory exists.
        File directory = new File(baseDirectory);
        if (!directory.exists()) {
            System.out.println("Creating directory: " + baseDirectory);
            directory.mkdirs();
        }

        // 4. Iterate through the list of bees and create a file for each.
        for (String beeName : beeNames) {
            // Construct the full file path.
            String fileName = beeName + ".json";
            String filePath = baseDirectory + fileName;

            // 5. Write the file.
            try (FileWriter fileWriter = new FileWriter(filePath)) {
                fileWriter.write(jsonContent);
                System.out.println("Successfully created and wrote to: " + filePath);
            } catch (IOException e) {
                // 6. Handle potential errors.
                System.err.println("An error occurred while writing the file for " + beeName + ":");
                e.printStackTrace();
            }
        }

        System.out.println("\nProcess complete. Check the directory for the new JSON files.");
    }
}
