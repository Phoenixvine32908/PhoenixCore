//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data.recipe;

import com.google.gson.JsonObject;
import fi.dea.mc.deafission.FissionMod;
import fi.dea.mc.deafission.core.components.EfficiencyComponent;
import fi.dea.mc.deafission.core.components.HeatComponent;
import fi.dea.mc.deafission.core.components.IReactorComponent;
import fi.dea.mc.deafission.core.components.ThrottleComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ComponentRecipe(ResourceLocation id, IReactorComponent component) implements Recipe<SimpleContainer> {
    public boolean matches(SimpleContainer inv, Level level) {
        return false;
    }

    public ItemStack assemble(SimpleContainer inv, RegistryAccess r) {
        return ItemStack.f_41583_;
    }

    public boolean m_8004_(int width, int height) {
        return false;
    }

    public ItemStack m_8043_(RegistryAccess r) {
        return ItemStack.f_41583_;
    }

    public ResourceLocation m_6423_() {
        return this.id;
    }

    public RecipeSerializer<?> m_7707_() {
        return ComponentRecipe.Serializer.INSTANCE;
    }

    public RecipeType<?> m_6671_() {
        return ComponentRecipe.Type.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<ComponentRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public ComponentRecipe fromJson(ResourceLocation id, JsonObject json) {
            Block block = (Block)Objects.requireNonNull((Block)ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(GsonHelper.m_13906_(json, "block"))));
            IReactorComponent.Type type = fi.dea.mc.deafission.core.components.IReactorComponent.Type.valueOf(GsonHelper.m_13906_(json, "componentType"));
            JsonObject dataJson = GsonHelper.m_13930_(json, "data");
            return new ComponentRecipe(id, this.makeComponent(block, type, dataJson));
        }

        public ComponentRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Block block = (Block)buf.readRegistryIdUnsafe(ForgeRegistries.BLOCKS);
            IReactorComponent.Type type = fi.dea.mc.deafission.core.components.IReactorComponent.Type.valueOf(buf.m_130136_(30));
            return new ComponentRecipe(id, this.makeComponent(block, type, buf));
        }

        public void toNetwork(FriendlyByteBuf buf, ComponentRecipe recipe) {
            buf.writeRegistryIdUnsafe(ForgeRegistries.BLOCKS, recipe.component.block());
            buf.m_130072_(recipe.component.getComponentType().name(), 30);
            this.writeComponentData(recipe.component, buf);
        }

        private IReactorComponent makeComponent(Block block, IReactorComponent.Type type, JsonObject data) {
            double value = data.getAsJsonPrimitive("value").getAsDouble();
            Object var10000;
            switch (type) {
                case HEAT -> var10000 = new HeatComponent(block, value);
                case EFFICIENCY -> var10000 = new EfficiencyComponent(block, value);
                case THROTTLE -> var10000 = new ThrottleComponent(block, value);
                default -> throw new IncompatibleClassChangeError();
            }

            return (IReactorComponent)var10000;
        }

        private IReactorComponent makeComponent(Block block, IReactorComponent.Type type, FriendlyByteBuf buf) {
            double value = buf.readDouble();
            Object var10000;
            switch (type) {
                case HEAT -> var10000 = new HeatComponent(block, value);
                case EFFICIENCY -> var10000 = new EfficiencyComponent(block, value);
                case THROTTLE -> var10000 = new ThrottleComponent(block, value);
                default -> throw new IncompatibleClassChangeError();
            }

            return (IReactorComponent)var10000;
        }

        private void writeComponentData(IReactorComponent component, FriendlyByteBuf buf) {
            switch (component.getComponentType()) {
                case HEAT -> buf.writeDouble(((HeatComponent)component).value());
                case EFFICIENCY -> buf.writeDouble(((EfficiencyComponent)component).value());
                case THROTTLE -> buf.writeDouble(((ThrottleComponent)component).value());
            }

        }
    }

    public static class Type implements RecipeType<ComponentRecipe> {
        public static final ResourceLocation ID = FissionMod.id("fission_component");
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        public String toString() {
            return ID.toString();
        }
    }

    public static class Cache {
        private static Map<Block, List<IReactorComponent>> _mapping;

        public static List<IReactorComponent> getComponents(Block block) {
            return Collections.unmodifiableList((List)_mapping.getOrDefault(block, List.of()));
        }

        static void clear() {
            _mapping = null;
        }

        public static void ensureLoaded(Level level) {
            if (_mapping == null) {
                load(level.m_7465_());
            }

        }

        private static void load(RecipeManager rm) {
            Map<Block, List<IReactorComponent>> mapping = new HashMap();

            for(ComponentRecipe r : rm.m_44013_(ComponentRecipe.Type.INSTANCE)) {
                IReactorComponent c = r.component();
                ((List)mapping.computeIfAbsent(c.block(), (k) -> new ArrayList())).add(c);
            }

            _mapping = mapping;
        }
    }
}
