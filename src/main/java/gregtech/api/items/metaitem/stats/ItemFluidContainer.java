package gregtech.api.items.metaitem.stats;

import gregtech.api.util.GTUtility;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.BlockLiquidWrapper;
import net.minecraftforge.fluids.capability.wrappers.BlockWrapper;
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
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!isBucket) return pass(stack);

        // can the player modify the clicked block
        ItemStack cellStack = GTUtility.copy(1, stack);

        var cellHandler = FluidUtil.getFluidHandler(cellStack);
        if (cellHandler == null) return pass(stack);

        var cellFluid = cellHandler.drain(Fluid.BUCKET_VOLUME, false);

        var result = rayTrace(world, player, false);
        if (result == null || result.typeOfHit != RayTraceResult.Type.BLOCK) {
            return pass(stack);
        }

        var blockHandler = FluidUtil.getFluidHandler(world, result.getBlockPos().offset(result.sideHit),
                result.sideHit);
        int freeSpace = cellHandler.getTankProperties()[0].getCapacity() - (cellFluid == null ? 0 : cellFluid.amount);
        boolean pickup = blockHandler != null && blockHandler.drain(Fluid.BUCKET_VOLUME, false) != null &&
                freeSpace > 0;
        if (pickup) {
            result = rayTrace(world, player, true);
            if (result == null || result.typeOfHit != RayTraceResult.Type.BLOCK) {
                return pass(stack);
            }
        }

        var pos = result.getBlockPos();

        if (!world.isBlockModifiable(player, pos)) {
            return fail(stack);
        }

        // can player edit
        if (!player.canPlayerEdit(pos, result.sideHit, cellStack)) {
            return fail(stack);
        }

        FluidStack soundFluid;
        if (blockHandler != null && cellFluid == null) {
            soundFluid = blockHandler.drain(Fluid.BUCKET_VOLUME, false);
            if (soundFluid == null) return pass(stack);
            soundFluid = soundFluid.copy();
        } else if (cellFluid != null) {
            soundFluid = cellFluid.copy();
        } else {
            return pass(stack);
        }

        // the defualt assumption is placing fluid, then picking up fluid
        if (!pickup && tryPlace(cellHandler, world, pos.offset(result.sideHit), result.sideHit, player)) {
            playSound(soundFluid, true, player);
            addToPlayerInventory(stack, cellHandler.getContainer(), player, hand);
            return success(stack);

        } else if (fillCell(cellStack, world, pos, result.sideHit, player)) {
            playSound(soundFluid, false, player);
            addToPlayerInventory(stack, cellHandler.getContainer(), player, hand);
            return success(stack);
        }

        return pass(stack);
    }

    private static boolean tryPlace(IFluidHandlerItem cellHandler, World world, BlockPos pos, EnumFacing side,
                                    EntityPlayer player) {
        var cellFluid = cellHandler.drain(Fluid.BUCKET_VOLUME, false);
        if (cellFluid == null || !cellFluid.getFluid().canBePlacedInWorld())
            return false;

        IFluidHandler blockHandler = getOrCreate(cellFluid, world, pos, side);

        // check that we can place the fluid at the destination
        IBlockState destBlockState = world.getBlockState(pos);
        Material destMaterial = destBlockState.getMaterial();
        boolean isDestNonSolid = !destMaterial.isSolid();
        boolean isDestReplaceable = destBlockState.getBlock().isReplaceable(world, pos);

        if (!world.isAirBlock(pos) && !isDestNonSolid && !isDestReplaceable) {
            // Non-air, solid, unreplacable block. We can't put fluid here.
            return false;
        }

        // check vaporize
        if (world.provider.doesWaterVaporize() && cellFluid.getFluid().doesVaporize(cellFluid)) {
            cellHandler.drain(Fluid.BUCKET_VOLUME, true);
            cellFluid.getFluid().vaporize(player, world, pos, cellFluid);
            return true;
        }

        // fill block
        int filled = blockHandler.fill(cellFluid, false);

        if (filled != Fluid.BUCKET_VOLUME) return false;

        boolean consume = !player.isSpectator() && !player.isCreative();
        blockHandler.fill(cellHandler.drain(Fluid.BUCKET_VOLUME, consume), true);
        return true;
    }

    private static boolean fillCell(ItemStack cellStack, World world, BlockPos pos, EnumFacing side,
                                    EntityPlayer player) {
        IFluidHandler blockHandler = FluidUtil.getFluidHandler(world, pos, side);
        if (blockHandler == null) return false;

        IFluidHandlerItem cellHandler = FluidUtil.getFluidHandler(cellStack);
        if (cellHandler == null) return false;

        FluidStack stack = blockHandler.drain(Fluid.BUCKET_VOLUME, false);
        int filled = cellHandler.fill(stack, false);

        if (filled != Fluid.BUCKET_VOLUME) return false;

        boolean consume = !player.isSpectator() && !player.isCreative();
        cellHandler.fill(blockHandler.drain(Fluid.BUCKET_VOLUME, true), consume);
        return true;
    }

    // copied and adapted from Item.java
    @Nullable
    private static RayTraceResult rayTrace(World worldIn, EntityPlayer player, boolean hitFluids) {
        Vec3d lookPos = player.getPositionVector()
                .add(0, player.getEyeHeight(), 0);

        Vec3d lookOffset = player.getLookVec()
                .scale(player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue());

        return worldIn.rayTraceBlocks(lookPos, lookPos.add(lookOffset),
                hitFluids, !hitFluids, false);
    }

    @NotNull
    private static IFluidHandler createHandler(FluidStack stack, World world, BlockPos pos) {
        Block block = stack.getFluid().getBlock();
        if (block instanceof IFluidBlock) {
            return new FluidBlockWrapper((IFluidBlock) block, world, pos);
        } else if (block instanceof BlockLiquid) {
            return new BlockLiquidWrapper((BlockLiquid) block, world, pos);
        } else {
            return new BlockWrapper(block, world, pos);
        }
    }

    private static IFluidHandler getOrCreate(FluidStack stack, World world, BlockPos pos, EnumFacing side) {
        IFluidHandler handler = FluidUtil.getFluidHandler(world, pos, side);
        if (handler != null) return handler;
        return createHandler(stack, world, pos);
    }

    private static void addToPlayerInventory(ItemStack playerStack, ItemStack resultStack, EntityPlayer player,
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
    private static void playSound(FluidStack fluid, boolean fill, EntityPlayer player) {
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
