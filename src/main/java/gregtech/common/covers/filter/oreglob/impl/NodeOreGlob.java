package gregtech.common.covers.filter.oreglob.impl;

import gregtech.api.util.oreglob.OreGlob;
import gregtech.api.util.oreglob.OreGlobTextBuilder;
import gregtech.common.covers.filter.oreglob.node.OreGlobNode;

import com.google.common.annotations.VisibleForTesting;

import javax.annotation.Nonnull;

/**
 * Node-based implementation of oreglob.
 */
public final class NodeOreGlob extends OreGlob {

    private final OreGlobNode root;

    public NodeOreGlob(@Nonnull OreGlobNode root) {
        this.root = root;
    }

    @VisibleForTesting
    public OreGlobNode getRoot() {
        return root;
    }

    @Nonnull
    @Override
    public <V extends OreGlobTextBuilder> V visualize(@Nonnull V visualizer) {
        new NodeVisualizer(visualizer).visit(this.root);
        return visualizer;
    }

    @Override
    public boolean matches(@Nonnull String input) {
        return new NodeInterpreter(input).evaluate(this.root).isMatch();
    }
}
