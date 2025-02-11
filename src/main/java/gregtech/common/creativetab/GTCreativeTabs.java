package gregtech.common.creativetab;

import gregtech.api.GTValues;
import gregtech.api.creativetab.BaseCreativeTab;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.BlockWarningSign;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.items.ToolItems;
import gregtech.common.metatileentities.MetaTileEntities;

public final class GTCreativeTabs {

    public static final BaseCreativeTab TAB_GREGTECH = new BaseCreativeTab(GTValues.MODID + ".main",
            () -> MetaItems.LOGO.getStackForm(), true);
    public static final BaseCreativeTab TAB_GREGTECH_MACHINES = new BaseCreativeTab(GTValues.MODID + ".machines",
            () -> MetaTileEntities.ELECTRIC_BLAST_FURNACE.getStackForm(), true);
    public static final BaseCreativeTab TAB_GREGTECH_CABLES = new BaseCreativeTab(GTValues.MODID + ".cables",
            () -> OreDictUnifier.get(OrePrefix.cableGtDouble, Materials.Aluminium), true);
    public static final BaseCreativeTab TAB_GREGTECH_PIPES = new BaseCreativeTab(GTValues.MODID + ".pipes",
            () -> OreDictUnifier.get(OrePrefix.pipeNormal, Materials.Aluminium), true);
    public static final BaseCreativeTab TAB_GREGTECH_TOOLS = new BaseCreativeTab(GTValues.MODID + ".tools",
            () -> ToolItems.HARD_HAMMER.get(Materials.Aluminium), true);
    public static final BaseCreativeTab TAB_GREGTECH_MATERIALS = new BaseCreativeTab(GTValues.MODID + ".materials",
            () -> OreDictUnifier.get(OrePrefix.ingot, Materials.Aluminium), true);
    public static final BaseCreativeTab TAB_GREGTECH_ORES = new BaseCreativeTab(GTValues.MODID + ".ores",
            () -> OreDictUnifier.get(OrePrefix.ore, Materials.Aluminium), true);
    public static final BaseCreativeTab TAB_GREGTECH_DECORATIONS = new BaseCreativeTab(GTValues.MODID + ".decorations",
            () -> MetaBlocks.WARNING_SIGN.getItemVariant(BlockWarningSign.SignType.YELLOW_STRIPES), true);

    private GTCreativeTabs() {}
}
