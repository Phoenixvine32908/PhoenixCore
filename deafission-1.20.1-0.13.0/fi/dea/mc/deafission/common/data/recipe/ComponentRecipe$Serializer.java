//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data.recipe;

import com.google.gson.JsonObject;
import fi.dea.mc.deafission.common.data.recipe.ComponentRecipe.1;
import fi.dea.mc.deafission.core.components.EfficiencyComponent;
import fi.dea.mc.deafission.core.components.HeatComponent;
import fi.dea.mc.deafission.core.components.IReactorComponent;
import fi.dea.mc.deafission.core.components.ThrottleComponent;
import fi.dea.mc.deafission.core.components.IReactorComponent.Type;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class ComponentRecipe$Serializer implements RecipeSerializer<ComponentRecipe> {
    public static final ComponentRecipe$Serializer INSTANCE = new ComponentRecipe$Serializer();

    public ComponentRecipe fromJson(ResourceLocation id, JsonObject json) {
        Block block = (Block)Objects.requireNonNull((Block)ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(GsonHelper.m_13906_(json, "block"))));
        IReactorComponent.Type type = Type.valueOf(GsonHelper.m_13906_(json, "componentType"));
        JsonObject dataJson = GsonHelper.m_13930_(json, "data");
        return new ComponentRecipe(id, this.makeComponent(block, type, dataJson));
    }

    public ComponentRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        Block block = (Block)buf.readRegistryIdUnsafe(ForgeRegistries.BLOCKS);
        IReactorComponent.Type type = Type.valueOf(buf.m_130136_(30));
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
        switch (1.$SwitchMap$fi$dea$mc$deafission$core$components$IReactorComponent$Type[type.ordinal()]) {
            case 1 -> var10000 = new HeatComponent(block, value);
            case 2 -> var10000 = new EfficiencyComponent(block, value);
            case 3 -> var10000 = new ThrottleComponent(block, value);
            default -> throw new IncompatibleClassChangeError();
        }

        return (IReactorComponent)var10000;
    }

    private IReactorComponent makeComponent(Block block, IReactorComponent.Type type, FriendlyByteBuf buf) {
        double value = buf.readDouble();
        Object var10000;
        switch (1.$SwitchMap$fi$dea$mc$deafission$core$components$IReactorComponent$Type[type.ordinal()]) {
            case 1 -> var10000 = new HeatComponent(block, value);
            case 2 -> var10000 = new EfficiencyComponent(block, value);
            case 3 -> var10000 = new ThrottleComponent(block, value);
            default -> throw new IncompatibleClassChangeError();
        }

        return (IReactorComponent)var10000;
    }

    private void writeComponentData(IReactorComponent component, FriendlyByteBuf buf) {
        switch (1.$SwitchMap$fi$dea$mc$deafission$core$components$IReactorComponent$Type[component.getComponentType().ordinal()]) {
            case 1 -> buf.writeDouble(((HeatComponent)component).value());
            case 2 -> buf.writeDouble(((EfficiencyComponent)component).value());
            case 3 -> buf.writeDouble(((ThrottleComponent)component).value());
        }

    }
}
