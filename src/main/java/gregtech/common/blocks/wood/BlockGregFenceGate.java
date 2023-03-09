package gregtech.common.blocks.wood;

import gregtech.api.GregTechAPI;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;

public class BlockGregFenceGate extends BlockFenceGate {

    public BlockGregFenceGate() {
        super( BlockPlanks.EnumType.OAK);
        setHardness(2.0F);
        setResistance(5.0F);
        setSoundType(SoundType.WOOD);
        setCreativeTab(GregTechAPI.TAB_GREGTECH);
    }
}
