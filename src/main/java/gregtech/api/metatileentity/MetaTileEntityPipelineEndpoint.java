package gregtech.api.metatileentity;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.tool.ISoftHammerItem;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.toolitem.IToolStats;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.transport.LongDistancePipeWalker;
import gregtech.common.tools.DamageValues;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public abstract class MetaTileEntityPipelineEndpoint extends MetaTileEntity implements IDataInfoProvider {

    protected MetaTileEntityPipelineEndpoint source = null;
    protected MetaTileEntityPipelineEndpoint target = null;
    protected int targetDistance = -1;
    private static long time = 0;

    public static void startTimer() {
        time = System.nanoTime();
    }

    public static void endTimer(int scannedPipes) {
        long end = System.nanoTime();
        GTLog.logger.info("Scanning {} pipes took {} ns", scannedPipes, end - time);
        time = 0;
    }

    public MetaTileEntityPipelineEndpoint(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    protected int getDistanceToPosition(@Nonnull BlockPos pos) {
        return (int) Math.ceil(getPos().getDistance(pos.getX(), pos.getY(), pos.getZ()));
    }

    protected void scanPipes() {
        LongDistancePipeWalker walker = getPipeWalker();
        walker.reset();
        walker.traversePipeNet();
        if (walker.getEndpoint() != null) {
            this.target = walker.getEndpoint();
            this.target.source = this;
            this.targetDistance = walker.getDistance();
        }
    }

    public static Pair<MetaTileEntityPipelineEndpoint, Integer> scanPipes(World world, BlockPos startPos, @Nullable EnumFacing endpointFacing, Predicate<IBlockState> pipeValidator, Predicate<MetaTileEntityPipelineEndpoint> endpointValidator) {
        startTimer();
        HashSet<BlockPos> observedSet = new HashSet<>();
        BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos(startPos);
        int length = 0;

        if (endpointFacing != null) {
            IBlockState block = world.getBlockState(startPos.offset(endpointFacing.getOpposite()));
            if (!pipeValidator.test(block)) {
                endTimer(0);
                return null;
            }
            observedSet.add(startPos);
            currentPos.move(endpointFacing.getOpposite());
            length++;
        }

        observedSet.add(startPos);

        Stack<EnumFacing> moveStack = new Stack<>();
        main:
        while (true) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                currentPos.move(facing);
                if (observedSet.contains(currentPos)) {
                    currentPos.move(facing.getOpposite());
                    continue;
                }
                IBlockState state = world.getBlockState(currentPos);
                if (pipeValidator.test(state)) {
                    observedSet.add(currentPos.toImmutable());
                    moveStack.push(facing.getOpposite());
                    length++;
                    continue main;
                }
                MetaTileEntity mte = MetaTileEntity.tryGet(world, currentPos);
                if (mte instanceof MetaTileEntityPipelineEndpoint && endpointValidator.test((MetaTileEntityPipelineEndpoint) mte)) {
                    endTimer(observedSet.size());
                    return Pair.of((MetaTileEntityPipelineEndpoint) mte, length);
                } else {
                    currentPos.move(facing.getOpposite());
                }
            }
            if (!moveStack.isEmpty()) {
                currentPos.move(moveStack.pop());
                //also remove already visited block from path
                //currentPath.path.remove(currentPos);
                length--;
            } else break;
        }
        return null;
    }

    public static MetaTileEntityPipelineEndpoint scanPipes2(World world, BlockPos pos, @Nullable EnumFacing endpointFacing, Predicate<IBlockState> pipeValidator, Predicate<MetaTileEntityPipelineEndpoint> endpointValidator) {
        startTimer();
        HashSet<BlockPos> newChecks = new HashSet<>();
        HashSet<BlockPos> oldChecks = new HashSet<>();
        HashSet<BlockPos> toCheck = new HashSet<>(Collections.singletonList(pos.offset(EnumFacing.NORTH)));

        // Start scanning from the output's side
        if (endpointFacing != null) {
            IBlockState block = world.getBlockState(pos.offset(endpointFacing.getOpposite()));
            if (!pipeValidator.test(block)) {
                endTimer(0);
                return null;
            }
            oldChecks.add(pos);
            toCheck.add(pos.offset(endpointFacing.getOpposite()));
        }

        while (!toCheck.isEmpty()) {
            for (BlockPos nextPos : toCheck) {
                if (pipeValidator.test(world.getBlockState(nextPos))) {
                    BlockPos surroundingPos;
                    for (EnumFacing facing : EnumFacing.VALUES) {
                        surroundingPos = nextPos.offset(facing);
                        if (oldChecks.add(surroundingPos)) newChecks.add(surroundingPos);
                    }
                } else {
                    MetaTileEntity mte = MetaTileEntity.tryGet(world, nextPos);
                    if (mte instanceof MetaTileEntityPipelineEndpoint && endpointValidator.test((MetaTileEntityPipelineEndpoint) mte)) {
                        endTimer(oldChecks.size() + newChecks.size());
                        return (MetaTileEntityPipelineEndpoint) mte;
                    }
                }
            }
            toCheck.clear();
            toCheck.addAll(newChecks);
            newChecks.clear();
        }
        endTimer(oldChecks.size());
        return null;
    }

    public void setSource(MetaTileEntityPipelineEndpoint endpoint) {
        this.source = endpoint;
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        onPipeBlockChanged(true);
    }

    public void onPipeBlockChanged() {
        onPipeBlockChanged(true);
    }

    private void onPipeBlockChanged(boolean send) {
        if (send) {
            if (source != null) {
                source.onPipeBlockChanged(false);
            }
            if (target != null) {
                target.onPipeBlockChanged(false);
            }
        }
        source = null;
        target = null;
        targetDistance = -1;
    }

    @Nullable
    public MetaTileEntityPipelineEndpoint getTargetEndpoint() {
        if (getWorld() == null || getWorld().isRemote) {
            throw new IllegalStateException("Can't check on null world or client!");
        }
        if (target == null) {
            if (targetDistance < 0) {
                scanPipes();
            }
            if (target == null) {
                return null;
            }
        }
        MetaTileEntity mte = MetaTileEntity.tryGet(getWorld(), target.getPos());
        if (mte != target) {
            onPipeBlockChanged(true);
            return getTargetEndpoint();
        }
        return target;
    }

    protected abstract LongDistancePipeWalker getPipeWalker();

    /**
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
        if (target != null && target != this) {
            BlockPos targetPos = target.getPos();
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
            //target = new BlockPos(data.getInteger("targetX"), data.getInteger("targetY"), data.getInteger("targetZ"));
            //if (getDistanceToPosition(target) < getMinimumEndpointDistance()) target = null;
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
                if (!getWorld().isRemote) {
                    if ((target == null && source == null) || targetDistance < 0) {
                        scanPipes();
                    } else {
                        onPipeBlockChanged();
                    }
                }

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
        if (isValid() && source != null && source.isValid()) {
            BlockPos pos = source.getPos();
            return Arrays.asList(new TextComponentTranslation("behavior.tricorder.long_distance_pipeline.is_target").setStyle(new Style().setColor(TextFormatting.YELLOW)),
                    new TextComponentTranslation("behavior.tricorder.long_distance_pipeline.source_pos",
                            new TextComponentTranslation(GTUtility.formatNumbers(pos.getX())).setStyle(new Style().setColor(TextFormatting.AQUA)),
                            new TextComponentTranslation(GTUtility.formatNumbers(pos.getY())).setStyle(new Style().setColor(TextFormatting.AQUA)),
                            new TextComponentTranslation(GTUtility.formatNumbers(pos.getZ())).setStyle(new Style().setColor(TextFormatting.AQUA))));
        }
        if (getTargetEndpoint() != null) {
            BlockPos targetPos = target.getPos();
            return Arrays.asList(new TextComponentTranslation("behavior.tricorder.long_distance_pipeline.has_target").setStyle(new Style().setColor(TextFormatting.GREEN)),
                    new TextComponentTranslation("behavior.tricorder.long_distance_pipeline.target_pos",
                            new TextComponentTranslation(GTUtility.formatNumbers(targetPos.getX())).setStyle(new Style().setColor(TextFormatting.AQUA)),
                            new TextComponentTranslation(GTUtility.formatNumbers(targetPos.getY())).setStyle(new Style().setColor(TextFormatting.AQUA)),
                            new TextComponentTranslation(GTUtility.formatNumbers(targetPos.getZ())).setStyle(new Style().setColor(TextFormatting.AQUA))),
                    new TextComponentString("Distance: " + targetDistance));
        }

        return Collections.singletonList(new TextComponentTranslation("behavior.tricorder.long_distance_pipeline.no_target").setStyle(new Style().setColor(TextFormatting.RED)));
    }
}
