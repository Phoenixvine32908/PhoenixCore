//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data.recipe;

import fi.dea.mc.deafission.common.data.recipe.ComponentRecipe.Type;
import fi.dea.mc.deafission.core.components.IReactorComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class ComponentRecipe$Cache {
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

        for(ComponentRecipe r : rm.m_44013_(Type.INSTANCE)) {
            IReactorComponent c = r.component();
            ((List)mapping.computeIfAbsent(c.block(), (k) -> new ArrayList())).add(c);
        }

        _mapping = mapping;
    }
}
