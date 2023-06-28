package io.github.racoondog.datadl.commands;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.racoondog.datadl.DataDL;
import io.github.racoondog.datadl.util.FileUtils;
import io.github.racoondog.datadl.util.JsonHelper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandException;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class AdvancementDLCommand {
    private static long timeout = 0;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var builder = literal("advancement");

        builder.then(literal("run")
                .executes(AdvancementDLCommand::run)
                .then(argument("folder", StringArgumentType.word()).executes(AdvancementDLCommand::runCustomFolder))
        );

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
            if (FileUtils.findSubfolder(dataFolder, "advancements", 2)) {
                if (timeout + 60000 >= System.currentTimeMillis()) {
                    FileUtils.deleteSubfolders(dataFolder, "advancements", 2);
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

        for (var advancement : MinecraftClient.getInstance().player.networkHandler.getAdvancementHandler().getManager().getAdvancements()) {
            Path targetFile = getAdvancementFile(dataFolder, advancement.getId());

            try {
                JsonObject object = JsonHelper.serializeAdvancement(advancement);

                Files.createDirectories(targetFile.getParent());
                Files.createFile(targetFile);
                Files.writeString(targetFile, DataDL.GSON.toJson(object));
            } catch (IOException e) {
                DataDL.LOGGER.warn("Error writing advancement '{}' to file '{}'.", advancement.getId(), targetFile);
            } catch (Exception e) {
                DataDL.LOGGER.warn("Error serializing advancement '{}'.", advancement.getId());
                e.printStackTrace();
            }
        }

        ctx.getSource().sendFeedback(Text.literal("Advancement download complete in %s ms.".formatted(System.currentTimeMillis() - timer)));
    }

    private static Path getAdvancementFile(Path root, Identifier id) {
        Path advancementFolder = root.resolve(id.getNamespace()).resolve("advancements");
        String[] subfolders = id.getPath().split("/");
        for (int i = 0; i < subfolders.length - 1; i++) {
            advancementFolder = advancementFolder.resolve(subfolders[i]);
        }
        return advancementFolder.resolve(subfolders[subfolders.length - 1] + ".json");
    }
}
