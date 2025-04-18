package gtqt.common.items;

import gregtech.api.items.metaitem.MetaItem;

import net.minecraft.item.EnumDyeColor;

public class GTQTMetaItems {

    public static MetaItem<?>.MetaValueItem COVER_PROGRAMMABLE_CIRCUIT;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_0;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_1;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_2;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_3;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_4;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_5;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_6;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_7;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_8;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_9;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_10;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_11;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_12;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_13;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_14;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_15;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_16;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_17;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_18;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_19;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_20;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_21;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_22;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_23;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_24;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_25;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_26;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_27;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_28;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_29;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_30;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_31;
    public static MetaItem<?>.MetaValueItem PROGRAMMABLE_CIRCUIT_32;

    public static final MetaItem<?>.MetaValueItem[] ENDLESS_SPRAY_CAN_DYES = new MetaItem.MetaValueItem[EnumDyeColor.values().length];
    public static MetaItem<?>.MetaValueItem ENDLESS_SPRAY_SOLVENT;
    public static MetaItem<?>.MetaValueItem ENDLESS_SPRAY_EMPTY;

    public static GTQTMetaItem1 GTQT_META_ITEM;

    public static void initialization() {
        GTQT_META_ITEM = new GTQTMetaItem1();

    }

    public static void initSubItems() {
        GTQTMetaItem1.registerItems();

    }

}
