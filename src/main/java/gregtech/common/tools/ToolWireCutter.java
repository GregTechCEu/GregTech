package gregtech.common.tools;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Set;

public class ToolWireCutter extends ToolBase {

    private static final Set<String> CUTTER_TOOL_CLASSES = Collections.singleton("cutter");

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return 1;
    }

    @Override
    public int getToolDamagePerContainerCraft(ItemStack stack) {
        return 4;
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        return 1.25F;
    }

    @Override
    public boolean canMineBlock(IBlockState block, ItemStack stack) {
        String tool = block.getBlock().getHarvestTool(block);
        return tool != null && CUTTER_TOOL_CLASSES.contains(tool);
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return CUTTER_TOOL_CLASSES;
    }

    @Override
    public boolean canPlayBreakingSound(ItemStack stack, IBlockState state) {
        return canMineBlock(state, stack);
    }
}
