package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IVentable;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.common.ConfigHolder;
import gregtech.common.advancement.GTTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;

public class RecipeLogicSteam extends AbstractRecipeLogic implements IVentable {

    private final IFluidTank steamFluidTank;
    private final boolean isHighPressure;
    private final double conversionRate; //energy units per millibucket

    private boolean needsVenting;
    private boolean ventingStuck;
    private EnumFacing ventingSide;

    public RecipeLogicSteam(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, boolean isHighPressure, IFluidTank steamFluidTank, double conversionRate) {
        super(tileEntity, recipeMap);
        this.steamFluidTank = steamFluidTank;
        this.conversionRate = conversionRate;
        this.isHighPressure = isHighPressure;
    }

    @Override
    public boolean isVentingStuck() {
        return needsVenting && ventingStuck;
    }

    @Override
    public boolean isNeedsVenting() {
        return needsVenting;
    }

    @Override
    public void onFrontFacingSet(EnumFacing newFrontFacing) {
        if (ventingSide == null) {
            setVentingSide(newFrontFacing.getOpposite());
        }
    }

    public EnumFacing getVentingSide() {
        return ventingSide == null ? EnumFacing.SOUTH : ventingSide;
    }

    public void setVentingStuck(boolean ventingStuck) {
        this.ventingStuck = ventingStuck;
        if (!metaTileEntity.getWorld().isRemote) {
            metaTileEntity.markDirty();
            writeCustomData(GregtechDataCodes.VENTING_STUCK, buf -> buf.writeBoolean(ventingStuck));
        }
    }

    @Override
    public void setNeedsVenting(boolean needsVenting) {
        this.needsVenting = needsVenting;
        if (!needsVenting && ventingStuck)
            setVentingStuck(false);
        if (!metaTileEntity.getWorld().isRemote) {
            metaTileEntity.markDirty();
            writeCustomData(GregtechDataCodes.NEEDS_VENTING, buf -> buf.writeBoolean(needsVenting));
        }
    }

    public void setVentingSide(EnumFacing ventingSide) {
        this.ventingSide = ventingSide;
        if (!metaTileEntity.getWorld().isRemote) {
            metaTileEntity.markDirty();
            writeCustomData(GregtechDataCodes.VENTING_SIDE, buf -> buf.writeByte(ventingSide.getIndex()));
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.NEEDS_VENTING) {
            this.needsVenting = buf.readBoolean();
        } else if (dataId == GregtechDataCodes.VENTING_SIDE) {
            this.ventingSide = EnumFacing.VALUES[buf.readByte()];
            getMetaTileEntity().scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.VENTING_STUCK) {
            this.ventingStuck = buf.readBoolean();
        }
    }

    @Override
    public void writeInitialData(@Nonnull PacketBuffer buf) {
        super.writeInitialData(buf);
        buf.writeByte(getVentingSide().getIndex());
        buf.writeBoolean(needsVenting);
        buf.writeBoolean(ventingStuck);
    }

    @Override
    public void receiveInitialData(@Nonnull PacketBuffer buf) {
        super.receiveInitialData(buf);
        this.ventingSide = EnumFacing.VALUES[buf.readByte()];
        this.needsVenting = buf.readBoolean();
        this.ventingStuck = buf.readBoolean();
    }

    @Override
    public void tryDoVenting() {
        BlockPos machinePos = metaTileEntity.getPos();
        EnumFacing ventingSide = getVentingSide();
        BlockPos ventingBlockPos = machinePos.offset(ventingSide);
        IBlockState blockOnPos = metaTileEntity.getWorld().getBlockState(ventingBlockPos);
        if (blockOnPos.getCollisionBoundingBox(metaTileEntity.getWorld(), ventingBlockPos) == Block.NULL_AABB) {
            performVentingAnimation(ventingBlockPos, machinePos);
        }
        else if(blockOnPos.getBlock() == Blocks.SNOW_LAYER && blockOnPos.getValue(BlockSnow.LAYERS) == 1) {
            performVentingAnimation(ventingBlockPos, machinePos);
            metaTileEntity.getWorld().destroyBlock(ventingBlockPos, false);
        }
        else if (!ventingStuck) {
            setVentingStuck(true);
        }
    }

    private void performVentingAnimation(BlockPos ventingBlockPos, BlockPos machinePos) {
        metaTileEntity.getWorld()
                .getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(ventingBlockPos), EntitySelectors.CAN_AI_TARGET)
                .forEach(entity -> {
                    entity.attackEntityFrom(DamageSources.getHeatDamage(), this.isHighPressure ? 12.0f : 6.0f);
                    if (entity instanceof EntityPlayerMP) {
                        GTTriggers.STEAM_VENT_DEATH.trigger((EntityPlayerMP) entity);
                    }
                });
        WorldServer world = (WorldServer) metaTileEntity.getWorld();
        double posX = machinePos.getX() + 0.5 + ventingSide.getXOffset() * 0.6;
        double posY = machinePos.getY() + 0.5 + ventingSide.getYOffset() * 0.6;
        double posZ = machinePos.getZ() + 0.5 + ventingSide.getZOffset() * 0.6;

        world.spawnParticle(EnumParticleTypes.CLOUD, posX, posY, posZ,
                7 + world.rand.nextInt(3),
                ventingSide.getXOffset() / 2.0,
                ventingSide.getYOffset() / 2.0,
                ventingSide.getZOffset() / 2.0, 0.1);
        if (ConfigHolder.machines.machineSounds && !metaTileEntity.isMuffled()){
            world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
        setNeedsVenting(false);

    }

    @Override
    public void update() {
        if (getMetaTileEntity().getWorld().isRemote)
            return;
        if (this.needsVenting && metaTileEntity.getOffsetTimer() % 10 == 0) {
            tryDoVenting();
        }
        super.update();
    }

    @Override
    protected boolean checkRecipe(@Nonnull Recipe recipe) {
        return super.checkRecipe(recipe) && !this.needsVenting;
    }

    @Override
    protected void completeRecipe() {
        super.completeRecipe();
        setNeedsVenting(true);
        tryDoVenting();
    }

    @Override
    protected int[] calculateOverclock(@Nonnull Recipe recipe) {

        //EUt, Duration
        int[] result = new int[2];

        result[0] = isHighPressure ? recipe.getEUt() * 2 : recipe.getEUt();
        result[1] = isHighPressure ? recipe.getDuration() : recipe.getDuration() * 2;

        return result;
    }

    @Override
    protected long getEnergyInputPerSecond() {
        return 0;
    }

    @Override
    protected long getEnergyStored() {
        return (long) Math.ceil(steamFluidTank.getFluidAmount() * conversionRate);
    }

    @Override
    protected long getEnergyCapacity() {
        return (long) Math.floor(steamFluidTank.getCapacity() * conversionRate);
    }

    @Override
    protected boolean drawEnergy(int recipeEUt, boolean simulate) {
        int resultDraw = (int) Math.ceil(recipeEUt / conversionRate);
        return resultDraw >= 0 && steamFluidTank.getFluidAmount() >= resultDraw &&
                steamFluidTank.drain(resultDraw, !simulate) != null;
    }

    @Override
    protected long getMaxVoltage() {
        return GTValues.V[GTValues.LV];
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        compound.setInteger("VentingSide", getVentingSide().getIndex());
        compound.setBoolean("NeedsVenting", needsVenting);
        compound.setBoolean("VentingStuck", ventingStuck);
        return compound;
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.ventingSide = EnumFacing.VALUES[compound.getInteger("VentingSide")];
        this.needsVenting = compound.getBoolean("NeedsVenting");
        this.ventingStuck = compound.getBoolean("VentingStuck");
    }
}
