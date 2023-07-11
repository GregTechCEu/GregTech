package gregtech.common.pipelike.optical;

import gregtech.api.pipenet.block.ItemBlockPipe;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockOpticalPipe extends ItemBlockPipe<OpticalPipeType, OpticalPipeProperties> {

    public ItemBlockOpticalPipe(BlockOpticalPipe block) {
        super(block);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(I18n.format("tile.optical_pipe_normal.tooltip1"));
    }
}
