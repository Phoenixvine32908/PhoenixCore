//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;
import fi.dea.mc.deafission.FissionMod;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class FissionReactorMachine$ReactorIo {
    private final List<IRecipeHandler<FluidIngredient>> _fluidIn;
    private final List<IRecipeHandler<FluidIngredient>> _fluidOut;
    private final List<IRecipeHandler<Ingredient>> _itemIn;
    private final List<IRecipeHandler<Ingredient>> _itemOut;
    private final List<ReactorFuelHolder> _fuels;
    private final List<ReactorMaterialHolder> _materials;
    private final List<ReactorRedstonePort> _redstone;

    public FissionReactorMachine$ReactorIo(FissionReactorMachine this$0) {
        this.this$0 = this$0;
        this._fluidIn = new ArrayList();
        this._fluidOut = new ArrayList();
        this._itemIn = new ArrayList();
        this._itemOut = new ArrayList();
        this._fuels = new ArrayList();
        this._materials = new ArrayList();
        this._redstone = new ArrayList();
    }

    public void clear() {
        this._fluidIn.clear();
        this._fluidOut.clear();
        this._itemIn.clear();
        this._itemOut.clear();
        this._fuels.clear();
        this._materials.clear();
        this._redstone.clear();
    }

    public void refresh() {
        this.clear();

        for(IMultiPart part : this.this$0.getParts()) {
            for(RecipeHandlerList handler : part.getRecipeHandlers()) {
                this.populate(this._fluidIn, GTRecipeCapabilities.FLUID, IO.IN, handler);
                this.populate(this._fluidOut, GTRecipeCapabilities.FLUID, IO.OUT, handler);
                this.populate(this._itemIn, GTRecipeCapabilities.ITEM, IO.IN, handler);
                this.populate(this._itemOut, GTRecipeCapabilities.ITEM, IO.OUT, handler);
            }

            if (part instanceof ReactorFuelHolder fh) {
                this._fuels.add(fh);
            }

            if (part instanceof ReactorMaterialHolder mh) {
                this._materials.add(mh);
            }

            if (part instanceof ReactorRedstonePort rp) {
                this._redstone.add(rp);
            }
        }

    }

    private <T> void populate(List<IRecipeHandler<T>> list, RecipeCapability<T> cap, IO io, RecipeHandlerList handler) {
        List<IRecipeHandler<?>> caps = handler.getCapability(cap);
        if (handler.getHandlerIO().support(io) && !caps.isEmpty()) {
            list.add((IRecipeHandler)caps.get(0));
        }

    }

    public int countInputFluid(Fluid fluid, int upTo) {
        List<FluidIngredient> drainRecipe = List.of(FluidIngredient.of(fluid, upTo));

        for(IRecipeHandler<FluidIngredient> handler : this._fluidIn) {
            drainRecipe = handler.handleRecipe(IO.IN, (GTRecipe)null, drainRecipe, true);
            if (drainRecipe == null) {
                return upTo;
            }
        }

        return upTo - ((FluidIngredient)drainRecipe.get(0)).getAmount();
    }

    public int drainInputFluid(Fluid fluid, int amount) {
        if (amount == 0) {
            return 0;
        } else {
            List<FluidIngredient> drainRecipe = List.of(FluidIngredient.of(fluid, amount));

            for(IRecipeHandler<FluidIngredient> handler : this._fluidIn) {
                drainRecipe = handler.handleRecipe(IO.IN, (GTRecipe)null, drainRecipe, false);
                if (drainRecipe == null) {
                    return amount;
                }
            }

            return amount - ((FluidIngredient)drainRecipe.get(0)).getAmount();
        }
    }

    public int pushOutputFluid(Fluid fluid, int amount) {
        if (amount == 0) {
            return 0;
        } else {
            List<FluidIngredient> fillRecipe = List.of(FluidIngredient.of(fluid, amount));

            for(IRecipeHandler<FluidIngredient> handler : this._fluidOut) {
                fillRecipe = handler.handleRecipe(IO.OUT, (GTRecipe)null, fillRecipe, false);
                if (fillRecipe == null) {
                    return amount;
                }
            }

            return amount - ((FluidIngredient)fillRecipe.get(0)).getAmount();
        }
    }

    public int pushOutputItem(ItemStack item) {
        ItemStack work = new ItemStack(item.m_41720_(), item.m_41613_());

        for(IRecipeHandler<Ingredient> handler : this._itemOut) {
            if (handler instanceof NotifiableItemStackHandler nist) {
                for(int slot = 0; slot < nist.getSlots(); ++slot) {
                    work = nist.insertItemInternal(slot, work, false);
                    if (work.m_41619_()) {
                        return item.m_41613_();
                    }
                }
            } else {
                FissionMod.LOG.warn("NotImplemented: output bus of type {}", handler.getClass().getName());
            }
        }

        return item.m_41613_() - work.m_41613_();
    }

    public @Nullable ItemStack drainSingleInputItem(Function<ItemStack, Boolean> predicate) {
        for(IRecipeHandler<Ingredient> handler : this._itemIn) {
            if (handler instanceof NotifiableItemStackHandler nist) {
                for(int slot = 0; slot < nist.getSlots(); ++slot) {
                    ItemStack slotItem = nist.getStackInSlot(slot);
                    if (!slotItem.m_41619_() && (Boolean)predicate.apply(slotItem)) {
                        ItemStack extractedItem = nist.extractItemInternal(slot, 1, false);
                        if (extractedItem != null) {
                            return extractedItem;
                        }
                    }
                }
            } else {
                FissionMod.LOG.warn("NotImplemented: input bus of type {}", handler.getClass().getName());
            }
        }

        return null;
    }

    public boolean haveSingleInputInput(Function<ItemStack, Boolean> predicate) {
        for(IRecipeHandler<Ingredient> handler : this._itemIn) {
            if (handler instanceof NotifiableItemStackHandler nist) {
                for(int slot = 0; slot < nist.getSlots(); ++slot) {
                    ItemStack slotItem = nist.getStackInSlot(slot);
                    if (!slotItem.m_41619_() && (Boolean)predicate.apply(slotItem)) {
                        ItemStack extractedItem = nist.extractItemInternal(slot, 1, true);
                        if (extractedItem != null) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
