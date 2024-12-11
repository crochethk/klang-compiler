package utils;

import java.nio.file.Path;

public class PathUtils {
    /**
     * Returns the parent path, or {@code Path.of("")} if the given path does
     * not have a parent.
     */
    public static Path getParentOrEmpty(Path path) {
        var parent = path.getParent();
        return parent != null ? parent : Path.of("");
    }

    /**
     * Returns the filename without the extension. In case of no extension
     * the farthest element from the root in the directory hierarchy is returned.
     */
    public static String getFileNameNoExt(Path filePath) {
        var fileName = filePath.getFileName().toString();
        var extIdx = fileName.lastIndexOf('.');
        // remove file extension
        return fileName.substring(0, extIdx > 0 ? extIdx : fileName.length());
    }
}
