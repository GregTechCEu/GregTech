package gregtech.client.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ICCLBlockRenderer {
    @SideOnly(Side.CLIENT)
    void renderItem(ItemStack rawStack, ItemCameraTransforms.TransformType transformType);

    @SideOnly(Side.CLIENT)
    void renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, BufferBuilder buffer);
}
