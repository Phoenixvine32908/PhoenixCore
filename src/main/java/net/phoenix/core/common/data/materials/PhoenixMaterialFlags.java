package net.phoenix.core.common.data.materials;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_DENSE;

public class PhoenixMaterialFlags {

    public static final MaterialFlag GENERATE_NANITES = new MaterialFlag.Builder("generate_nanites")
            .requireFlags(GENERATE_DENSE)
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_CRYSTAL_ROSE = new MaterialFlag.Builder("generate_crystal_rose")
            .requireProps(PropertyKey.DUST)
            .build();

    public static final TagPrefix nanites = new TagPrefix("nanites")
            .idPattern("%s_nanites")
            .defaultTagPath("nanites/%s")
            .unformattedTagPath("nanites")
            .langValue("%s Nanites")
            .materialAmount(GTValues.M / 4)
            .unificationEnabled(true)
            .generateItem(true)
            .materialIconType(PhoenixMaterialSet.nanites)
            .generationCondition(mat -> mat.hasFlag(PhoenixMaterialFlags.GENERATE_NANITES));

    public static final TagPrefix crystal_rose = new TagPrefix("crystal_rose")
            .idPattern("%s_crystal_rose")
            .defaultTagPath("crystal_roses/%s")
            .unformattedTagPath("crystal_rose")
            .langValue("%s Crystal Rose")
            .materialAmount(GTValues.M / 4)
            .unificationEnabled(true)
            .generateItem(true)
            .materialIconType(PhoenixMaterialSet.crystal_rose)
            .generationCondition(mat -> mat.hasFlag(PhoenixMaterialFlags.GENERATE_CRYSTAL_ROSE));

    public static void init() {}
}
