package io.github.racoondog.recipedl.commands;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.racoondog.recipedl.DataDL;
import io.github.racoondog.recipedl.util.FileUtils;
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
    private static Path cachedPath;
    private static long timeout = 0;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var builder = literal("recipe");

        builder.then(literal("confirm").executes(RecipeDLCommand::confirm));

        builder.then(literal("run")
                .executes(RecipeDLCommand::run)
                .then(argument("folder", StringArgumentType.word()).executes(RecipeDLCommand::runCustomFolder))
        );

        dispatcher.register(literal("data-dl").then(builder));
    }

    private static int confirm(CommandContext<FabricClientCommandSource> ctx) {
        if (timeout + 60000 >= System.currentTimeMillis()) {
            timeout = 0;
            run(ctx, cachedPath, true);
        } else ctx.getSource().sendFeedback(Text.literal("No confirmation prompts in the last minute."));
        return 1;
    }

    private static int run(CommandContext<FabricClientCommandSource> ctx) {
        run(ctx, DataDL.ROOT_FOLDER.resolve(DataDL.getWorldName()), false);
        return 1;
    }

    private static int runCustomFolder(CommandContext<FabricClientCommandSource> ctx) {
        run(ctx, DataDL.ROOT_FOLDER.resolve(StringArgumentType.getString(ctx, "folder")), false);
        return 1;
    }

    private static void run(CommandContext<FabricClientCommandSource> ctx, Path dataFolder, boolean force) {
        long timer = System.currentTimeMillis();

        try {
            if (FileUtils.findSubfolder(dataFolder, "recipes", 2)) {
                if (force) {
                    FileUtils.deleteSubfolders(dataFolder, "recipes", 2);
                } else {
                    ctx.getSource().sendFeedback(Text.literal("Directory already exists. Run '/data-dl recipe confirm' to confirm run."));
                    timeout = System.currentTimeMillis();
                    cachedPath = dataFolder;
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommandException(Text.literal("Could not delete directory."));
        }

        ctx.getSource().sendFeedback(Text.literal("Old data deletion took %s ms.".formatted(System.currentTimeMillis() - timer)));
        timer = System.currentTimeMillis();

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

        ctx.getSource().sendFeedback(Text.literal("New data write took %s ms.".formatted(System.currentTimeMillis() - timer)));

        //ctx.getSource().sendFeedback(Text.literal("Recipe download complete in %s ms.".formatted(System.currentTimeMillis() - timer)));
    }

    private static Path getRecipePath(Path root, Identifier id) {
        return root.resolve(id.getNamespace()).resolve("recipes").resolve(id.getPath() + ".json");
    }
}
