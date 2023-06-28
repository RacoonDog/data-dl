package io.github.racoondog.recipedl.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileUtils {
    public static boolean findSubfolder(Path rootFolder, String subfolderName, int maxDepth) throws IOException {
        if (!Files.isDirectory(rootFolder)) return false;

        try (var pathStream = Files.walk(rootFolder, maxDepth)) {
            return pathStream.anyMatch(path -> path.getFileName().toString().equals(subfolderName));
        }
    }

    public static void deleteSubfolders(Path rootFolder, String subfolderName, int maxDepth) throws IOException {
        try (var pathStream = Files.walk(rootFolder, maxDepth)) {
            List<Path> subfolders = pathStream.filter(path -> path.getFileName().toString().equals(subfolderName)).toList();
            for (var subfolder : subfolders) org.apache.commons.io.FileUtils.deleteDirectory(subfolder.toFile());
        }
    }
}
