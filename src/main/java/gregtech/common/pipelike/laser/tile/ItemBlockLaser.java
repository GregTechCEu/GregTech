package gregtech.common.pipelike.laser.tile;
import gregtech.api.GTValues;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.ItemBlockPipe;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.block.material.ItemBlockMaterialPipe;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.common.pipelike.laser.net.BlockLaser;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import javax.annotation.Nullable;
import java.util.List;
public class ItemBlockLaser extends ItemBlockMaterialPipe<LaserSize, LaserProperties> {


    public ItemBlockLaser(BlockLaser block) {
        super(block);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        LaserProperties wireProperties = blockPipe.createItemProperties(stack);
        String voltageName = GTValues.VN[GTUtility.getTierByVoltage(wireProperties.laserVoltage)];
        tooltip.add(I18n.format("gregtech.cable.voltage", wireProperties.laserVoltage, voltageName));
        tooltip.add(I18n.format("gregtech.cable.amperage", wireProperties.parallel));

    }
}
