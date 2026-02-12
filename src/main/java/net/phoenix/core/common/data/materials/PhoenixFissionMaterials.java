package net.phoenix.core.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.common.data.GTElements;

import net.phoenix.core.PhoenixCore;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;

public class PhoenixFissionMaterials {

    public static Material URANIUM_233, URANIUM_236, AMERICIUM_241, AMERICIUM_HEXAFLUORIDE, URANIUM_OXIDE;
    public static Material INERT_GAS_WASTE, IRRADIATED_THORIUM, SPENT_URANIUM_233, SPENT_URANIUM_235, DEPLETED_URANIUM;
    public static Material CRITICAL_STEAM, HOT_SODIUM_POTASSIUM, BORON_CARBIDE, ZIRCALLOY;

    public static void register() {
        URANIUM_233 = new Material.Builder(PhoenixCore.id("uranium_233"))
                .ingot().fluid().color(0x33FF33).secondaryColor(0x00CC00).element(PhoenixElements.URANIUM_233)
                .iconSet(MaterialIconSet.RADIOACTIVE).buildAndRegister();

        URANIUM_236 = new Material.Builder(PhoenixCore.id("uranium_236"))
                .ingot().fluid().color(0x33CCFF).secondaryColor(0x0099EE).element(PhoenixElements.URANIUM_236)
                .iconSet(MaterialIconSet.RADIOACTIVE).buildAndRegister();

        AMERICIUM_241 = new Material.Builder(PhoenixCore.id("americium_241"))
                .ingot().color(0xCDC9C9).secondaryColor(0x8B8B7A).element(PhoenixElements.AMERICIUM_241)
                .iconSet(MaterialIconSet.RADIOACTIVE).buildAndRegister();

        AMERICIUM_HEXAFLUORIDE = new Material.Builder(PhoenixCore.id("americium_hexafluoride"))
                .gas().color(0xFFFFFF).secondaryColor(0xADD8E6).iconSet(MaterialIconSet.DULL).buildAndRegister();

        URANIUM_OXIDE = new Material.Builder(PhoenixCore.id("uranium_oxide"))
                .fluid().color(0x00FF00).secondaryColor(0x000000).iconSet(MaterialIconSet.DULL).buildAndRegister();

        INERT_GAS_WASTE = new Material.Builder(PhoenixCore.id("inert_gas_waste"))
                .gas().color(0xC0C0C0).secondaryColor(0xD0D0D0).iconSet(MaterialIconSet.DULL).buildAndRegister();

        IRRADIATED_THORIUM = new Material.Builder(PhoenixCore.id("irradiated_thorium"))
                .ingot().fluid().dust().color(0x90A090).secondaryColor(0x708070).element(GTElements.Th)
                .iconSet(MaterialIconSet.RADIOACTIVE).buildAndRegister();

        SPENT_URANIUM_233 = new Material.Builder(PhoenixCore.id("spent_uranium_233"))
                .dust().ingot().color(0x503040).secondaryColor(0x705060).element(PhoenixElements.URANIUM_233)
                .iconSet(MaterialIconSet.RADIOACTIVE).buildAndRegister();

        SPENT_URANIUM_235 = new Material.Builder(PhoenixCore.id("spent_uranium_235"))
                .ingot().color(0x603030).secondaryColor(0x402020).element(GTElements.U235)
                .iconSet(MaterialIconSet.RADIOACTIVE).buildAndRegister();

        DEPLETED_URANIUM = new Material.Builder(PhoenixCore.id("depleted_uranium"))
                .ingot().color(0x555030).secondaryColor(0x333010).element(GTElements.U238)
                .iconSet(MaterialIconSet.RADIOACTIVE).buildAndRegister();

        CRITICAL_STEAM = new Material.Builder(PhoenixCore.id("critical_steam"))
                .gas().color(0xF0F8FF).secondaryColor(0xADD8E6).iconSet(MaterialIconSet.DULL).buildAndRegister();

        HOT_SODIUM_POTASSIUM = new Material.Builder(PhoenixCore.id("hot_sodium_potassium"))
                .liquid(3800).color(0xFF4500).secondaryColor(0xFFD700).iconSet(MaterialIconSet.DULL).buildAndRegister();

        BORON_CARBIDE = new Material.Builder(PhoenixCore.id("boron_carbide"))
                .ingot().color(0x353630).formula("B4C").iconSet(MaterialIconSet.DULL)
                .blastTemp(3600, BlastProperty.GasTier.LOW, 500, 1500)
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_DENSE).buildAndRegister();

        ZIRCALLOY = new Material.Builder(PhoenixCore.id("zircalloy"))
                .ingot().color(0x002327).secondaryColor(0x000000).iconSet(MaterialIconSet.DULL)
                .flags(GENERATE_PLATE, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_ROD, GENERATE_DENSE, GENERATE_FOIL,
                        GENERATE_SPRING, GENERATE_FRAME)
                .buildAndRegister();
    }
}
