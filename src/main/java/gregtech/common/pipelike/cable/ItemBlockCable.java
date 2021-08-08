package gregtech.common.pipelike.cable;

import gregtech.api.GTValues;
import gregtech.api.pipenet.block.material.ItemBlockMaterialPipe;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.util.GTUtility;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockCable extends ItemBlockMaterialPipe<Insulation, WireProperties> {

    public ItemBlockCable(BlockCable block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        WireProperties wireProperties = blockPipe.createItemProperties(stack);
        String voltageName = GTValues.VN[GTUtility.getTierByVoltage(wireProperties.voltage)];
        tooltip.add(I18n.format("gregtech.cable.voltage", wireProperties.voltage, voltageName));
        tooltip.add(I18n.format("gregtech.cable.amperage", wireProperties.amperage));
        tooltip.add(I18n.format("gregtech.cable.loss_per_block", wireProperties.lossPerBlock));
    }
}
