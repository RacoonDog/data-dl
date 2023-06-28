package io.github.racoondog.datadl.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {
    public static boolean findSubfolder(Path rootFolder, String subfolderName, int maxDepth) throws IOException {
        if (!Files.isDirectory(rootFolder)) return false;

        try (var pathStream = Files.walk(rootFolder, maxDepth)) {
            return pathStream.anyMatch(path -> path.getFileName().toString().equals(subfolderName));
        }
    }

    public static void deleteSubfolders(Path rootFolder, String subfolderName, int maxDepth) throws IOException {
        Files.walkFileTree(rootFolder, new FileVisitor<>() {
            private int delete = 0;
            private int depth = 0;

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (dir == rootFolder) return FileVisitResult.CONTINUE;

                depth++;
                if (dir.getFileName().toString().equals(subfolderName)) delete++;
                if (delete == 0 && depth >= maxDepth) return FileVisitResult.SKIP_SUBTREE;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (delete > 0) Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (dir == rootFolder) return FileVisitResult.CONTINUE;

                if (dir.getFileName().toString().equals(subfolderName)) {
                    Files.delete(dir);
                    delete--;
                }
                depth--;
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
