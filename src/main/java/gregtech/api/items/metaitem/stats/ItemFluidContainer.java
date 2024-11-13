package gregtech.api.items.metaitem.stats;

import gregtech.api.util.GTUtility;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
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
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
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
        IFluidHandlerItem handler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
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

        var thisHandler = getHandler(stack);
        if (thisHandler == null) return pass(stack);

        var fluid = thisHandler.drain(Integer.MAX_VALUE, false);
        var blockHandler = FluidUtil.getFluidHandler(world, result.getBlockPos(), result.sideHit);
        boolean success;
        if (blockHandler == null) {
            if (fluid == null || !fluid.getFluid().canBePlacedInWorld())
                return pass(stack);

            blockHandler = createHandler(fluid.getFluid().getBlock(), world, pos.offset(facing));
            success = transfer(thisHandler, blockHandler, player);
        } else {
            success = transfer(blockHandler, thisHandler, player);
        }

        return success ? success(stack) : pass(stack);
    }

    private boolean transfer(IFluidHandler source, IFluidHandler dest, EntityPlayer player) {
        var fluid = source.drain(Integer.MAX_VALUE, false);
        int filled = dest.fill(fluid, false);
        if (fluid == null || fluid.amount == 0 || filled == 0)
            return false;

        playSound(fluid, true, player);
        source.drain(filled, true);
        dest.fill(fluid, true);
        return true;
    }

    private static <T> ActionResult<T> pass(T t) {
        return ActionResult.newResult(EnumActionResult.PASS, t);
    }

    private static <T> ActionResult<T> success(T t) {
        return ActionResult.newResult(EnumActionResult.SUCCESS, t);
    }

    @Nullable
    private static IFluidHandlerItem getHandler(ItemStack stack) {
        if (stack.getCount() > 1) stack = GTUtility.copy(1, stack);
        return FluidUtil.getFluidHandler(stack);
    }

    @Nullable
    private RayTraceResult rayTrace(World worldIn, EntityPlayer playerIn) {
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
    private IFluidHandler createHandler(Block block, World world, BlockPos pos) {
        if (block instanceof IFluidBlock fluidBlock) {
            return new FluidBlockWrapper(fluidBlock, world, pos);
        } else if (block instanceof BlockLiquid blockLiquid) {
            return new BlockLiquidWrapper(blockLiquid, world, pos);
        }
        throw new IllegalArgumentException("Block must be a liquid!");
    }

    /**
     * Play the appropriate fluid interaction sound for the fluid. <br />
     * Must be called on server to work correctly
     **/
    private void playSound(FluidStack fluid, boolean fill, EntityPlayer player) {
        if (fluid == null) return;
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
