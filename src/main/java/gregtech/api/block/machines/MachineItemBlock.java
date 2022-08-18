package gregtech.api.block.machines;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.util.GTUtility;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class MachineItemBlock extends ItemBlock {

    public MachineItemBlock(BlockMachine block) {
        super(block);
        setHasSubtypes(true);
    }

    @Nonnull
    @Override
    public String getTranslationKey(@Nonnull ItemStack stack) {
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(stack);
        return metaTileEntity == null ? "unnamed" : metaTileEntity.getMetaName();
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(stack);
        //prevent rendering glitch before meta tile entity sync to client, but after block placement
        //set opaque property on the placing on block, instead during set of meta tile entity
        boolean superVal = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ,
                newState.withProperty(BlockMachine.OPAQUE, metaTileEntity != null && metaTileEntity.isOpaqueCube()));
        if (superVal && !world.isRemote) {
            BlockPos possiblePipe = pos.offset(side.getOpposite());
            Block block = world.getBlockState(possiblePipe).getBlock();
            if (block instanceof BlockPipe) {
                IPipeTile pipeTile = ((BlockPipe<?, ?, ?>) block).getPipeTileEntity(world, possiblePipe);
                if (pipeTile != null && ((BlockPipe<?, ?, ?>) block).canPipeConnectToBlock(pipeTile, side.getOpposite(), world.getTileEntity(pos))) {
                    pipeTile.setConnection(side, true, false);
                }
            }
        }
        return superVal;
    }

    @Nullable
    @Override
    public String getCreatorModId(@Nonnull ItemStack itemStack) {
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(itemStack);
        if (metaTileEntity == null) {
            return GTValues.MODID;
        }
        ResourceLocation metaTileEntityId = metaTileEntity.metaTileEntityId;
        return metaTileEntityId.getNamespace();
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable NBTTagCompound nbt) {
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(stack);
        return metaTileEntity == null ? null : metaTileEntity.initItemStackCapabilities(stack);
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) != null;
    }

    @Override
    public @Nonnull ItemStack getContainerItem(@Nonnull ItemStack itemStack) {
        if (!hasContainerItem(itemStack)) {
            return ItemStack.EMPTY;
        }
        if (itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            IFluidHandlerItem handler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
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
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(stack);
        if (metaTileEntity == null) return;

        //item specific tooltip like: gregtech.machine.lathe.lv.tooltip
        String tooltipLocale = metaTileEntity.getMetaName() + ".tooltip";
        if (I18n.hasKey(tooltipLocale)) {
            String[] lines = I18n.format(tooltipLocale).split("/n");
            tooltip.addAll(Arrays.asList(lines));
        }

        //tier less tooltip for a electric machine like: gregtech.machine.lathe.tooltip
        if (metaTileEntity instanceof ITieredMetaTileEntity) {
            String tierlessTooltipLocale = ((ITieredMetaTileEntity) metaTileEntity).getTierlessTooltipKey();
            //only add tierless tooltip if it's key is not equal to normal tooltip key (i.e if machine name has dot in it's name)
            //case when it's not true would be any machine extending from TieredMetaTileEntity but having only one tier
            if (!tooltipLocale.equals(tierlessTooltipLocale) && I18n.hasKey(tierlessTooltipLocale)) {
                String[] lines = I18n.format(tierlessTooltipLocale).split("/n");
                tooltip.addAll(Arrays.asList(lines));
            }
        }
        metaTileEntity.addInformation(stack, worldIn, tooltip, flagIn.isAdvanced());

        if (flagIn.isAdvanced()) {
            tooltip.add(String.format("MetaTileEntity Id: %s", metaTileEntity.metaTileEntityId.toString()));
        }
    }
}
