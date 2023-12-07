package gregtech.common.covers.filter.oreglob.impl;

import gregtech.common.covers.filter.oreglob.node.BranchType;
import gregtech.common.covers.filter.oreglob.node.NodeVisitor;
import gregtech.common.covers.filter.oreglob.node.OreGlobNode;

import it.unimi.dsi.fastutil.ints.*;

import java.util.List;

/**
 * Node-based, state-based evaluator for oreGlob.
 * <p>
 * OreGlob nodes are evaluated by simulating match for each possible branch of states.
 * Each state corresponds to index of character the next match will start. All matches
 * start with {@code [ 0 ]} as input state, and match is considered success if output
 * state contains length of the input string after evaluating all possible branch of
 * the expression.
 * <p>
 * For example, matching the input {@code "ingotIron"} with string {@code "i"} and
 * input state of {@code [ 0, 1, 2, 3, 4, 5 ]} would each produce these output states.
 * 
 * <pre>
 *     0  =>  [ 1 ]  (the first "i" matches the string match node "i")
 *     1  =>  [ ]    (no match; "n" does not match the string match node "i")
 *     2  =>  [ ]    (no match; "g" does not match the string match node "i")
 *     3  =>  [ ]    (no match; "o" does not match the string match node "i")
 *     4  =>  [ ]    (no match; "t" does not match the string match node "i")
 *     5  =>  [ 6 ]  (the "I" matches the string match node "i"; oreglob is by default case insensitive.)
 * </pre>
 * 
 * When the next node gets evaluated, the input state will be the last evaluation result
 * from the last node; in the example above, input state for the node after {@code "i"}
 * will be {@code [ 1, 6 ]}.
 * <p>
 * Note that this implementation assumes both the input and match string consists of character
 * no bigger than {@code 0xFFFF} as their codepoint value; i.e. characters that UTF-16 can express
 * without using surrogate pairs.
 */
class NodeInterpreter implements NodeVisitor {

    private final String input;
    private IntSet inputStates;
    private IntSet outputStates = new IntLinkedOpenHashSet();

    NodeInterpreter(String input) {
        this.input = input;
        this.inputStates = new IntLinkedOpenHashSet();
        this.inputStates.add(0);
    }

    private NodeInterpreter(String input, IntCollection inputStates) {
        this.input = input;
        this.inputStates = new IntLinkedOpenHashSet(inputStates);
    }

    NodeInterpreter evaluate(OreGlobNode root) {
        boolean first = true;
        while (root != null) {
            if (first) first = false;
            else swapStateBuffer();
            root.visit(this);
            if (this.outputStates.isEmpty())
                break; // If no output states are provided after visiting, the match is aborted
            root = root.getNext();
        }
        return this;
    }

    boolean isMatch() {
        return this.outputStates.contains(this.input.length());
    }

    private void swapStateBuffer() {
        IntSet t = this.inputStates;
        this.inputStates = this.outputStates;
        this.outputStates = t;
        // clear output states for use
        t.clear();
    }

    @Override
    public void match(String match, boolean ignoreCase, boolean not) {
        IntIterator it = this.inputStates.iterator();
        while (it.hasNext()) {
            int state = it.nextInt();
            if (this.input.regionMatches(ignoreCase, state, match, 0, match.length())) {
                this.outputStates.add(state + match.length());
            }
        }
        if (not) negate();
    }

    @Override
    public void chars(int amount, boolean not) {
        if (not) {
            int state = computeMinInputState();
            for (int i = state + amount; state < i; state++) {
                this.outputStates.add(state);
            }
            for (state++; state <= this.input.length(); state++) {
                if (!this.inputStates.contains(state - amount)) this.outputStates.add(state);
            }
        } else {
            IntIterator it = this.inputStates.iterator();
            while (it.hasNext()) {
                int state = it.nextInt();
                if (state + amount <= this.input.length()) {
                    this.outputStates.add(state + amount);
                }
            }
        }
    }

    @Override
    public void charsOrMore(int amount, boolean not) {
        IntIterator it = this.inputStates.iterator();
        if (not) {
            // less than n chars
            while (it.hasNext()) {
                int state = it.nextInt();
                for (int i = state; i < state + amount; i++) {
                    this.outputStates.add(i);
                }
            }
        } else {
            // Match n~ chars, where n is amount of characters needed to match the whole input
            while (it.hasNext()) {
                int state = it.nextInt();
                for (int i = state + amount; i <= this.input.length(); i++) {
                    this.outputStates.add(i);
                }
            }
        }
    }

    @Override
    public void group(OreGlobNode node, boolean not) {
        evaluate(node);
        if (not) negate();
    }

    @Override
    public void branch(BranchType type, List<OreGlobNode> nodes, boolean not) {
        switch (type) {
            case OR -> {
                // Compute max possible state for short circuit - if outputState of one branch is equal to U then
                // the entire set of possible output state is covered.
                // Max amount of states possible from current input states is equal to
                // number of characters left plus one full match state
                int maxPossibleBranches = this.input.length() - computeMinInputState() + 1;
                for (OreGlobNode node : nodes) {
                    NodeInterpreter branchState = new NodeInterpreter(this.input, this.inputStates).evaluate(node);
                    this.outputStates.addAll(branchState.outputStates);
                    if (this.outputStates.size() >= maxPossibleBranches) break; // Already max
                }
            }
            case AND -> {
                boolean first = true;
                for (OreGlobNode node : nodes) {
                    NodeInterpreter branchState = new NodeInterpreter(this.input, this.inputStates).evaluate(node);
                    if (first) {
                        this.outputStates.addAll(branchState.outputStates);
                        first = false;
                    } else {
                        this.outputStates.retainAll(branchState.outputStates);
                    }
                    if (this.outputStates.isEmpty()) break; // Short circuit
                }
            }
            case XOR -> {
                for (OreGlobNode node : nodes) {
                    NodeInterpreter branchState = new NodeInterpreter(this.input, this.inputStates).evaluate(node);

                    IntSet out2 = new IntOpenHashSet(branchState.outputStates);
                    out2.removeAll(this.outputStates); // out2 = { x in out2 AND x !in out }
                    this.outputStates.removeAll(branchState.outputStates); // out = { x in out AND x !in out2 }
                    this.outputStates.addAll(out2); // out = { ( x in out AND x !in out2 ) OR ( x in out2 AND x !in out
                                                    // ) }
                }
            }
            default -> throw new IllegalStateException("Unknown BranchType '" + type + "'");
        }
        if (not) negate();
    }

    @Override
    public void everything() {
        for (int i = computeMinInputState(); i <= this.input.length(); i++) {
            this.outputStates.add(i);
        }
    }

    @Override
    public void nothing() {
        // Do not match anything!
    }

    @Override
    public void nonempty() {
        for (int i = computeMinInputState() + 1; i <= this.input.length(); i++) {
            this.outputStates.add(i);
        }
    }

    @Override
    public void empty() {
        // matches 0 chars; match every input state as-is
        this.outputStates.addAll(this.inputStates);
    }

    @Override
    public void error() {
        // Do not match anything!
    }

    private int computeMinInputState() {
        int minInputState = this.input.length();
        IntIterator it = this.inputStates.iterator();
        while (it.hasNext()) {
            minInputState = Math.min(minInputState, it.nextInt());
        }
        return minInputState;
    }

    /**
     * Applies logical complement to current outputStates.
     */
    private void negate() {
        int minInputState = computeMinInputState();

        // Max amount of states possible from current input states is equal to
        // number of characters left plus one full match state
        int maxPossibleBranches = this.input.length() - minInputState + 1;

        // If outputStates is a set of max states, then its complement set is nothing
        if (this.outputStates.size() >= maxPossibleBranches) {
            this.outputStates.clear();
            return;
        }

        swapStateBuffer();
        for (int i = minInputState; i <= this.input.length(); i++) {
            if (!this.inputStates.contains(i)) this.outputStates.add(i);
        }
    }
}
