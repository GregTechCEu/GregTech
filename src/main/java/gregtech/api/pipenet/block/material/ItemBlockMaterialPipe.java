package gregtech.api.pipenet.block.material;

import gregtech.api.pipenet.block.ItemBlockPipe;
import gregtech.api.unification.material.Material;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class ItemBlockMaterialPipe<PipeType extends Enum<PipeType> & IMaterialPipeType<NodeDataType>, NodeDataType>
                                  extends ItemBlockPipe<PipeType, NodeDataType> {

    public ItemBlockMaterialPipe(BlockMaterialPipe<PipeType, NodeDataType, ?> block) {
        super(block);
    }

    @NotNull
    @Override
    public String getItemStackDisplayName(@NotNull ItemStack stack) {
        PipeType pipeType = blockPipe.getItemPipeType(stack);
        Material material = ((BlockMaterialPipe<PipeType, NodeDataType, ?>) blockPipe).getItemMaterial(stack);
        return material == null ? " " : pipeType.getOrePrefix().getLocalNameForItem(material);
    }
}
