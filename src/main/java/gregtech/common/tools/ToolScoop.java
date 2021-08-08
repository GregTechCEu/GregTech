package gregtech.common.tools;

import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.common.items.behaviors.ScoopBehaviour;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Set;

public class ToolScoop extends ToolBase {

    private static final Set<String> SCOOP_TOOL_CLASSES = Collections.singleton("scoop");

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return 2;
    }

    @Override
    public boolean canMineBlock(IBlockState block, ItemStack stack) {
        String tool = block.getBlock().getHarvestTool(block);
        return tool != null && tool.equals("scoop");
    }

    @Override
    public void onStatsAddedToTool(MetaValueItem item) {
        item.addComponents(new ScoopBehaviour(DamageValues.DAMAGE_FOR_SCOOP));
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return SCOOP_TOOL_CLASSES;
    }
}
