package gregtech.common.covers.filter.oreglob.node;

import it.unimi.dsi.fastutil.ints.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Node-based, state based evaluator for oreGlob.
 * <p>
 * OreGlob nodes are evaluated by simulating match for each possible branch of states.
 * Each state corresponds to current index of character the next match will start.
 * Each evaluation of node evaluates a union of output state for each input states.
 * And the next node is evaluated with previous output state as input state.
 * <p>
 * For example, matching the input {@code "ingotIron"} with string match node with
 * {@code "i"} value and state of {@code [ 0, 1, 2, 3, 4, 5 ]} would each produce these results.
 * <pre>
 *     0  =>  [ 1 ]  (the first "i" matches the string match node "i")
 *     1  =>  [ ]    (no match; "n" does not match the string match node "i")
 *     2  =>  [ ]    (no match; "g" does not match the string match node "i")
 *     3  =>  [ ]    (no match; "o" does not match the string match node "i")
 *     4  =>  [ ]    (no match; "t" does not match the string match node "i")
 *     5  =>  [ 6 ]  (the "I" matches the string match node "i"; oreglob is by default case insensitive.)
 * </pre>
 * When the next node is evaluated, the input state will be {@code [ 1, 6 ]} which is the output state of previous match.
 * <p>
 * All matches start with {@code [ 0 ]} as input state, and match is considered success
 * if output state contains length of the input string after evaluating all possible branch of the expression.
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
            root = root.visit(this);
            if (this.outputStates.isEmpty())
                break; // If no output states are provided after visiting, the match is aborted
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
    public void match(String match, boolean ignoreCase, boolean inverted) {
        IntIterator it = this.inputStates.iterator();
        while (it.hasNext()) {
            int state = it.nextInt();
            if (this.input.regionMatches(ignoreCase, state, match, 0, match.length())) {
                this.outputStates.add(state + match.length());
            }
        }
        if (inverted) invert();
    }

    @Override
    public void chars(int amount, boolean inverted) {
        IntIterator it = this.inputStates.iterator();
        while (it.hasNext()) {
            int state = it.nextInt();
            if (state + amount <= this.input.length()) {
                for (int i = 0; i < amount; i++)
                    state += Character.isSurrogate(this.input.charAt(i)) ? 2 : 1;
                this.outputStates.add(state);
            }
        }
        if (inverted) invert();
    }

    @Override
    public void charsOrMore(int amount, boolean inverted) {
        if (inverted) {
            if (amount > 0) { // inversion of 'zero or more' is impossible
                // less than n chars
                IntIterator it = this.inputStates.iterator();
                while (it.hasNext()) {
                    int state = it.nextInt();
                    IntIterator it2 = new PossibleStateIterator(state, calculateOffset(state, amount - 1));
                    while (it2.hasNext()) {
                        this.outputStates.add(it2.nextInt());
                    }
                }
            }
        } else {
            // Match n~ chars, where n is amount of characters needed to match the whole input
            IntIterator it = new PossibleStateIterator(computeMinInputState());
            it.skip(amount);
            while (it.hasNext()) this.outputStates.add(it.nextInt());
        }
    }

    @Override
    public void group(OreGlobNode node, boolean inverted) {
        evaluate(node);
        if (inverted) invert();
    }

    @Override
    public void branch(BranchType type, List<OreGlobNode> nodes, boolean inverted) {
        switch (type) {
            case OR: {
                // Compute max possible state for short circuit - if outputState of one branch is equal to U then
                // the entire set of possible output state is covered.
                // Max amount of states possible from input states is equal to number of characters left plus one full match state
                // This is a naive calculation; it does not account for surrogates.
                // But such case is rare and not accounting for that case only breaks short circuiting behavior, which is inconsequential
                int maxPossibleBranches = this.input.length() - computeMinInputState() + 1;
                for (OreGlobNode node : nodes) {
                    NodeInterpreter branchState = new NodeInterpreter(this.input, this.inputStates).evaluate(node);
                    this.outputStates.addAll(branchState.outputStates);
                    if (this.outputStates.size() >= maxPossibleBranches) break; // Already max
                }
                break;
            }
            case AND: {
                if (nodes.isEmpty()) {
                    IntIterator it = new PossibleStateIterator(computeMinInputState());
                    while (it.hasNext()) this.outputStates.add(it.nextInt());
                    return;
                }
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
                break;
            }
            case XOR:
                for (OreGlobNode node : nodes) {
                    NodeInterpreter branchState = new NodeInterpreter(this.input, this.inputStates).evaluate(node);

                    IntSet out2 = new IntOpenHashSet(branchState.outputStates);
                    out2.removeAll(this.outputStates); // out2 = { x in out2 AND x !in out }
                    this.outputStates.removeAll(branchState.outputStates); // out = { x in out AND x !in out2 }
                    this.outputStates.addAll(out2); // out = { ( x in out AND x !in out2 ) OR ( x in out2 AND x !in out ) }
                }
                break;
            default:
                throw new IllegalStateException("Unknown BranchType '" + type + "'");
        }
        if (inverted) invert();
    }

    @Override
    public void everything() {
        charsOrMore(0, false);
    }

    @Override
    public void impossible() {
        // Do not match anything!
    }

    @Override
    public void something() {
        charsOrMore(1, false);
    }

    @Override
    public void nothing() {
        // matches 0 chars; match every input state as-is
        this.outputStates.addAll(this.inputStates);
    }

    @Override
    public void error() {
        // Do not match anything!
    }

    private int computeMinInputState() {
        int minInputState = this.input.length();
        for (IntIterator it = this.inputStates.iterator(); it.hasNext(); ) {
            minInputState = Math.min(minInputState, it.nextInt());
        }
        return minInputState;
    }

    private int calculateOffset(int index, int offset) {
        for (int i = 0; i < offset; i++) {
            index += input.length() > i && Character.isSurrogate(input.charAt(i)) ? 2 : 1;
        }
        return index;
    }

    /**
     * Applies inversion to current outputStates.
     */
    private void invert() {
        int minInputState = computeMinInputState();

        // Max amount of states possible from input states is equal to number of characters left plus one full match state
        // This is a naive calculation; it does not account for surrogates.
        // But such case is rare and not accounting for that case only breaks short circuiting behavior, which is inconsequential
        int maxPossibleBranches = this.input.length() - minInputState + 1;

        // If outputStates is a set of max states, then its inversion is nothing
        if (this.outputStates.size() >= maxPossibleBranches) {
            this.outputStates.clear();
            return;
        }

        swapStateBuffer();
        IntIterator it = new PossibleStateIterator(minInputState);
        while (it.hasNext()) {
            int possibleState = it.nextInt();
            if (!this.inputStates.contains(possibleState)) this.outputStates.add(possibleState);
        }
    }

    private final class PossibleStateIterator implements IntIterator {
        private final int limitInclusive;
        private int i;

        PossibleStateIterator(int start) {
            this(start, input.length());
        }

        PossibleStateIterator(int start, int limitInclusive) {
            this.i = start;
            this.limitInclusive = limitInclusive;
        }

        @Override
        public int nextInt() {
            if (!hasNext()) throw new NoSuchElementException();
            int i = this.i;
            this.i += input.length() > i && Character.isSurrogate(input.charAt(i)) ? 2 : 1;
            return i;
        }

        @Override
        public int skip(int n) {
            int skipped = 0;
            while (hasNext() && n-- > 0) {
                this.i += input.length() > i && Character.isSurrogate(input.charAt(this.i)) ? 2 : 1;
                skipped += 1;
            }
            return skipped;
        }

        @Override
        public boolean hasNext() {
            return this.i <= limitInclusive;
        }

        @Override
        public Integer next() {
            return nextInt();
        }
    }
}
