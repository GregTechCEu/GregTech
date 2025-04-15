package gtqt.common.items;

import gregtech.api.items.metaitem.StandardMetaItem;

import gtqt.common.GTQTCommonProxy;
import gtqt.common.items.behaviors.ProgrammableCircuit;
public class GTQTMetaItem1 extends StandardMetaItem {

    public GTQTMetaItem1() {
        this.setRegistryName("gtqt_meta_item_0");
        setCreativeTab(GTQTCommonProxy.GTQTCore_TAB);
    }

    public void registerSubItems() {
        GTQTMetaItems.COVER_PROGRAMMABLE_CIRCUIT = addItem(1, "cover.programmable_circuit").setCreativeTabs(
                GTQTCommonProxy.GTQTCore_PC);

        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_0 = this.addItem(20, "programmable_circuit_0").addComponents(new ProgrammableCircuit(0, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_1 = this.addItem(21, "programmable_circuit_1").addComponents(new ProgrammableCircuit(1, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_2 = this.addItem(22, "programmable_circuit_2").addComponents(new ProgrammableCircuit(2, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_3 = this.addItem(23, "programmable_circuit_3").addComponents(new ProgrammableCircuit(3, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_4 = this.addItem(24, "programmable_circuit_4").addComponents(new ProgrammableCircuit(4, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_5 = this.addItem(25, "programmable_circuit_5").addComponents(new ProgrammableCircuit(5, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_6 = this.addItem(26, "programmable_circuit_6").addComponents(new ProgrammableCircuit(6, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_7 = this.addItem(27, "programmable_circuit_7").addComponents(new ProgrammableCircuit(7, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_8 = this.addItem(28, "programmable_circuit_8").addComponents(new ProgrammableCircuit(8, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_9 = this.addItem(29, "programmable_circuit_9").addComponents(new ProgrammableCircuit(9, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_10 = this.addItem(30, "programmable_circuit_10").addComponents(new ProgrammableCircuit(10, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_11 = this.addItem(31, "programmable_circuit_11").addComponents(new ProgrammableCircuit(11, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_12 = this.addItem(32, "programmable_circuit_12").addComponents(new ProgrammableCircuit(12, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_13 = this.addItem(33, "programmable_circuit_13").addComponents(new ProgrammableCircuit(13, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_14 = this.addItem(34, "programmable_circuit_14").addComponents(new ProgrammableCircuit(14, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_15 = this.addItem(35, "programmable_circuit_15").addComponents(new ProgrammableCircuit(15, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_16 = this.addItem(36, "programmable_circuit_16").addComponents(new ProgrammableCircuit(16, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_17 = this.addItem(37, "programmable_circuit_17").addComponents(new ProgrammableCircuit(17, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_18 = this.addItem(38, "programmable_circuit_18").addComponents(new ProgrammableCircuit(18, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_19 = this.addItem(39, "programmable_circuit_19").addComponents(new ProgrammableCircuit(19, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_20 = this.addItem(40, "programmable_circuit_20").addComponents(new ProgrammableCircuit(20, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_21 = this.addItem(41, "programmable_circuit_21").addComponents(new ProgrammableCircuit(21, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_22 = this.addItem(42, "programmable_circuit_22").addComponents(new ProgrammableCircuit(22, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_23 = this.addItem(43, "programmable_circuit_23").addComponents(new ProgrammableCircuit(23, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_24 = this.addItem(44, "programmable_circuit_24").addComponents(new ProgrammableCircuit(24, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_25 = this.addItem(45, "programmable_circuit_25").addComponents(new ProgrammableCircuit(25, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_26 = this.addItem(46, "programmable_circuit_26").addComponents(new ProgrammableCircuit(26, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_27 = this.addItem(47, "programmable_circuit_27").addComponents(new ProgrammableCircuit(27, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_28 = this.addItem(48, "programmable_circuit_28").addComponents(new ProgrammableCircuit(28, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_29 = this.addItem(49, "programmable_circuit_29").addComponents(new ProgrammableCircuit(29, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_30 = this.addItem(50, "programmable_circuit_30").addComponents(new ProgrammableCircuit(30, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_31 = this.addItem(51, "programmable_circuit_31").addComponents(new ProgrammableCircuit(31, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);
        GTQTMetaItems.PROGRAMMABLE_CIRCUIT_32 = this.addItem(52, "programmable_circuit_32").addComponents(new ProgrammableCircuit(32, "programmable_circuit")).setCreativeTabs(GTQTCommonProxy.GTQTCore_PC);

    }
}
