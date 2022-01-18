package gregtech.common.pipelike.cable;

import gregtech.api.GTValues;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
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
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        WireProperties wireProperties = blockPipe.createItemProperties(stack);
        int tier = GTUtility.getTierByVoltage(wireProperties.getVoltage());
        if (wireProperties.isSuperconductor()) tooltip.add(I18n.format("gregtech.cable.superconductor", GTValues.VN[tier]));
        tooltip.add(I18n.format("gregtech.cable.voltage", wireProperties.getVoltage(), GTValues.VNF[tier]));
        tooltip.add(I18n.format("gregtech.cable.amperage", wireProperties.getAmperage()));
        tooltip.add(I18n.format("gregtech.cable.loss_per_block", wireProperties.getLossPerBlock()));

        if (flagIn.isAdvanced()) {
            tooltip.add("MetaItem Id: " + ((BlockMaterialPipe<?, ?, ?>)blockPipe).getPrefix().name + ((BlockMaterialPipe<?, ?, ?>)blockPipe).getItemMaterial(stack).toCamelCaseString());
        }
    }
}
