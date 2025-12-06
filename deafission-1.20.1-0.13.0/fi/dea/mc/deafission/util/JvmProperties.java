//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.util;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public class JvmProperties {
    public static final Environment Env = getEnv();
    @Nullable
    public static final String AutoLoadLevel = System.getProperty("fi.dea.mc.autoload");

    private JvmProperties() {
    }

    private static Environment getEnv() {
        String prop = System.getProperty("fi.dea.mc.environment");
        if (prop == null) {
            return JvmProperties.Environment.Default;
        } else {
            Environment var10000;
            switch (prop) {
                case "DEBUG" -> var10000 = JvmProperties.Environment.Debug;
                default -> var10000 = JvmProperties.Environment.Unknown;
            }

            return var10000;
        }
    }

    public static enum Environment {
        Unknown,
        Default,
        Debug;
    }
}
