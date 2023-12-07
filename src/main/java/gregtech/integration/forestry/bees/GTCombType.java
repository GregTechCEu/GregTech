package gregtech.integration.forestry.bees;

import gregtech.api.GTValues;

import net.minecraftforge.fml.common.Loader;

public enum GTCombType {

    // Organic
    COAL("coal", 0x525252, 0x666666),
    COKE("coke", 0x4B4B4B, 0x7D7D7D),
    STICKY("stickyresin", 0x2E8F5B, 0xDCC289),
    OIL("oil", 0x333333, 0x4C4C4C),
    APATITE("apatite", 0xC1C1F6, 0x676784),
    ASH("ash", 0x1E1A18, 0xC6C6C6),
    BIOMASS("biomass", 0x17AF0E, 0x21E118),
    PHOSPHORUS("phosphorus", 0xC1C1F6, 0xFFC826),

    // Industrial
    ENERGY("energy", 0xC11F1F, 0xEBB9B9),
    LAPOTRON("lapotron", 0x1414FF, 0x6478FF),

    // Alloy
    REDALLOY("redalloy", 0xE60000, 0xB80000),
    STAINLESSSTEEL("stainlesssteel", 0x778899, 0xC8C8DC),

    // Gem
    STONE("stone", 0x808080, 0x999999),
    CERTUS("certus", 0x57CFFB, 0xBBEEFF),
    FLUIX("fluix", 0xA375FF, 0xB591FF, Loader.isModLoaded(GTValues.MODID_APPENG)),
    REDSTONE("redstone", 0x7D0F0F, 0xD11919),
    RAREEARTH("rareearth", 0x555643, 0x343428),
    LAPIS("lapis", 0x1947D1, 0x476CDA),
    RUBY("ruby", 0xE6005C, 0xCC0052),
    SAPPHIRE("sapphire", 0x0033CC, 0x00248F),
    DIAMOND("diamond", 0xCCFFFF, 0xA3CCCC),
    OLIVINE("olivine", 0x248F24, 0xCCFFCC),
    EMERALD("emerald", 0x248F24, 0x2EB82E),
    PYROPE("pyrope", 0x763162, 0x8B8B8B),
    GROSSULAR("grossular", 0x9B4E00, 0x8B8B8B),
    SPARKLING("sparkling", 0x7A007A, 0xFFFFFF, Loader.isModLoaded(GTValues.MODID_MB)),

    // Metal
    SLAG("slag", 0xD4D4D4, 0x58300B),
    COPPER("copper", 0xFF6600, 0xE65C00),
    TIN("tin", 0xD4D4D4, 0xDDDDDD),
    LEAD("lead", 0x666699, 0xA3A3CC),
    IRON("iron", 0xDA9147, 0xDE9C59),
    STEEL("steel", 0x808080, 0x999999),
    NICKEL("nickel", 0x8585AD, 0x9D9DBD),
    ZINC("zinc", 0xF0DEF0, 0xF2E1F2),
    SILVER("silver", 0xC2C2D6, 0xCECEDE),
    GOLD("gold", 0xE6B800, 0xCFA600),
    SULFUR("sulfur", 0x6F6F01, 0x8B8B8B),
    GALLIUM("gallium", 0x8B8B8B, 0xC5C5E4),
    ARSENIC("arsenic", 0x736C52, 0x292412),

    // Rare Metal
    BAUXITE("bauxite", 0x6B3600, 0x8B8B8B),
    ALUMINIUM("aluminium", 0x008AB8, 0xD6D6FF),
    MANGANESE("manganese", 0xD5D5D5, 0xCDE1B9),
    MAGNESIUM("magnesium", 0xF1D9D9, 0x8B8B8B),
    TITANIUM("titanium", 0xCC99FF, 0xDBB8FF),
    CHROME("chrome", 0xEBA1EB, 0xF2C3F2),
    TUNGSTEN("tungsten", 0x62626D, 0x161620),
    PLATINUM("platinum", 0xE6E6E6, 0xFFFFCC),
    IRIDIUM("iridium", 0xDADADA, 0xA1E4E4),
    MOLYBDENUM("molybdenum", 0xAEAED4, 0x8B8B8B),
    OSMIUM("osmium", 0x2B2BDA, 0x8B8B8B),
    LITHIUM("lithium", 0xF0328C, 0xE1DCFF),
    SALT("salt", 0xF0C8C8, 0xFAFAFA),
    ELECTROTINE("electrotine", 0x1E90FF, 0x3CB4C8),
    ALMANDINE("almandine", 0xC60000, 0x8B8B8B),
    INDIUM("indium", 0x8F5D99, 0xFFA9FF),

    // Radioactive
    URANIUM("uranium", 0x19AF19, 0x169E16),
    PLUTONIUM("plutonium", 0x240000, 0x570000),
    NAQUADAH("naquadah", 0x000000, 0x004400),
    NAQUADRIA("naquadria", 0x000000, 0x002400),
    TRINIUM("trinium", 0x9973BD, 0xC8C8D2),
    THORIUM("thorium", 0x001E00, 0x005000),
    LUTETIUM("lutetium", 0x0059FF, 0x00AAFF),
    AMERICIUM("americium", 0x0C453A, 0x287869),
    NEUTRONIUM("neutronium", 0xFFF0F0, 0xFAFAFA),

    // Noble Gas
    HELIUM("helium", 0xFFA9FF, 0xFFFFC3),
    ARGON("argon", 0x00FF00, 0x160822),
    XENON("xenon", 0x160822, 0x8A97B0),
    NEON("neon", 0xFAB4B4, 0xFFC826),
    KRYPTON("krypton", 0x80FF80, 0xFFFFC3),
    NITROGEN("nitrogen", 0x00BFC1, 0xFFFFFF),
    OXYGEN("oxygen", 0x8F8FFF, 0xFFFFFF),
    HYDROGEN("hydrogen", 0x0000B5, 0xFFFFFF),
    FLUORINE("fluorine", 0xFF6D00, 0x86AFF0);

    public static final GTCombType[] VALUES = values();

    public final boolean showInList;
    public final String name;
    public final int[] color;

    GTCombType(String name, int primary, int secondary) {
        this(name, primary, secondary, true);
    }

    GTCombType(String name, int primary, int secondary, boolean show) {
        this.name = name;
        this.color = new int[] { primary, secondary };
        this.showInList = show;
    }

    public static GTCombType getComb(int meta) {
        return meta < 0 || meta > VALUES.length ? VALUES[0] : VALUES[meta];
    }
}
