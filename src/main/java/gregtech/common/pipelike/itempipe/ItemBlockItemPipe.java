package gregtech.common.pipelike.itempipe;

import gregtech.api.pipenet.block.material.ItemBlockMaterialPipe;
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
        //FluidPipeProperties pipeProperties = blockPipe.createItemProperties(stack);
        //tooltip.add(I18n.format("gregtech.fluid_pipe.throughput", pipeProperties.throughput * 20));
        //tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", pipeProperties.maxFluidTemperature));
        //if (!pipeProperties.gasProof) tooltip.add(I18n.format("gregtech.fluid_pipe.non_gas_proof"));
    }
}
