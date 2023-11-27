package gregtech.common.covers.filter.oreglob.impl;

import gregtech.api.util.oreglob.OreGlob;
import gregtech.api.util.oreglob.OreGlobTextBuilder;
import gregtech.common.covers.filter.oreglob.node.OreGlobNode;

import com.google.common.annotations.VisibleForTesting;
import org.jetbrains.annotations.NotNull;

/**
 * Node-based implementation of oreglob.
 */
public final class NodeOreGlob extends OreGlob {

    private final OreGlobNode root;

    public NodeOreGlob(@NotNull OreGlobNode root) {
        this.root = root;
    }

    @VisibleForTesting
    public OreGlobNode getRoot() {
        return root;
    }

    @NotNull
    @Override
    public <V extends OreGlobTextBuilder> V visualize(@NotNull V visualizer) {
        new NodeVisualizer(visualizer).visit(this.root);
        return visualizer;
    }

    @Override
    public boolean matches(@NotNull String input) {
        return new NodeInterpreter(input).evaluate(this.root).isMatch();
    }
}
