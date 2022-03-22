package gregtech.common.tools;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.toolitem.IToolStats;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;

public class HardHammerBehavior implements IItemBehaviour {

    private final int cost;

    public HardHammerBehavior(int cost) {
        this.cost = cost;
    }

    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (world.isRemote || world.isAirBlock(pos)) {
            return EnumActionResult.PASS;
        }
        ItemStack stack = player.getHeldItem(hand);

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
            player.sendMessage(metaTileEntity.isMuffled() ?
                    new TextComponentTranslation("gregtech.machine.muffle.off") :
                    new TextComponentTranslation("gregtech.machine.muffle.on"));
            metaTileEntity.toggleMuffled();
            GTUtility.doDamageItem(stack, cost, false);
            IToolStats.onOtherUse(stack, world, pos);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("behaviour.hammer"));
    }
}
