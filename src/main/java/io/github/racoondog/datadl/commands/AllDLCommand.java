package io.github.racoondog.datadl.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.racoondog.datadl.DataDL;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandException;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class AllDLCommand {
    private static long timeout = 0;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var builder = literal("all");

        builder.executes(AllDLCommand::run)
                .then(argument("folder", StringArgumentType.word()).executes(AllDLCommand::runCustomFolder));

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
            if (Files.isDirectory(dataFolder)) {
                if (timeout + 60000 >= System.currentTimeMillis()) {
                    FileUtils.deleteDirectory(dataFolder.toFile());
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

        AdvancementDLCommand.download(dataFolder);
        RecipeDLCommand.download(dataFolder);
        TagDLCommand.download(dataFolder);

        ctx.getSource().sendFeedback(Text.literal("Full download complete in %s ms.".formatted(System.currentTimeMillis() - timer)));
    }
}
