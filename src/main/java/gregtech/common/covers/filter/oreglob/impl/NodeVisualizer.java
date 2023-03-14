package gregtech.common.covers.filter.oreglob.impl;

import gregtech.api.util.oreglob.OreGlob;
import gregtech.common.covers.filter.oreglob.node.BranchNode.BranchType;
import gregtech.common.covers.filter.oreglob.node.NodeVisitor;
import gregtech.common.covers.filter.oreglob.node.OreGlobNode;

import java.util.List;

import static gregtech.api.util.oreglob.OreGlob.VisualizationHint.*;

class NodeVisualizer implements NodeVisitor {
    private final OreGlob.Visualizer visualizer;
    private final int indents;

    NodeVisualizer(OreGlob.Visualizer visualizer) {
        this(visualizer, 0);
    }

    NodeVisualizer(OreGlob.Visualizer visualizer, int indents) {
        this.visualizer = visualizer;
        this.indents = indents;
    }

    void visit(OreGlobNode node) {
        boolean first = true;
        while (node != null) {
            if (first) first = false;
            else {
                visualizer.newLine(indents);
                visualizer.text("... followed by ");
            }
            node = node.visit(this);
        }
    }

    @Override
    public void match(String match, boolean ignoreCase, boolean inverted) {
        if (inverted) visualizer.text("not ", LOGIC_INVERSION);
        visualizer.text("'", NODE);
        visualizer.text(match, VALUE);
        visualizer.text("'", NODE);
    }

    @Override
    public void chars(int amount, boolean inverted) {
        if (inverted) visualizer.text("either more or less than ", LOGIC_INVERSION);
        visualizer.number(amount, VALUE);
        visualizer.text(amount == 1 ? " character" : " characters", NODE);
    }

    @Override
    public void charsOrMore(int amount, boolean inverted) {
        if (inverted) {
            visualizer.text("less than ", NODE);
            visualizer.number(amount);
            visualizer.text(" characters", NODE);
        } else {
            visualizer.number(amount);
            visualizer.text(" or more characters", NODE);
        }
    }

    @Override
    public void group(OreGlobNode node, boolean inverted) {
        if (inverted) {
            visualizer.text("not", LOGIC_INVERSION);
            visualizer.text(":", NODE);
            visualizer.newLine(indents + 1);
            new NodeVisualizer(visualizer, indents + 1).visit(node);
        } else {
            visit(node);
        }
    }

    @Override
    public void branch(BranchType type, List<OreGlobNode> nodes, boolean inverted) {
        switch (type) {
            case OR:
                if (inverted) {
                    visualizer.text("anything that ", NODE);
                    visualizer.text("isn't", LOGIC_INVERSION);
                    visualizer.text(" one of...", NODE);
                } else {
                    visualizer.text("one of...", NODE);
                }
                for (int i = 0; i < nodes.size(); i++) {
                    OreGlobNode node = nodes.get(i);
                    visualizer.newLine(indents);
                    if (i == 0) {
                        visualizer.text("> ", LABEL);
                    } else {
                        visualizer.text("> or ", LABEL);
                    }
                    new NodeVisualizer(visualizer, indents + 1).visit(node);
                }
                return;
            case AND:
                if (inverted) {
                    visualizer.text("anything that ", NODE);
                    visualizer.text("isn't", LOGIC_INVERSION);
                    visualizer.text("...", NODE);
                } else {
                    visualizer.text("anything that is...", NODE);
                }
                for (int i = 0; i < nodes.size(); i++) {
                    OreGlobNode node = nodes.get(i);
                    visualizer.newLine(indents);
                    if (i == 0) {
                        visualizer.text("> ", LABEL);
                    } else {
                        visualizer.text("> and ", LABEL);
                    }
                    new NodeVisualizer(visualizer, indents + 1).visit(node);
                }
                return;
            case XOR:
                // TODO no idea if this visualization is logically correct
                //  does ! ( a ^ b ^ c ) equal to ( a eq b eq c )?
                if (inverted) {
                    visualizer.text("either both or none of...", LOGIC_INVERSION);
                } else {
                    visualizer.text("only one of...", NODE);
                }
                // Needs to do special shit because the cursed nature of XOR makes it
                // impossible to display them in neat, organized fashion
                for (int i = 0; i < nodes.size() - 1; i++) {
                    visualizer.newLine(indents + i);
                    visualizer.text("> ", LABEL);
                    new NodeVisualizer(visualizer, indents + i + 1).visit(nodes.get(i));
                    visualizer.newLine(indents + i);
                    visualizer.text("> ", LABEL);
                    if (i == nodes.size() - 2) { // append last entry
                        new NodeVisualizer(visualizer, indents + i + 1).visit(nodes.get(nodes.size() - 1));
                    } else { // append another XOR text
                        if (inverted) {
                            visualizer.text("either both or none of...", LOGIC_INVERSION);
                        } else {
                            visualizer.text("only one of...", NODE);
                        }
                    }
                }
                return;
            default:
                throw new IllegalStateException("Unknown BranchType '" + type + "'");
        }
    }

    @Override
    public void everything() {
        everything(this.visualizer);
    }

    @Override
    public void impossible() {
        impossible(this.visualizer);
    }

    @Override
    public void something() {
        something(this.visualizer);
    }

    @Override
    public void nothing() {
        nothing(this.visualizer);
    }

    @Override
    public void error() {
        error(this.visualizer);
    }

    public static void everything(OreGlob.Visualizer visualizer) {
        visualizer.text("anything", NODE);
    }

    public static void impossible(OreGlob.Visualizer visualizer) {
        visualizer.text("(impossible to match)", LOGIC_INVERSION);
    }

    public static void something(OreGlob.Visualizer visualizer) {
        visualizer.text("something", NODE);
    }

    public static void nothing(OreGlob.Visualizer visualizer) {
        visualizer.text("nothing", LOGIC_INVERSION);
    }

    public static void error(OreGlob.Visualizer visualizer) {
        visualizer.text("ERROR!", ERROR);
    }
}
