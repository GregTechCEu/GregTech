package gregtech.api.items.metaitem.stats;

import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;

import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.BlockLiquidWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemFluidContainer implements IItemContainerItemProvider, IItemBehaviour {

    private final boolean isBucket;

    public ItemFluidContainer(boolean isBucket) {
        this.isBucket = isBucket;
    }

    public ItemFluidContainer() {
        this(false);
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(itemStack);
        if (handler != null) {
            FluidStack drained = handler.drain(1000, false);
            if (drained == null || drained.amount != 1000) return ItemStack.EMPTY;
            handler.drain(1000, true);
            return handler.getContainer().copy();
        }
        return itemStack;
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                             EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!isBucket) return pass(stack);

        var result = rayTrace(world, player);
        if (result == null) return pass(stack);

        ItemStack cellStack = GTUtility.copy(1, stack);
        var cellHandler = FluidUtil.getFluidHandler(cellStack);
        if (cellHandler == null) return pass(stack);

        var cellFluid = cellHandler.drain(Integer.MAX_VALUE, false);
        var blockHandler = FluidUtil.getFluidHandler(world, result.getBlockPos(), result.sideHit);
        FluidStack soundFluid = cellFluid;
        boolean success, isFill;

        if (blockHandler == null) {
            if (cellFluid == null || !cellFluid.getFluid().canBePlacedInWorld())
                return pass(stack);

            blockHandler = createHandler(cellFluid, world, pos.offset(facing));
            success = GTTransferUtils.transferFluids(cellHandler, blockHandler) > 0;
            isFill = true;
        } else {
            soundFluid = blockHandler.drain(Integer.MAX_VALUE, false);
            success = GTTransferUtils.transferFluids(blockHandler, cellHandler) > 0;
            isFill = false;
        }

        if (success) {
            playSound(soundFluid, isFill, player);
            addToPlayerInventory(stack, cellHandler.getContainer(), player, hand);
            return success(stack);
        }

        return pass(stack);
    }

    // copied from Item.java
    @Nullable
    private static RayTraceResult rayTrace(World worldIn, EntityPlayer playerIn) {
        float f = playerIn.rotationPitch;
        float f1 = playerIn.rotationYaw;
        double d0 = playerIn.posX;
        double d1 = playerIn.posY + (double) playerIn.getEyeHeight();
        double d2 = playerIn.posZ;
        Vec3d vec3d = new Vec3d(d0, d1, d2);
        float f2 = MathHelper.cos(-f1 * 0.017453292F - (float) Math.PI);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - (float) Math.PI);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d3 = playerIn.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        Vec3d vec3d1 = vec3d.add((double) f6 * d3, (double) f5 * d3, (double) f7 * d3);
        return worldIn.rayTraceBlocks(vec3d, vec3d1, true, false, false);
    }

    @NotNull
    private IFluidHandler createHandler(FluidStack stack, World world, BlockPos pos) {
        var block = stack.getFluid().getBlock();
        if (block instanceof IFluidBlock fluidBlock) {
            return new FluidBlockWrapper(fluidBlock, world, pos);
        } else if (block instanceof BlockLiquid blockLiquid) {
            return new BlockLiquidWrapper(blockLiquid, world, pos);
        }
        throw new IllegalArgumentException("Block must be a liquid!");
    }

    private void addToPlayerInventory(ItemStack playerStack, ItemStack resultStack, EntityPlayer player,
                                      EnumHand hand) {
        if (playerStack.getCount() > resultStack.getCount()) {
            playerStack.shrink(resultStack.getCount());
            if (!player.inventory.addItemStackToInventory(resultStack) && !player.world.isRemote) {
                EntityItem dropItem = player.entityDropItem(resultStack, 0);
                if (dropItem != null) dropItem.setPickupDelay(0);
            }
        } else {
            player.setHeldItem(hand, resultStack);
        }
    }

    /**
     * Play the appropriate fluid interaction sound for the fluid. <br />
     * Must be called on server to work correctly
     **/
    private void playSound(FluidStack fluid, boolean fill, EntityPlayer player) {
        if (fluid == null || player.world.isRemote) return;
        SoundEvent soundEvent;
        if (fill) {
            soundEvent = fluid.getFluid().getFillSound(fluid);
        } else {
            soundEvent = fluid.getFluid().getEmptySound(fluid);
        }
        player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ,
                soundEvent, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }
}
