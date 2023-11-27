package gregtech.api.terminal.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileTree extends TreeNode<File, File> {

    public FileTree(File dir) {
        this(0, dir);
    }

    private FileTree(int dimension, File key) {
        super(dimension, key);
    }

    @Override
    public boolean isLeaf() {
        return getKey().isFile();
    }

    @Override
    public File getContent() {
        return isLeaf() ? getKey() : null;
    }

    @Override
    public List<TreeNode<File, File>> getChildren() {
        if (children == null && !isLeaf()) {
            children = new ArrayList<>();
            File[] listFiles = key.listFiles();
            if (listFiles != null) {
                Arrays.stream(listFiles).sorted((a, b) -> {
                    if (a.isFile() && b.isFile()) {
                        return a.compareTo(b);
                    } else if (a.isDirectory() && b.isDirectory()) {
                        return a.compareTo(b);
                    } else if (a.isDirectory()) {
                        return -1;
                    }
                    return 1;
                }).forEach(file -> children.add(new FileTree(dimension + 1, file)));
            }
        }
        return super.getChildren();
    }

    @Override
    public String toString() {
        return getKey().getName();
    }
}
