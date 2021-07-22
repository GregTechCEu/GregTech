package gregtech.common.pipelike.itempipe;

import gregtech.api.pipenet.block.material.ItemBlockMaterialPipe;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockItemPipe extends ItemBlockMaterialPipe<ItemPipeType, ItemPipeProperties> {

    public ItemBlockItemPipe(BlockItemPipe block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        ItemPipeProperties pipeProperties = blockPipe.createItemProperties(stack);
        if (pipeProperties.transferRate % 1 != 0)
            tooltip.add(I18n.format("gregtech.item_pipe.rate_items", (int) ((pipeProperties.transferRate * 64) + 0.5)));
        else
            tooltip.add(I18n.format("gregtech.item_pipe.rate_stacks", (int) pipeProperties.transferRate));
        tooltip.add(I18n.format("gregtech.item_pipe.priority", pipeProperties.priority));
    }
}
