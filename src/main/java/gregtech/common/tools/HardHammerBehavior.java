package gregtech.common.tools;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemComponent;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
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
        if (world.isAirBlock(pos)) {
            return EnumActionResult.PASS;
        }

        ItemStack stack = player.getHeldItem(hand);

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof MetaTileEntityHolder) {
            MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
            if (metaTileEntity != null) {
                if(!world.isRemote) { // If it gets to this point, it will run on the client thread and do what it's supposed to, but we don't want this to pull up the GUI.
                    return EnumActionResult.SUCCESS;
                }
                metaTileEntity.toggleMuffled();
                if(metaTileEntity.isMuffled())
                    player.sendMessage(new TextComponentTranslation("gregtech.machine.muffle.on"));
                else
                    player.sendMessage(new TextComponentTranslation("gregtech.machine.muffle.off"));
                GTUtility.doDamageItem(stack, cost, false);
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("behaviour.hammer"));
    }
}
