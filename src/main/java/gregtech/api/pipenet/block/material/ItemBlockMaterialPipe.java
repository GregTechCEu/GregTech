package gregtech.api.pipenet.block.material;

import gregtech.api.pipenet.block.ItemBlockPipe;
import gregtech.api.unification.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockMaterialPipe<PipeType extends Enum<PipeType> & IMaterialPipeType<NodeDataType>, NodeDataType> extends ItemBlockPipe<PipeType, NodeDataType> {

    public ItemBlockMaterialPipe(BlockMaterialPipe<PipeType, NodeDataType, ?> block) {
        super(block);
    }

    @SuppressWarnings("unchecked")
    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        PipeType pipeType = blockPipe.getItemPipeType(stack);
        Material material = ((BlockMaterialPipe<PipeType, NodeDataType, ?>) blockPipe).getItemMaterial(stack);
        return material == null ? " " : pipeType.getOrePrefix().getLocalNameForItem(material);
    }
}
