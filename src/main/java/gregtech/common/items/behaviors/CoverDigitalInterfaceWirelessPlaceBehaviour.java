package gregtech.common.items.behaviors;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.items.behavior.CoverItemBehavior;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.handler.BlockPosHighlightRenderer;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityCentralMonitor;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class CoverDigitalInterfaceWirelessPlaceBehaviour extends CoverItemBehavior {

    public CoverDigitalInterfaceWirelessPlaceBehaviour(CoverDefinition coverDefinition) {
        super(coverDefinition);
    }

    public static BlockPos getRemotePos(ItemStack itemStack) {
        NBTTagCompound tag = itemStack.getTagCompound();
        if (tag != null) {
            return NBTUtil.getPosFromTag(tag);
        }
        return null;
    }

    public static int getRemoteDim(ItemStack itemStack) {
        NBTTagCompound tag = itemStack.getTagCompound();
        if (tag != null) {
            return tag.getInteger("dimension");
        }

        return 0;
    }

    @Override
    public void onUpdate(ItemStack itemStack, Entity entity) {
        if (entity.world.isRemote && entity instanceof EntityLivingBase) {
            ItemStack held = ((EntityLivingBase) entity).getHeldItemMainhand();
            if (held == itemStack) {
                BlockPos pos = getRemotePos(itemStack);
                int dim = getRemoteDim(itemStack);
                if (pos != null && entity.world.provider.getDimension() == dim) {
                    BlockPosHighlightRenderer.renderBlockBoxHighLight(pos, 1500);
                }
            }
        }
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
                                           float hitY, float hitZ, EnumHand hand) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity &&
                ((IGregTechTileEntity) tileEntity).getMetaTileEntity() instanceof MetaTileEntityCentralMonitor) {
            ItemStack itemStack = player.getHeldItem(hand);
            itemStack.setTagCompound(NBTUtil.createPosTag(pos));
            NBTTagCompound tag = itemStack.getTagCompound();
            if (tag != null && !tag.hasKey("dimension")) {
                tag.setInteger("dimension", world.provider.getDimension());
            }
            return EnumActionResult.SUCCESS;
        }
        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (player.isSneaking()) {
            player.getHeldItem(hand).setTagCompound(null);
            return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }
        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        BlockPos pos = getRemotePos(itemStack);
        String binding = pos == null ? "---" : String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ());
        lines.add(I18n.format("metaitem.cover.digital.wireless.tooltip.1"));
        lines.add(I18n.format("metaitem.cover.digital.wireless.tooltip.2"));
        lines.add(I18n.format("metaitem.cover.digital.wireless.tooltip.3"));
        lines.add(I18n.format("metaitem.cover.digital.wireless.tooltip.4", binding));
    }
}
