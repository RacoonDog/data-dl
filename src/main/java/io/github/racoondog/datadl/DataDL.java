package io.github.racoondog.datadl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import io.github.racoondog.datadl.commands.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;

import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public class DataDL implements ClientModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Path ROOT_FOLDER = FabricLoader.getInstance().getGameDir().resolve("data-dl");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            AllDLCommand.register(dispatcher);
            AdvancementDLCommand.register(dispatcher);
            RecipeDLCommand.register(dispatcher);
            TagDLCommand.register(dispatcher);
        }));
    }

    public static String getWorldName() {
        return MinecraftClient.getInstance().isIntegratedServerRunning() ?
                MinecraftClient.getInstance().getServer().getSaveProperties().getLevelName() :
                MinecraftClient.getInstance().getCurrentServerEntry().address;
    }
}
