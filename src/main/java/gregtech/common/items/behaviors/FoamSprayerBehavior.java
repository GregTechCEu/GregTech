package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import java.util.*;

public class FoamSprayerBehavior implements IItemCapabilityProvider, IItemDurabilityManager, IItemBehaviour {

    private static final int FLUID_PER_BLOCK = 100;

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemStack = player.getHeldItem(hand);
        IFluidHandlerItem fluidHandlerItem = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        FluidStack fluidStack = fluidHandlerItem.drain(Integer.MAX_VALUE, false);
        if (fluidStack != null && fluidStack.amount >= FLUID_PER_BLOCK) {
            BlockPos offsetPos = pos.offset(facing);
            IBlockState offsetState = world.getBlockState(offsetPos);

            if (offsetState.getBlock().isReplaceable(world, offsetPos)) {
                int blocksToFoam = fluidStack.amount / FLUID_PER_BLOCK;
                int blocksFoamed = foamReplacableBlocks(world, offsetPos, blocksToFoam);
                if (!player.capabilities.isCreativeMode) {
                    fluidHandlerItem.drain(FLUID_PER_BLOCK * blocksFoamed, true);
                }
                return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);
            }
        }
        return ActionResult.newResult(EnumActionResult.PASS, itemStack);
    }

    @Override
    public boolean showsDurabilityBar(ItemStack itemStack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        IFluidHandlerItem fluidHandlerItem = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandlerItem == null) return 0.0;

        IFluidTankProperties fluidTankProperties = fluidHandlerItem.getTankProperties()[0];
        FluidStack fluidStack = fluidTankProperties.getContents();
        return fluidStack == null ? 1.0 : (1.0 - fluidStack.amount / (fluidTankProperties.getCapacity() * 1.0));
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack itemStack) {
        return MathHelper.hsvToRGB(0.33f, 1.0f, 1.0f);
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        return new FluidHandlerItemStack(itemStack, 10000) {
            @Override
            public boolean canFillFluidType(FluidStack fluid) {
                return fluid != null && fluid.isFluidEqual(Materials.ConstructionFoam.getFluid(1));
            }
        };
    }

    private static int foamReplacableBlocks(World world, BlockPos pos, int maxBlocksToFoam) {
        List<BlockPos> replacableBlocks = gatherReplacableBlocks(world, pos, 10);
        replacableBlocks = replacableBlocks.subList(0, Math.min(replacableBlocks.size(), maxBlocksToFoam));

        for (BlockPos blockPos : replacableBlocks) {
            //foaming air blocks doesn't cause updates of other blocks, so just proceed
            world.setBlockState(blockPos, MetaBlocks.FOAM.getDefaultState(), 2);
        }

        //perform block physics updates
        for (BlockPos blockPos : replacableBlocks) {
            IBlockState blockState = world.getBlockState(blockPos);
            world.notifyNeighborsRespectDebug(pos, blockState.getBlock(), true);
        }
        return replacableBlocks.size();
    }

    private static List<BlockPos> gatherReplacableBlocks(World worldIn, BlockPos centerPos, int maxRadiusSq) {
        HashSet<BlockPos> observedSet = new HashSet<>();
        ArrayList<BlockPos> resultAirBlocks = new ArrayList<>();
        observedSet.add(centerPos);
        resultAirBlocks.add(centerPos);
        Stack<EnumFacing> moveStack = new Stack<>();
        MutableBlockPos currentPos = new MutableBlockPos(centerPos);
        main:
        while (true) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                currentPos.move(facing);
                IBlockState blockStateHere = worldIn.getBlockState(currentPos);
                //if there is node, and it can connect with previous node, add it to list, and set previous node as current
                if (blockStateHere.getBlock().isReplaceable(worldIn, currentPos) &&
                        currentPos.distanceSq(centerPos) <= maxRadiusSq && !observedSet.contains(currentPos)) {
                    BlockPos immutablePos = currentPos.toImmutable();
                    observedSet.add(immutablePos);
                    resultAirBlocks.add(immutablePos);
                    moveStack.push(facing.getOpposite());
                    continue main;
                } else currentPos.move(facing.getOpposite());
            }
            if (!moveStack.isEmpty()) {
                currentPos.move(moveStack.pop());
            } else break;
        }
        resultAirBlocks.sort(Comparator.comparing(it -> it.distanceSq(centerPos)));
        return resultAirBlocks;
    }
}
