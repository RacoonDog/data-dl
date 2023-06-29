package io.github.racoondog.datadl.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.racoondog.datadl.DataDL;
import io.github.racoondog.datadl.util.FileUtils;
import io.github.racoondog.datadl.util.JsonHelper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandException;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class TagDLCommand {
    private static long timeout = 0;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var builder = literal("tag");

        builder.executes(TagDLCommand::run)
                .then(argument("folder", StringArgumentType.word()).executes(TagDLCommand::runCustomFolder));

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
            if (FileUtils.findSubfolder(dataFolder, "tags", 2)) {
                if (timeout + 60000 >= System.currentTimeMillis()) {
                    FileUtils.deleteSubfolders(dataFolder, "tags", 2);
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

        ctx.getSource().sendFeedback(Text.literal("Tag download complete in %s ms.".formatted(System.currentTimeMillis() - timer)));
    }

    public static void download(Path dataFolder) {
        download(dataFolder, Registries.BANNER_PATTERN, "banner_pattern");
        download(dataFolder, Registries.BLOCK, "blocks");
        download(dataFolder, Registries.CAT_VARIANT, "cat_variant");
        download(dataFolder, Registries.ENTITY_TYPE, "entity_types");
        download(dataFolder, Registries.FLUID, "fluids");
        download(dataFolder, Registries.GAME_EVENT, "game_events");
        download(dataFolder, Registries.INSTRUMENT, "instrument");
        download(dataFolder, Registries.ITEM, "items");
        download(dataFolder, Registries.PAINTING_VARIANT, "painting_variant");
        download(dataFolder, Registries.POINT_OF_INTEREST_TYPE, "point_of_interest_type");
    }

    private static <T> void download(Path tagFolder, Registry<T> registry, String subfolder) {
        registry.streamTags().forEach(tagKey -> {
            Path targetFile = getTagFile(tagFolder, tagKey.id(), subfolder);
            JsonObject object = new JsonObject();

            JsonArray values = JsonHelper.addArray(object, "values");
            for (var entry : registry.iterateEntries(tagKey)) values.add(registry.getId(entry.value()).toString());

            try {
                Files.createDirectories(targetFile.getParent());
                Files.createFile(targetFile);
                Files.writeString(targetFile, DataDL.GSON.toJson(object));
            } catch (IOException e) {
                DataDL.LOGGER.warn("Error writing advancement '{}' to file '{}'.", tagKey.id(), targetFile);
            } catch (Exception e) {
                DataDL.LOGGER.warn("Error serializing advancement '{}'.", tagKey.id());
                e.printStackTrace();
            }
        });
    }

    private static Path getTagFile(Path root, Identifier id, String subfolder) {
        return root.resolve(id.getNamespace()).resolve("tags").resolve(subfolder).resolve(id.getPath() + ".json");
    }
}
