package net.phoenix.core.mixin;


import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import net.phoenix.core.api.item.tool.PhoenixToolType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin class to inject our custom tool types into the GregTech registry.
 * By using a Mixin, we avoid the registry issues associated with directly extending the GTToolType class.
 */
@Mixin(GTToolType.class)
public abstract class GTToolTypeMixin {

    /**
     * Injects the registration of our custom tool type into the GregTech registry.
     * The @Inject annotation targets the static initializer of the GTToolType class.
     * This ensures that our tool type is built and registered at the same time as
     * the other base GregTech tool types.
     *
     * <p>This method simply registers the tool type defined in the public {@link PhoenixToolType} class.
     *
     * @param ci CallbackInfo object for the injection point.
     */
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onToolTypeRegister(CallbackInfo ci) {
        // We only need to reference the field here to trigger its static initialization and registration.
        // The build method in GTToolType.Builder handles the internal registration when called.
        // It's already been built and registered in the static block of PhoenixToolTypes.
        GTToolType ignored = PhoenixToolType.DRILL_LuV;
    }
}