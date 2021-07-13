package gregtech.common.tileentities;

public enum EnumShiftPosition {
    NO_SHIFT(0),
    HALF_SHIFT(1),
    FULL_SHIFT(2);

    private int ID;

    EnumShiftPosition(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return this.ID;
    }

    public static EnumShiftPosition getEnumFromID(int id) {
        EnumShiftPosition position = NO_SHIFT;
        switch (id) {
            case 1:
                position = HALF_SHIFT;
                break;
            case 2:
                position = FULL_SHIFT;
                break;
        }
        return position;
    }
}
