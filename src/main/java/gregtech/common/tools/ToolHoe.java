package gregtech.common.tools;

import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.common.items.behaviors.HoeBehaviour;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Set;

public class ToolHoe extends ToolBase {

    private static final Set<String> HOE_TOOL_CLASSES = Collections.singleton("hoe");

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return 1;
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        return 1.75F;
    }

    @Override
    public boolean canMineBlock(IBlockState block, ItemStack stack) {
        String tool = block.getBlock().getHarvestTool(block);
        return (tool != null && HOE_TOOL_CLASSES.contains(tool)) ||
                block.getMaterial() == Material.GROUND;
    }

    @Override
    public void onStatsAddedToTool(MetaValueItem item) {
        item.addComponents(new HoeBehaviour(DamageValues.DAMAGE_FOR_HOE));
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return HOE_TOOL_CLASSES;
    }
}
