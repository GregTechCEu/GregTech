package gregtech.api.items.toolitem;

import net.minecraft.block.Block;
import net.minecraft.item.ItemTool;

import java.util.Set;

/**
 * GT-styled ItemTool (generic tool item).
 *
 * Use this class if your tool isn't specialized (e.g. {@link GTSwordItem})
 */
public class GTToolItem extends ItemTool implements GTToolDefinition {

    protected GTToolItem(float attackDamageIn, float attackSpeedIn, ToolMaterial materialIn, Set<Block> effectiveBlocksIn) {
        super(attackDamageIn, attackSpeedIn, materialIn, effectiveBlocksIn);
    }


}
