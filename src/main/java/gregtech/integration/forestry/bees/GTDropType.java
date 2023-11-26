package gregtech.integration.forestry.bees;

public enum GTDropType {

    OIL("oil", 0x19191B, 0x303032),
    BIOMASS("biomass", 0x21E118, 0x17AF0E),
    ETHANOL("ethanol", 0xCE5504, 0x853703),
    MUTAGEN("mutagen", 0xFFC100, 0x00FF11);

    public static final GTDropType[] VALUES = values();

    public final String name;
    public final int[] color;

    GTDropType(String name, int primary, int secondary) {
        this.name = name;
        this.color = new int[] { primary, secondary };
    }

    public static GTDropType getDrop(int meta) {
        return meta < 0 || meta >= VALUES.length ? VALUES[0] : VALUES[meta];
    }
}
