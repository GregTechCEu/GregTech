package gregtech.common.covers.filter.oreglob.impl;

import com.google.common.annotations.VisibleForTesting;
import gregtech.api.util.oreglob.OreGlob;
import gregtech.common.covers.filter.oreglob.node.OreGlobNode;

/**
 * Node-based implementation of oreglob.
 */
public final class NodeOreGlob extends OreGlob {

    private final OreGlobNode root;

    public NodeOreGlob(OreGlobNode root) {
        this.root = root;
    }

    @VisibleForTesting
    public OreGlobNode getRoot() {
        return root;
    }

    @Override
    public <V extends Visualizer> V visualize(V visualizer) {
        new NodeVisualizer(visualizer).visit(this.root);
        return visualizer;
    }

    @Override
    public boolean matches(String input) {
        return new NodeInterpreter(input).evaluate(this.root).isMatch();
    }
}
