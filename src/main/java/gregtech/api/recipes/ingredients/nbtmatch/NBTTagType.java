package gregtech.api.recipes.ingredients.nbtmatch;

public enum NBTTagType {

    BOOLEAN(1),
    BYTE(1),
    SHORT(2),
    INT(3),
    LONG(4),
    FLOAT(5),
    DOUBLE(6),
    BYTE_ARRAY(7),
    STRING(8),
    LIST(9),
    COMPOUND(10),
    INT_ARRAY(11),
    LONG_ARRAY(12),
    NUMBER(99);

    public static boolean isNumeric(NBTTagType type) {
        return type == BYTE || type == SHORT || type == INT || type == LONG || type == FLOAT || type == DOUBLE ||
                type == NUMBER;
    }

    public final int typeId;

    NBTTagType(int typeId) {
        this.typeId = typeId;
    }
}
