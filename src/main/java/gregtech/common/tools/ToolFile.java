package gregtech.common.tools;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Set;

public class ToolFile extends ToolBase {

    private static final Set<String> FILE_TOOL_CLASSES = Collections.singleton("file");

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
        return 1.5F;
    }

    @Override
    public boolean canMineBlock(IBlockState block, ItemStack stack) {
        String tool = block.getBlock().getHarvestTool(block);
        return (tool != null && FILE_TOOL_CLASSES.contains(tool)) ||
                block.getMaterial() == Material.CIRCUITS;
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return FILE_TOOL_CLASSES;
    }
}
