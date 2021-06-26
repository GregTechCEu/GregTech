package gregtech.common.pipelike.laser.tile;
import gregtech.api.GTValues;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.ItemBlockPipe;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import javax.annotation.Nullable;
import java.util.List;
public class ItemBlockLaser extends ItemBlockPipe<LaserSize, LaserProperties>  {

    public ItemBlockLaser(BlockPipe block) {
        super(block);
    }

    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        LaserSize pipeType = blockPipe.getItemPipeType(stack);
        OrePrefix orePrefix = pipeType.getOrePrefix();
        String specfiedUnlocalized = "item.oreprefix." + orePrefix.name();
        return I18n.format(specfiedUnlocalized);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        LaserProperties opticalFiberProperties = blockPipe.createItemProperties(stack);
       // String voltageName = String.valueOf(GTValues.V[opticalFiberProperties.laserVoltage]);
        tooltip.add(I18n.format("laser_voltage", opticalFiberProperties.laserVoltage));
        tooltip.add(I18n.format("laser_parallel", opticalFiberProperties.parallel));
    }
}
