package gregtech.common.items.behaviors;

import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.handler.MultiblockPreviewRenderer;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.List;

public class RenderItemBehavior implements IItemBehaviour, ItemUIFactory {
    MultiblockControllerBase controller;
    private BlockPos initialRelativePos;
    private boolean isFollowing = true;
    private BlockPos fixedPosition;

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (player.isSneaking()) {
            if (GTUtility.getMetaTileEntity(world, pos) instanceof MetaTileEntity) {
                MetaTileEntity mte = GTUtility.getMetaTileEntity(world, pos);
                if (mte instanceof MultiblockControllerBase) {
                    controller = (MultiblockControllerBase) mte;
                    initialRelativePos = pos.subtract(player.getPosition());
                    isFollowing = true;
                }
            }
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void onUpdate(ItemStack itemStack, Entity entity) {
        if (entity.world.isRemote && entity instanceof EntityPlayer player) {
            if (controller == null || player.getHeldItemMainhand() != itemStack) return;
            BlockPos renderPos = isFollowing ?
                    player.getPosition().add(initialRelativePos) :
                    fixedPosition;

            // 每20tick且按住Shift时刷新预览
            if (player.isSneaking() && player.world.getWorldTime() % 20 == 0) {
                MultiblockPreviewRenderer.renderMultiBlockPreview(controller, renderPos, 60000);
                player.sendMessage(new TextComponentString("正在预览下一等级"));
            }else if(isFollowing){
                MultiblockPreviewRenderer.renderMultiBlockPreview(controller, renderPos,0, 60000);
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (player.world.isRemote && controller != null) {
            if (isFollowing) {
                fixedPosition = player.getPosition().add(initialRelativePos);
            }
            isFollowing = !isFollowing;
            player.sendMessage(new TextComponentString("预览跟随模式："+isFollowing));
            return pass(player.getHeldItem(hand));
        }
        return pass(player.getHeldItem(hand));
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("shift+右键绑定需要预览到多方块"));
        lines.add(I18n.format("右键切换多方块预览的跟随模式"));
        lines.add(I18n.format("长按shift切换多方块预览层级"));
    }
}
