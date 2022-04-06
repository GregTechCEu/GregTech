package gregtech.api.metatileentity;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.tool.ISoftHammerItem;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.toolitem.IToolStats;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.tools.DamageValues;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public abstract class MetaTileEntityPipelineEndpoint extends MetaTileEntity implements IDataInfoProvider {

    protected MetaTileEntityPipelineEndpoint source;
    protected MetaTileEntityPipelineEndpoint target;
    protected BlockPos targetPos;

    public MetaTileEntityPipelineEndpoint(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    protected int getDistanceToPosition(@Nonnull BlockPos pos) {
        return (int) Math.ceil(getPos().getDistance(pos.getX(), pos.getY(), pos.getZ()));
    }

    protected void scanPipes() {
        if (source != null && source.isValid() && source.target == this) return;
        targetPos = getPos();
        target = this;
        source = null;

        // Start scanning from the output's side
        Block block = getWorld().getBlockState(getPos().offset(getFrontFacing().getOpposite())).getBlock();
        if (!isPipeBlockValid(block)) return;

        HashSet<BlockPos> newChecks = new HashSet<>();
        HashSet<BlockPos> oldChecks = new HashSet<>(Collections.singletonList(getPos()));
        HashSet<BlockPos> toCheck = new HashSet<>(Collections.singletonList(getPos().offset(EnumFacing.NORTH)));
        HashSet<BlockPos> pipes = new HashSet<>();

        while (!toCheck.isEmpty()) {
            for (BlockPos pos : toCheck) {
                if (getWorld().getBlockState(pos).getBlock() == block) {
                    pipes.add(pos);
                    BlockPos surroundingPos;
                    for (EnumFacing facing : EnumFacing.VALUES) {
                        surroundingPos = pos.offset(facing);
                        if (oldChecks.add(surroundingPos)) newChecks.add(surroundingPos);
                    }
                } else {
                    TileEntity tileEntity = getWorld().getTileEntity(pos);
                    if (tileEntity != this.getHolder() && tileEntity instanceof IGregTechTileEntity) {
                        MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
                        if (metaTileEntity != this && metaTileEntity instanceof MetaTileEntityPipelineEndpoint) {
                            if (pipes.contains(metaTileEntity.getPos().offset(metaTileEntity.getFrontFacing()))) {
                                if (getDistanceToPosition(pos) >= getMinimumEndpointDistance()) {
                                    target = (MetaTileEntityPipelineEndpoint) metaTileEntity;
                                    targetPos = target.getPos();
                                    return;
                                }
                            }
                        }
                        oldChecks.remove(pos);
                    }
                }
            }
            toCheck.clear();
            toCheck.addAll(newChecks);
            newChecks.clear();
        }
    }

    public boolean checkTargetValid() {
        if (getWorld() == null || getWorld().isRemote)
            return false;

        if (targetPos == null) {
            // no target position, so scan for one
            scanPipes();
        } else if (target == null || !target.isValid()) {
            // no target, try checking the position
            target = null;
            if (getWorld().isBlockLoaded(targetPos)) {
                // only check if the chunk is loaded
                TileEntity tileEntity = getWorld().getTileEntity(targetPos);
                if (tileEntity instanceof IGregTechTileEntity && ((IGregTechTileEntity) tileEntity).getMetaTileEntity() instanceof MetaTileEntityPipelineEndpoint &&
                        isSameConnector(((IGregTechTileEntity) tileEntity).getMetaTileEntity())) {
                    // correct connector found
                    target = (MetaTileEntityPipelineEndpoint) ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
                } else if (tileEntity != null) {
                    // wrong connector type, so invalidate the position
                    targetPos = null;
                }
            }
        }
        // no target found
        if (target == null || target == this) return false;
        // if the target does not have a source, or this is not the target's source, make it this
        if (target.source == null || !target.source.isValid() || target.source.target == null || !target.source.target.isValid())
            target.source = this;

        // return if this is the target's source
        return target.source == this;
    }

    /**
     *
     * @param block the block to check
     * @return {@code true} if the pipe block is valid for this pipeline, else {@code false}
     */
    protected abstract boolean isPipeBlockValid(Block block);

    /**
     *
     * @param metaTileEntity the metaTileEntity to check
     * @return {@code true} if the metaTileEntity is the same type of pipeline connector, else {@code false}
     */
    protected abstract boolean isSameConnector(MetaTileEntity metaTileEntity);

    /**
     *
     * @return the minimum distance in blocks between pipeline endpoints for a valid pipeline connection
     */
    protected abstract int getMinimumEndpointDistance();

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        if (targetPos != null && target != this) {
            data.setBoolean("hasTarget", true);
            data.setInteger("targetX", targetPos.getX());
            data.setInteger("targetY", targetPos.getY());
            data.setInteger("targetZ", targetPos.getZ());
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("hasTarget")) {
            targetPos = new BlockPos(data.getInteger("targetX"), data.getInteger("targetY"), data.getInteger("targetZ"));
            if (getDistanceToPosition(targetPos) < getMinimumEndpointDistance()) targetPos = null;
        }
    }

    @Override
    public boolean onRightClick(@Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (stack.hasCapability(GregtechCapabilities.CAPABILITY_MALLET, null)) {
            ISoftHammerItem softHammerItem = stack.getCapability(GregtechCapabilities.CAPABILITY_MALLET, null);
            if (softHammerItem == null) return false;

            if (softHammerItem.damageItem(DamageValues.DAMAGE_FOR_SOFT_HAMMER, true)) {
                softHammerItem.damageItem(DamageValues.DAMAGE_FOR_SOFT_HAMMER, false);
                if (!getWorld().isRemote) scanPipes();

                IToolStats.onOtherUse(stack, getWorld(), getPos());
                return true;
            }
            return false;
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.long_distance_pipeline.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.long_distance_pipeline.tooltip.2", getMinimumEndpointDistance()));
        tooltip.add(I18n.format("gregtech.machine.long_distance_pipeline.tooltip.3"));
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        if (source != null && source.isValid() && source.target == this) {
            BlockPos pos = source.getPos();
            return Arrays.asList(new TextComponentTranslation("behavior.tricorder.long_distance_pipeline.is_target").setStyle(new Style().setColor(TextFormatting.YELLOW)),
                    new TextComponentTranslation("behavior.tricorder.long_distance_pipeline.source_pos",
                            new TextComponentTranslation(GTUtility.formatNumbers(pos.getX())).setStyle(new Style().setColor(TextFormatting.AQUA)),
                            new TextComponentTranslation(GTUtility.formatNumbers(pos.getY())).setStyle(new Style().setColor(TextFormatting.AQUA)),
                            new TextComponentTranslation(GTUtility.formatNumbers(pos.getZ())).setStyle(new Style().setColor(TextFormatting.AQUA))));
        }
        if (checkTargetValid()) {
            return Arrays.asList(new TextComponentTranslation("behavior.tricorder.long_distance_pipeline.has_target").setStyle(new Style().setColor(TextFormatting.GREEN)),
                    new TextComponentTranslation("behavior.tricorder.long_distance_pipeline.target_pos",
                            new TextComponentTranslation(GTUtility.formatNumbers(targetPos.getX())).setStyle(new Style().setColor(TextFormatting.AQUA)),
                            new TextComponentTranslation(GTUtility.formatNumbers(targetPos.getY())).setStyle(new Style().setColor(TextFormatting.AQUA)),
                            new TextComponentTranslation(GTUtility.formatNumbers(targetPos.getZ())).setStyle(new Style().setColor(TextFormatting.AQUA))));
        }

        return Collections.singletonList(new TextComponentTranslation("behavior.tricorder.long_distance_pipeline.no_target").setStyle(new Style().setColor(TextFormatting.RED)));
    }
}
