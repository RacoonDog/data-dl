package io.github.racoondog.datadl.commands;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.racoondog.datadl.DataDL;
import io.github.racoondog.datadl.util.FileUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandException;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class RecipeDLCommand {
    private static long timeout = 0;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var builder = literal("recipe");

        builder.executes(RecipeDLCommand::run)
                .then(argument("folder", StringArgumentType.word()).executes(RecipeDLCommand::runCustomFolder));

        dispatcher.register(literal("data-dl").then(builder));
    }

    private static int run(CommandContext<FabricClientCommandSource> ctx) {
        run(ctx, DataDL.ROOT_FOLDER.resolve(DataDL.getWorldName()));
        return 1;
    }

    private static int runCustomFolder(CommandContext<FabricClientCommandSource> ctx) {
        run(ctx, DataDL.ROOT_FOLDER.resolve(StringArgumentType.getString(ctx, "folder")));
        return 1;
    }

    private static void run(CommandContext<FabricClientCommandSource> ctx, Path dataFolder) {
        long timer = System.currentTimeMillis();

        try {
            if (FileUtils.findSubfolder(dataFolder, "recipes", 2)) {
                if (timeout + 60000 >= System.currentTimeMillis()) {
                    FileUtils.deleteSubfolders(dataFolder, "recipes", 2);
                    timeout = 0;
                } else {
                    ctx.getSource().sendFeedback(Text.literal("Directory already exists. Run to command again to delete the directory and run."));
                    timeout = System.currentTimeMillis();
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommandException(Text.literal("Could not delete directory."));
        }

        download(dataFolder);

        ctx.getSource().sendFeedback(Text.literal("Recipe download complete in %s ms.".formatted(System.currentTimeMillis() - timer)));
    }

    public static void download(Path dataFolder) {
        MinecraftClient.getInstance().player.getRecipeBook().getOrderedResults().stream().parallel().flatMap(collection -> collection.getAllRecipes().stream()).forEach(recipe -> {
            Path targetFile = getRecipePath(dataFolder, recipe.getId());

            try {
                JsonObject object = recipe.serializeToJson();

                Files.createDirectories(targetFile.getParent());
                Files.createFile(targetFile);
                Files.writeString(targetFile, DataDL.GSON.toJson(object));
            } catch (IOException e) {
                DataDL.LOGGER.warn("Error writing recipe '{}' to file '{}'.", recipe.getId(), targetFile);
            } catch (Exception e) {
                DataDL.LOGGER.warn("Error serializing recipe '{}'.", recipe.getId());
                e.printStackTrace();
            }
        });
    }

    private static Path getRecipePath(Path root, Identifier id) {
        return root.resolve(id.getNamespace()).resolve("recipes").resolve(id.getPath() + ".json");
    }
}
