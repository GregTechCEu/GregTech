package gregtech.api.terminal.util;

import java.util.ArrayList;
import java.util.List;

/***
 * Tree
 * 
 * @param <T> key
 * @param <K> leaf
 */
public class TreeNode<T, K> {

    public final int dimension;
    protected final T key;
    protected K content;
    protected List<TreeNode<T, K>> children;

    public TreeNode(int dimension, T key) {
        this.dimension = dimension;
        this.key = key;
    }

    public boolean isLeaf() {
        return getChildren() == null || getChildren().isEmpty();
    }

    public TreeNode<T, K> getOrCreateChild(T childKey) {
        TreeNode<T, K> result;
        if (getChildren() != null) {
            result = getChildren().stream().filter(child -> child.key.equals(childKey)).findFirst().orElseGet(() -> {
                TreeNode<T, K> newNode = new TreeNode<>(dimension + 1, childKey);
                getChildren().add(newNode);
                return newNode;
            });
        } else {
            children = new ArrayList<>();
            result = new TreeNode<>(dimension + 1, childKey);
            getChildren().add(result);
        }
        return result;
    }

    public TreeNode<T, K> getChild(T key) {
        if (getChildren() != null) {
            for (TreeNode<T, K> child : getChildren()) {
                if (child.key.equals(key)) {
                    return child;
                }
            }
        }
        return null;
    }

    public void addContent(T key, K content) {
        getOrCreateChild(key).content = content;
    }

    public T getKey() {
        return key;
    }

    public K getContent() {
        return content;
    }

    public List<TreeNode<T, K>> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return key.toString();
    }
}
