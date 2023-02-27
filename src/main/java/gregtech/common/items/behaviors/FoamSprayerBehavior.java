package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.items.metaitem.stats.ISubItemHandler;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GradientUtil;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;

public class FoamSprayerBehavior implements IItemCapabilityProvider, IItemDurabilityManager, IItemBehaviour, ISubItemHandler {

    private static final int FLUID_PER_BLOCK = 100;

    private final Pair<Color, Color> durabilityBarColors;

    public FoamSprayerBehavior() {
        this.durabilityBarColors = GradientUtil.getGradient(Materials.ConstructionFoam.getMaterialRGB(), 10);
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemStack = player.getHeldItem(hand);
        IFluidHandlerItem fluidHandlerItem = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        FluidStack fluidStack = fluidHandlerItem.drain(Integer.MAX_VALUE, false);
        if (fluidStack != null && fluidStack.amount >= FLUID_PER_BLOCK) {
            BlockPos offsetPos = pos.offset(facing);
            IBlockState initialBlockState = world.getBlockState(pos);
            IBlockState offsetState = world.getBlockState(offsetPos);

            if (initialBlockState.getBlock() instanceof BlockFrame) {
                int blocksToFoam = fluidStack.amount / FLUID_PER_BLOCK;
                int blocksFoamed = foamAllFrameBlocks(world, pos, blocksToFoam, player.isSneaking());
                if (!player.capabilities.isCreativeMode) {
                    fluidHandlerItem.drain(FLUID_PER_BLOCK * blocksFoamed, true);
                }
                return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);

            } else if (offsetState.getBlock().isReplaceable(world, offsetPos)) {
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
    public double getDurabilityForDisplay(ItemStack itemStack) {
        IFluidHandlerItem fluidHandlerItem = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandlerItem == null) return 0;
        IFluidTankProperties fluidTankProperties = fluidHandlerItem.getTankProperties()[0];
        FluidStack fluidStack = fluidTankProperties.getContents();
        return fluidStack == null ? 0 : (double) fluidStack.amount / (double) fluidTankProperties.getCapacity();
    }

    @Nullable
    @Override
    public Pair<Color, Color> getDurabilityColorsForDisplay(ItemStack itemStack) {
        return durabilityBarColors;
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

    private static int foamAllFrameBlocks(World world, BlockPos pos, int maxBlocksToFoam, boolean isSneaking) {
        List<BlockPos> frameBlocks = gatherFrameBlocks(world, pos, 1024);
        frameBlocks = frameBlocks.subList(0, Math.min(frameBlocks.size(), maxBlocksToFoam));

        //replace blocks without updating physics
        for (BlockPos framePos : frameBlocks) {
            IBlockState blockState = world.getBlockState(framePos);
            boolean isNormalFrame = blockState.getBlock().getMaterial(blockState) == Material.WOOD || isSneaking;
            if (isNormalFrame) {
                blockState.getBlock().dropBlockAsItem(world, framePos, blockState, 0);
            }
            IBlockState foamToPlace = isNormalFrame ? MetaBlocks.FOAM.getDefaultState() : MetaBlocks.REINFORCED_FOAM.getDefaultState();
            world.setBlockState(framePos, foamToPlace, 2);
        }

        //perform block physics updates
        for (BlockPos blockPos : frameBlocks) {
            IBlockState blockState = world.getBlockState(blockPos);
            world.notifyNeighborsRespectDebug(blockPos, blockState.getBlock(), true);
        }
        return frameBlocks.size();
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
        Deque<EnumFacing> moveStack = new ArrayDeque<>();
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

    private static List<BlockPos> gatherFrameBlocks(World worldIn, BlockPos centerPos, int maxRadiusSq) {
        HashSet<BlockPos> observedSet = new HashSet<>();
        ArrayList<BlockPos> resultFrameBlocks = new ArrayList<>();
        observedSet.add(centerPos);
        resultFrameBlocks.add(centerPos);
        IBlockState frameState = null;
        Deque<EnumFacing> moveStack = new ArrayDeque<>();
        MutableBlockPos currentPos = new MutableBlockPos(centerPos);
        main:
        while (true) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                currentPos.move(facing);
                IBlockState blockStateHere = worldIn.getBlockState(currentPos);
                //if there is node, and it can connect with previous node, add it to list, and set previous node as current
                if (blockStateHere.getBlock() instanceof BlockFrame &&
                        currentPos.distanceSq(centerPos) <= maxRadiusSq &&
                        (frameState == null || frameState == blockStateHere) && !observedSet.contains(currentPos)) {
                    BlockPos immutablePos = currentPos.toImmutable();
                    observedSet.add(immutablePos);
                    resultFrameBlocks.add(immutablePos);
                    moveStack.push(facing.getOpposite());
                    frameState = blockStateHere;
                    continue main;
                } else currentPos.move(facing.getOpposite());
            }
            if (!moveStack.isEmpty()) {
                currentPos.move(moveStack.pop());
            } else break;
        }
        resultFrameBlocks.sort(Comparator.comparing(it -> it.distanceSq(centerPos)));
        return resultFrameBlocks;
    }

    @Override
    public String getItemSubType(ItemStack itemStack) {
        return "";
    }

    @Override
    public void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        ItemStack copy = itemStack.copy();
        IFluidHandlerItem fluidHandlerItem = copy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandlerItem != null) {
            fluidHandlerItem.fill(Materials.ConstructionFoam.getFluid(10000), true);
            subItems.add(copy);
        } else {
            subItems.add(itemStack);
        }
    }
}
