package gregtech.api.util.oreglob;

public enum VisualizationHint {
    TEXT, // Plain text
    NODE, // Text indicating part of a node
    VALUE, // Text indicating some kind of value, whether it's string or number
    NOT, // Text indicating logical negation of the statement
    LABEL, // Text indication for each label in group nodes
    LOGIC, // Text indication for logical operation excluding negation
    ERROR // Text indicating a syntax error; you shouldn't be able to see this
}
