//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data;

import com.tterrag.registrate.providers.RegistrateItemTagsProvider;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import fi.dea.mc.deafission.FissionMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class FissionTags {
    public static final TagKey<Item> FUELS;
    public static final TagKey<Block> COMPONENT;

    private FissionTags() {
    }

    public static void init() {
        FissionItems.init();
    }

    public static void initItem(RegistrateItemTagsProvider tags) {
        tags.addTag(FUELS);
    }

    public static void initBlock(RegistrateTagsProvider tags) {
        tags.addTag(COMPONENT);
    }

    static {
        FUELS = TagKey.m_203882_(Registries.f_256913_, FissionMod.id("fuels"));
        COMPONENT = TagKey.m_203882_(Registries.f_256747_, FissionMod.id("components"));
    }
}
