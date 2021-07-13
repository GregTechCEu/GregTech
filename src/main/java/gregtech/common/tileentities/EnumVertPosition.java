package gregtech.common.tileentities;

public enum EnumVertPosition {
    FLOOR(0),
    WALL(1),
    CEILING(2);

    private int ID;

    EnumVertPosition(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return this.ID;
    }

    public static EnumVertPosition getEnumFromID(int id) {
        EnumVertPosition position = FLOOR;
        switch (id) {
            case 1:
                position = WALL;
                break;
            case 2:
                position = CEILING;
                break;
        }
        return position;
    }
}
