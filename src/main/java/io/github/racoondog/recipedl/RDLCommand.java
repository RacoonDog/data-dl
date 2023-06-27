package io.github.racoondog.recipedl;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandException;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

@Environment(EnvType.CLIENT)
public class RDLCommand {
    private static Path cachedPath;
    private static long timeout = 0;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("recipe-dl").then(literal("confirm").executes(RDLCommand::confirm)));

        dispatcher.register(literal("recipe-dl").then(
                literal("run").executes(RDLCommand::run).then(
                        argument("folder", StringArgumentType.word()).executes(RDLCommand::runCustomFolder))
        ));
    }

    private static int confirm(CommandContext<FabricClientCommandSource> ctx) {
        if (timeout + 60000 >= System.currentTimeMillis()) {
            timeout = 0;
            run(ctx, cachedPath, true);
        } else ctx.getSource().sendFeedback(Text.literal("No confirmation prompts in the last minute."));
        return 1;
    }

    private static int run(CommandContext<FabricClientCommandSource> ctx) {
        String folderName = MinecraftClient.getInstance().isIntegratedServerRunning() ?
                MinecraftClient.getInstance().getServer().getSaveProperties().getLevelName() :
                MinecraftClient.getInstance().getCurrentServerEntry().address;

        run(ctx, RecipeDL.ROOT_FOLDER.resolve(folderName), false);
        return 1;
    }

    private static int runCustomFolder(CommandContext<FabricClientCommandSource> ctx) {
        run(ctx, RecipeDL.ROOT_FOLDER.resolve(StringArgumentType.getString(ctx, "folder")), false);
        return 1;
    }

    private static void run(CommandContext<FabricClientCommandSource> ctx, Path dataFolder, boolean force) {
        if (Files.isDirectory(dataFolder)) {
            if (force) {
                try {
                    FileUtils.deleteDirectory(dataFolder.toFile());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new CommandException(Text.literal("Could not delete directory."));
                }
            } else {
                ctx.getSource().sendFeedback(Text.literal("Directory already exists. Run '/recipe-dl confirm' to confirm run."));
                timeout = System.currentTimeMillis();
                cachedPath = dataFolder;
                return;
            }
        }

        MinecraftClient.getInstance().player.getRecipeBook().getOrderedResults().stream().parallel().flatMap(collection -> collection.getAllRecipes().stream()).forEach(recipe -> {
            Path targetFile = getRecipeFile(dataFolder, recipe.getId());

            try {
                JsonObject object = recipe.serializeToJson();

                Files.createDirectories(targetFile.getParent());
                Files.createFile(targetFile);
                Files.writeString(targetFile, RecipeDL.GSON.toJson(object));
            } catch (IOException e) {
                RecipeDL.LOGGER.warn("Error writing recipe '{}' to file '{}'.", recipe.getId(), targetFile);
            } catch (Exception e) {
                RecipeDL.LOGGER.warn("Error serializing recipe '{}'.", recipe.getId());
                e.printStackTrace();
            }
        });

        ctx.getSource().sendFeedback(Text.literal("Download complete."));
    }

    private static Path getRecipeFile(Path root, Identifier id) {
        return root.resolve(id.getNamespace()).resolve("recipes").resolve(id.getPath() + ".json");
    }
}
