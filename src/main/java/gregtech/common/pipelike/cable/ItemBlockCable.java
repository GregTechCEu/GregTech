package gregtech.common.pipelike.cable;

import gregtech.api.GTValues;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.block.material.ItemBlockMaterialPipe;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.util.GTUtility;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemBlockCable extends ItemBlockMaterialPipe<Insulation, WireProperties> {

    public ItemBlockCable(BlockCable block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        WireProperties wireProperties = blockPipe.createItemProperties(stack);
        int tier = GTUtility.getTierByVoltage(wireProperties.getVoltage());
        if (wireProperties.isSuperconductor())
            tooltip.add(I18n.format("gregtech.cable.superconductor", GTValues.VN[tier]));
        tooltip.add(I18n.format("gregtech.cable.voltage", wireProperties.getVoltage(), GTValues.VNF[tier]));
        tooltip.add(I18n.format("gregtech.cable.amperage", wireProperties.getAmperage()));
        tooltip.add(I18n.format("gregtech.cable.loss_per_block", wireProperties.getLossPerBlock()));

        if (TooltipHelper.isShiftDown()) {
            tooltip.add(I18n.format("gregtech.tool_action.wire_cutter.connect"));
            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
            tooltip.add(I18n.format("gregtech.tool_action.crowbar"));
        } else {
            tooltip.add(I18n.format("gregtech.tool_action.show_tooltips"));
        }

        if (ConfigHolder.misc.debug) {
            BlockMaterialPipe<?, ?, ?> blockMaterialPipe = (BlockMaterialPipe<?, ?, ?>) blockPipe;
            tooltip.add("MetaItem Id: " + blockMaterialPipe.getPrefix().name +
                    blockMaterialPipe.getItemMaterial(stack).toCamelCaseString());
        }
    }
}
