package gregtech.api.terminal.util;


import java.util.ArrayList;
import java.util.List;

/***
 * Tree
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

    public boolean isLeaf(){
        return children == null || children.isEmpty();
    }

    public TreeNode<T, K> getOrCreateChild (T childKey) {
        TreeNode<T, K> result;
        if (children != null) {
            result = children.stream().filter(child->child.key.equals(childKey)).findFirst().orElseGet(()->{
                TreeNode<T, K> newNode = new TreeNode<>(dimension + 1, childKey);
                children.add(newNode);
                return newNode;
            });
        } else {
            children = new ArrayList<>();
            result = new TreeNode<>(dimension + 1, childKey);
            children.add(result);
        }
        return result;
    }

    public void addContent (T key, K content) {
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
