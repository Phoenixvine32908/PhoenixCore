//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import fi.dea.mc.deafission.core.components.EfficiencyComponent;
import fi.dea.mc.deafission.core.components.HeatComponent;
import fi.dea.mc.deafission.core.components.IReactorComponent;
import fi.dea.mc.deafission.core.components.ThrottleComponent;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class FissionEmiComponentRecipe implements EmiRecipe {
    private final ResourceLocation _id;
    private final Block _block;
    private final IReactorComponent.Type _type;
    private final String _langKeyPrefix;
    private final double _value;

    private FissionEmiComponentRecipe(ResourceLocation id, IReactorComponent c, String langKey, double value) {
        this._id = id;
        this._block = c.block();
        this._type = c.getComponentType();
        this._langKeyPrefix = langKey;
        this._value = value;
    }

    public FissionEmiComponentRecipe(ResourceLocation id, HeatComponent c) {
        this(id, c, "emi.deafission.heat_component", c.value());
    }

    public FissionEmiComponentRecipe(ResourceLocation id, EfficiencyComponent c) {
        this(id, c, "emi.deafission.efficiency_component", c.value());
    }

    public FissionEmiComponentRecipe(ResourceLocation id, ThrottleComponent c) {
        this(id, c, "emi.deafission.throttle_component", c.value());
    }

    public ResourceLocation getId() {
        return this._id;
    }

    public EmiRecipeCategory getCategory() {
        return FissionEmiPlugin.FISSION_COMPONENT;
    }

    public int getDisplayWidth() {
        return 140;
    }

    public int getDisplayHeight() {
        return 30;
    }

    public void addWidgets(WidgetHolder widgetHolder) {
        int itemOffsetY = 2;
        int itemOffsetX = 4;
        this.createItemWidget(widgetHolder, itemOffsetY, itemOffsetX, EmiStack.of(this._block.m_5456_()));
        this.createCompStatsWidget(widgetHolder, itemOffsetY + 5, itemOffsetX + 26 + 4);
    }

    private void createItemWidget(WidgetHolder holder, int offsetY, int offsetX, EmiIngredient stack) {
        SlotWidget widget = new SlotWidget(stack, offsetX, offsetY);
        widget.large(true);
        holder.add(widget);
        widget.getBounds();
        widget.appendTooltip(Component.m_237113_("id: " + this._id));
    }

    private void createCompStatsWidget(WidgetHolder holder, int offsetY, int offsetX) {
        Font font = Minecraft.m_91087_().f_91062_;
        Objects.requireNonNull(font);
        int lineHeight = 9;
        holder.addText(Component.m_237115_(this._langKeyPrefix + "_key"), offsetX, offsetY, 16777215, true);
        offsetY += lineHeight;
        holder.addText(Component.m_237110_(this._langKeyPrefix + "_value", new Object[]{this._value}), offsetX, offsetY, 16777215, true);
    }

    public List<EmiIngredient> getInputs() {
        return List.of(EmiStack.of(this._block.m_5456_()));
    }

    public List<EmiStack> getOutputs() {
        return List.of();
    }
}
