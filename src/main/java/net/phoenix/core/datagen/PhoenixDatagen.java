package net.phoenix.core.datagen;

import net.phoenix.core.datagen.lang.PhoenixLangHandler;

import com.tterrag.registrate.providers.ProviderType;
import net.phoenix.core.datagen.lang.PhoenixMachineLangHandler;
import net.phoenix.core.datagen.lang.PhoenixMaterialLangHandler;

import static net.phoenix.core.common.registry.PhoenixRegistration.REGISTRATE;

public class PhoenixDatagen {

    public static void init() {
        REGISTRATE.addDataGenerator(ProviderType.LANG, PhoenixLangHandler::init);
        REGISTRATE.addDataGenerator(ProviderType.LANG, PhoenixMachineLangHandler::init);
        REGISTRATE.addDataGenerator(ProviderType.LANG, PhoenixMaterialLangHandler::init);
    }
}
