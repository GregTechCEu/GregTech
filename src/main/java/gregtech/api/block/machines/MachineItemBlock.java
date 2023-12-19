package gregtech.api.block.machines;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MachineItemBlock extends ItemBlock {

    private static final Set<CreativeTabs> ADDITIONAL_CREATIVE_TABS = new ObjectArraySet<>();

    /**
     * Adds another creative tab for the machine item. Additional tabs added by this method are checked along with
     * default tabs ({@link GregTechAPI#MACHINE} and {@link CreativeTabs#SEARCH}) during
     * {@link net.minecraft.item.Item#getSubItems(CreativeTabs, NonNullList) Item#getSubItems()} operation.<br>
     * Note that, for machines to be properly registered on the creative tab, a matching implementation of
     * {@link MetaTileEntity#isInCreativeTab(CreativeTabs)} should be provided as well.
     *
     * @param creativeTab Creative tab to be checked during
     *                    {@link net.minecraft.item.Item#getSubItems(CreativeTabs, NonNullList)}
     * @throws NullPointerException     If {@code creativeTab == null}
     * @throws IllegalArgumentException If
     *                                  {@code creativeTab == GregTechAPI.TAB_GREGTECH_MACHINES || creativeTab == CreativeTabs.SEARCH}
     * @see MetaTileEntity#isInCreativeTab(CreativeTabs)
     */
    public static void addCreativeTab(CreativeTabs creativeTab) {
        Preconditions.checkNotNull(creativeTab, "creativeTab");
        if (creativeTab == GregTechAPI.TAB_GREGTECH_MACHINES) {
            throw new IllegalArgumentException("Adding " + GregTechAPI.TAB_GREGTECH_MACHINES.tabLabel +
                    " as additional creative tab is redundant.");
        } else if (creativeTab == CreativeTabs.SEARCH) {
            throw new IllegalArgumentException(
                    "Adding " + CreativeTabs.SEARCH.tabLabel + " as additional creative tab is redundant.");
        }
        ADDITIONAL_CREATIVE_TABS.add(creativeTab);
    }

    public MachineItemBlock(BlockMachine block) {
        super(block);
        setHasSubtypes(true);
    }

    @NotNull
    @Override
    public String getTranslationKey(@NotNull ItemStack stack) {
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(stack);
        return metaTileEntity == null ? "unnamed" : metaTileEntity.getMetaName();
    }

    @Override
    public boolean placeBlockAt(@NotNull ItemStack stack, @NotNull EntityPlayer player, @NotNull World world,
                                @NotNull BlockPos pos, @NotNull EnumFacing side, float hitX, float hitY, float hitZ,
                                IBlockState newState) {
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(stack);
        // prevent rendering glitch before meta tile entity sync to client, but after block placement
        // set opaque property on the placing on block, instead during set of meta tile entity
        boolean superVal = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ,
                newState.withProperty(BlockMachine.OPAQUE, metaTileEntity != null && metaTileEntity.isOpaqueCube()));
        if (superVal && !world.isRemote) {
            BlockPos possiblePipe = pos.offset(side.getOpposite());
            Block block = world.getBlockState(possiblePipe).getBlock();
            if (block instanceof BlockPipe) {
                IPipeTile pipeTile = ((BlockPipe<?, ?, ?>) block).getPipeTileEntity(world, possiblePipe);
                if (pipeTile != null && ((BlockPipe<?, ?, ?>) block).canPipeConnectToBlock(pipeTile, side.getOpposite(),
                        world.getTileEntity(pos))) {
                    pipeTile.setConnection(side, true, false);
                }
            }
        }
        return superVal;
    }

    @Nullable
    @Override
    public String getCreatorModId(@NotNull ItemStack itemStack) {
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(itemStack);
        if (metaTileEntity == null) {
            return GTValues.MODID;
        }
        ResourceLocation metaTileEntityId = metaTileEntity.metaTileEntityId;
        return metaTileEntityId.getNamespace();
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable NBTTagCompound nbt) {
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(stack);
        return metaTileEntity == null ? null : metaTileEntity.initItemStackCapabilities(stack);
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) != null;
    }

    @NotNull
    @Override
    public ItemStack getContainerItem(@NotNull ItemStack itemStack) {
        if (!hasContainerItem(itemStack)) {
            return ItemStack.EMPTY;
        }
        if (itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            IFluidHandlerItem handler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY,
                    null);
            if (handler != null) {
                FluidStack drained = handler.drain(1000, true);
                if (drained == null || drained.amount != 1000) {
                    return ItemStack.EMPTY;
                }
                return handler.getContainer().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn) {
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(stack);
        if (metaTileEntity == null) return;

        // item specific tooltip like: gregtech.machine.lathe.lv.tooltip
        String tooltipLocale = metaTileEntity.getMetaName() + ".tooltip";
        if (I18n.hasKey(tooltipLocale)) {
            Collections.addAll(tooltip, LocalizationUtils.formatLines(tooltipLocale));
        }

        // tier less tooltip for a electric machine like: gregtech.machine.lathe.tooltip
        if (metaTileEntity instanceof ITieredMetaTileEntity) {
            String tierlessTooltipLocale = ((ITieredMetaTileEntity) metaTileEntity).getTierlessTooltipKey();
            // only add tierless tooltip if it's key is not equal to normal tooltip key (i.e if machine name has dot in
            // it's name)
            // case when it's not true would be any machine extending from TieredMetaTileEntity but having only one tier
            if (!tooltipLocale.equals(tierlessTooltipLocale) && I18n.hasKey(tierlessTooltipLocale)) {
                Collections.addAll(tooltip, LocalizationUtils.formatLines(tierlessTooltipLocale));
            }
        }

        // additional tooltips that the MTE provides
        metaTileEntity.addInformation(stack, worldIn, tooltip, flagIn.isAdvanced());

        // tool usages tooltips
        if (metaTileEntity.showToolUsages()) {
            if (TooltipHelper.isShiftDown()) {
                metaTileEntity.addToolUsages(stack, worldIn, tooltip, flagIn.isAdvanced());
            } else {
                tooltip.add(I18n.format("gregtech.tool_action.show_tooltips"));
            }
        }

        if (ConfigHolder.misc.debug) {
            tooltip.add(String.format("MetaTileEntity Id: %s", metaTileEntity.metaTileEntityId.toString()));
        }
    }

    @Override
    public CreativeTabs[] getCreativeTabs() {
        CreativeTabs[] tabs = ADDITIONAL_CREATIVE_TABS.toArray(new CreativeTabs[ADDITIONAL_CREATIVE_TABS.size() + 1]);
        tabs[tabs.length - 1] = getCreativeTab();
        return tabs;
    }

    @Override
    public int getItemStackLimit(@NotNull ItemStack stack) {
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(stack);
        return metaTileEntity != null ? metaTileEntity.getItemStackLimit(stack) : super.getItemStackLimit(stack);
    }
}
