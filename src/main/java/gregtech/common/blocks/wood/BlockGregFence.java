package gregtech.common.blocks.wood;

import gregtech.api.GregTechAPI;
import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.block.BlockFence;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class BlockGregFence extends BlockFence {

    public BlockGregFence() {
        super(Material.WOOD, MapColor.WOOD);
        setHardness(2.0F);
        setResistance(5.0F);
        setSoundType(SoundType.WOOD);
        setCreativeTab(GregTechAPI.TAB_GREGTECH_DECORATIONS);
        setHarvestLevel(ToolClasses.AXE, 0);
    }
}
