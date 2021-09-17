package gregtech.api;

import gregtech.api.block.machines.BlockMachine;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.util.BaseCreativeTab;
import gregtech.api.util.IBlockOre;
import gregtech.common.items.MetaItems;

import java.util.HashMap;
import java.util.Map;

public class GregTechAPI {

    public static BlockMachine MACHINE;
    public static final Map<Material, Map<StoneType, IBlockOre>> oreBlockTable = new HashMap<>();

    public static final BaseCreativeTab TAB_GREGTECH =
            new BaseCreativeTab(GTValues.MODID + ".main", () -> MetaItems.BATTERY_HULL_HV.getStackForm(), true);
    public static final BaseCreativeTab TAB_GREGTECH_MATERIALS =
            new BaseCreativeTab(GTValues.MODID + ".materials", () -> OreDictUnifier.get(OrePrefix.ingot, Materials.Aluminium), true);
    public static final BaseCreativeTab TAB_GREGTECH_ORES =
            new BaseCreativeTab(GTValues.MODID + ".ores", () -> MetaItems.DRILL_MV.getStackForm(), true);
}
