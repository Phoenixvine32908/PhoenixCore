package net.phoenix.core.common.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class PTags {
    public static final TagKey<Item> FLOWERS =
            TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "flowers"));
    public static final TagKey<Item> CROPS =
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "crops"));
    public static final TagKey<Item> MUSHROOMS =
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "mushrooms"));
    public static final TagKey<Item> LOGS =
            TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "logs"));
    public static final TagKey<Item> PLANKS =
            TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "planks"));
}
