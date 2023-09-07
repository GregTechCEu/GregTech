package gregtech.api.util.oreglob;

public enum VisualizationHint {
    /**
     * Plain text
     */
    TEXT,
    /**
     * Text indicating part of a node
     */
    NODE,
    /**
     * Text indicating some kind of value, whether it's string or number
     */
    VALUE,
    /**
     * Text indicating logical negation of the statement
     */
    NOT,
    /**
     * Text indication for each label in group nodes
     */
    LABEL,
    /**
     * Text indication for logical operation excluding negation
     */
    LOGIC,
    /**
     * Text indicating a syntax error; you shouldn't be able to see this
     */
    ERROR
}
