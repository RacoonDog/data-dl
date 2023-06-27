package io.github.racoondog.recipedl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public class RecipeDL implements ClientModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Path ROOT_FOLDER = FabricLoader.getInstance().getGameDir().resolve("recipe-dl");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            RDLCommand.register(dispatcher);
        }));
    }
}
