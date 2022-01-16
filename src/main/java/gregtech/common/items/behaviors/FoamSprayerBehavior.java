package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.sound.GTSounds;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.tools.ToolUtility;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class FoamSprayerBehavior implements IItemCapabilityProvider, IItemDurabilityManager, IItemBehaviour {

    private static final int FLUID_PER_BLOCK = 50;
    private SprayerMode sprayerMode;

    public FoamSprayerBehavior() {
        this.sprayerMode = SprayerMode.SINGLE_BLOCK;
    }

    @Override
    public ActionResult<ItemStack> onItemUse(@Nonnull EntityPlayer player, @Nonnull World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemStack = player.getHeldItem(hand);

        if (!world.isRemote && player.isSneaking()) {
            if (sprayerMode == SprayerMode.SINGLE_BLOCK)
                sprayerMode = SprayerMode.LINE;
            else if (sprayerMode == SprayerMode.LINE)
                sprayerMode = SprayerMode.PANEL;
            else if (sprayerMode == SprayerMode.PANEL)
                sprayerMode = SprayerMode.SINGLE_BLOCK;

            player.sendMessage(new TextComponentTranslation(sprayerMode.getTranslationKey()));
            return ActionResult.newResult(EnumActionResult.PASS, itemStack);
        }

        IFluidHandlerItem fluidHandlerItem = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandlerItem == null) return ActionResult.newResult(EnumActionResult.PASS, itemStack);

        FluidStack fluidStack = fluidHandlerItem.drain(Integer.MAX_VALUE, false);
        if (fluidStack != null && fluidStack.amount >= FLUID_PER_BLOCK) {
            BlockPos offsetPos = pos.offset(facing);
            IBlockState offsetState = world.getBlockState(offsetPos);
            EnumFacing sideHit = ToolUtility.getSideHit(world, pos, player);

            ItemStack heldItem = player.getHeldItemOffhand();

            EnumDyeColor color = getSprayColor(heldItem);
            boolean placeDry = getPlaceDry(heldItem);

            // foam all connected frames
            if (world.getBlockState(pos).getBlock() instanceof BlockFrame) {
                int blocksFoamed = foamFrameBlockPanel(world, pos, facing, sideHit, 5, 5, color, placeDry);
                if (!player.capabilities.isCreativeMode)
                    fluidHandlerItem.drain(FLUID_PER_BLOCK * blocksFoamed, true);

                world.playSound(null, player.posX, player.posY, player.posZ, GTSounds.SPRAY_CAN_TOOL, SoundCategory.PLAYERS, 100, 0);
                return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);

            // foam regular blocks
            } else if (offsetState.getBlock().isReplaceable(world, offsetPos)) {
                int blocksFoamed = 0;
                if (sprayerMode == SprayerMode.SINGLE_BLOCK) {
                    blocksFoamed = foamBlock(world, offsetPos, color, placeDry, false);
                } else if (sprayerMode == SprayerMode.LINE) {
                    blocksFoamed = foamReplaceableBlockPanel(world, offsetPos, facing, sideHit, 1, 1, 4, color, placeDry);
                } else if (sprayerMode == SprayerMode.PANEL) {
                    blocksFoamed = foamReplaceableBlockPanel(world, offsetPos, facing, sideHit, 3, 3, 1, color, placeDry);
                }

                if (!player.capabilities.isCreativeMode)
                    fluidHandlerItem.drain(FLUID_PER_BLOCK * blocksFoamed, true);

                world.playSound(null, player.posX, player.posY, player.posZ, GTSounds.SPRAY_CAN_TOOL, SoundCategory.PLAYERS, 100, 0);
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
    public double getDurabilityForDisplay(@Nonnull ItemStack itemStack) {
        IFluidHandlerItem fluidHandlerItem = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandlerItem == null)
            return 0.0;

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

    private static int foamBlock(@Nonnull World world, BlockPos pos, EnumDyeColor color, boolean placeDry, boolean checkReplaceable) {
        if (checkReplaceable && !world.getBlockState(pos).getBlock().isReplaceable(world, pos))
            return 0;

        //foaming air blocks doesn't cause updates of other blocks, so just proceed
        world.setBlockState(pos, getFoamState(placeDry).withProperty(BlockColored.COLOR, color), 2);

        //perform block physics updates
        IBlockState blockState = world.getBlockState(pos);
        world.notifyNeighborsRespectDebug(pos, blockState.getBlock(), true);

        return 1;
    }

    private static int foamReplaceableBlockPanel(@Nonnull World world, BlockPos pos, EnumFacing facing, EnumFacing sideHit, int rows, int cols, int length, EnumDyeColor color, boolean placeDry) {
        int xSizeExtend = rows / 2;
        int ySizeExtend = cols / 2;

        int foamed = 0;
        for (int i = 0; i < length; i++) {
            for (int x = -xSizeExtend; x <= xSizeExtend; x++) {
                for (int y = -ySizeExtend; y <= ySizeExtend; y++) {
                    BlockPos offsetPos = rotate(pos, x, y, sideHit, facing).offset(sideHit, i);
                    foamed += foamBlock(world, offsetPos, color, placeDry, true);
                }
            }
        }
        return foamed;
    }

    @SuppressWarnings("SameParameterValue")
    private static int foamFrameBlockPanel(@Nonnull World world, BlockPos pos, EnumFacing facing, EnumFacing sideHit, int rows, int cols, EnumDyeColor color, boolean placeDry) {
        int xSizeExtend = rows / 2;
        int ySizeExtend = cols / 2;
        int foamed = 0;
        for (int x = -xSizeExtend; x <= xSizeExtend; x++) {
            for (int y = -ySizeExtend; y <= ySizeExtend; y++) {
                BlockPos offsetPos = rotate(pos, x, y, sideHit, facing);
                if (world.getBlockState(offsetPos).getBlock() instanceof BlockFrame) {
                    foamed += foamBlock(world, offsetPos, color, placeDry, false);
                }
            }
        }
        return foamed;
    }

    private static BlockPos rotate(BlockPos origin, int x, int y, EnumFacing sideHit, EnumFacing horizontalFacing) {
        if (sideHit == null) {
            return BlockPos.ORIGIN;
        }
        switch (sideHit.getAxis()) {
            case X:
                return origin.add(0, y, x);
            case Z:
                return origin.add(x, y, 0);
            case Y:
                return rotateVertical(origin, x, y, horizontalFacing);
            default:
                return BlockPos.ORIGIN;
        }
    }

    private static BlockPos rotateVertical(BlockPos origin, int x, int y, @Nonnull EnumFacing horizontalFacing) {
        switch (horizontalFacing.getAxis()) {
            case X:
                return origin.add(y, 0, x);
            case Z:
                return origin.add(x, 0, y);
            default:
                return BlockPos.ORIGIN;
        }
    }

    private enum SprayerMode {
        SINGLE_BLOCK("behavior.foam_spray.mode.single_block"),
        LINE("behavior.foam_spray.mode.line"),
        PANEL("behavior.foam_spray.mode.panel");

        private final String translationKey;

        SprayerMode(String translationKey) {
            this.translationKey = translationKey;
        }

        @Nonnull
        public String getTranslationKey() {
            return translationKey;
        }
    }

    private static List<IItemBehaviour> getItemBehaviors(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof MetaItem)
            return ((MetaItem<?>) stack.getItem()).getItem(stack).getBehaviours();
        return Collections.emptyList();
    }

    private static EnumDyeColor getSprayColor(@Nonnull ItemStack stack) {
        for (IItemBehaviour behaviour : getItemBehaviors(stack)) {
            if (behaviour instanceof ColorSprayBehaviour)
                return ((ColorSprayBehaviour) behaviour).getColor();
        }
        return EnumDyeColor.WHITE;
    }

    //todo
    private static boolean getPlaceDry(@Nonnull ItemStack stack) {
        for (IItemBehaviour behaviour : getItemBehaviors(stack)) {
//            if (behaviour instanceof ColorSprayBehaviour)
//                return true;
        }
        return false;
    }

    @Nonnull
    private static IBlockState getFoamState(boolean isDry) {
        return isDry ? MetaBlocks.CONSTRUCTION_FOAM.getDefaultState() : MetaBlocks.CONSTRUCTION_FOAM_WET.getDefaultState();
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nonnull List<String> lines) {
        lines.add(I18n.format(sprayerMode.getTranslationKey()));
    }
}
