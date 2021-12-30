package gregtech.common.tools;

import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.common.items.behaviors.CrowbarBehaviour;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Set;

public class ToolCrowbar extends ToolBase {

    private static final Set<String> CROWBAR_TOOL_CLASSES = Collections.singleton("crowbar");

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return 1;
    }

    @Override
    public int getToolDamagePerContainerCraft(ItemStack stack) {
        return 1;
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        return 2.0F;
    }

    @Override
    public boolean isCrowbar(ItemStack stack) {
        return true;
    }

    @Override
    public void onStatsAddedToTool(MetaValueItem item) {
        item.addComponents(new CrowbarBehaviour(DamageValues.DAMAGE_FOR_CROWBAR));
    }

    @Override
    public boolean canMineBlock(IBlockState block, ItemStack stack) {
        String tool = block.getBlock().getHarvestTool(block);
        return (tool != null && CROWBAR_TOOL_CLASSES.contains(tool)) ||
                block.getMaterial() == Material.CIRCUITS;
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return CROWBAR_TOOL_CLASSES;
    }

    @Override
    public boolean canPlayBreakingSound(ItemStack stack, IBlockState state) {
        return canMineBlock(state, stack);
    }
}
