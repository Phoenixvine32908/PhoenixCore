package net.phoenix.core.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;

public class PhoenixMaterialSet {

    public static final MaterialIconType nanites = new MaterialIconType("nanites");
    public static final MaterialIconType crystal_rose = new MaterialIconType("crystal_rose");
    public static final MaterialIconSet ALMOST_PURE_NEVONIAN_STEEL = new MaterialIconSet("almost_pure_nevonian_steel",
            SHINY);

    public static void init() {}
}
