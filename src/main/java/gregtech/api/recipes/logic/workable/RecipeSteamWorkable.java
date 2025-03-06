package gregtech.api.recipes.logic.workable;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IVentable;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.logic.statemachine.builder.RecipeStandardStateMachineBuilder;
import gregtech.api.statemachine.GTStateMachine;
import gregtech.api.util.FacingPos;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.core.advancement.AdvancementTriggers;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class RecipeSteamWorkable extends RecipeWorkable implements IVentable {

    protected boolean needsVenting;
    protected boolean ventingStuck;

    public <T extends MetaTileEntity & ISupportsRecipeSteamWorkable> RecipeSteamWorkable(@NotNull T metaTileEntity,
                                                                                         @NotNull RecipeStandardStateMachineBuilder builder) {
        super(metaTileEntity, builder);
    }

    protected <T extends MetaTileEntity & ISupportsRecipeSteamWorkable> RecipeSteamWorkable(@NotNull T metaTileEntity,
                                                                                            @NotNull GTStateMachine recipeStateMachine) {
        super(metaTileEntity, recipeStateMachine);
    }

    @Override
    protected void onRecipeCompleted(NBTTagCompound recipe) {
        super.onRecipeCompleted(recipe);
        setNeedsVenting(true);
        tryDoVenting();
    }

    @Override
    protected @NotNull ISupportsRecipeSteamWorkable getSupport() {
        return (ISupportsRecipeSteamWorkable) getMetaTileEntity();
    }

    @Override
    public void update() {
        if (isNeedsVenting() && getMetaTileEntity().getOffsetTimer() % 10 == 0) {
            tryDoVenting();
        }
        if (isVentingStuck()) return;
        super.update();
    }

    @Override
    public boolean isNeedsVenting() {
        return needsVenting;
    }

    @Override
    public void tryDoVenting() {
        if (getMetaTileEntity().getWorld().isRemote) return;
        Collection<FacingPos> valid = getSupport().getVentingBlockFacings();
        if (valid.size() == 0) {
            setVentingStuck(true);
            return;
        }
        FacingPos[] arr = valid.toArray(new FacingPos[0]);
        int count = arr.length;
        for (int i = 0; i < arr.length; i++) {
            BlockPos offset = arr[i].offset();
            IBlockState blockOnPos = getMetaTileEntity().getWorld().getBlockState(offset);
            if (blockOnPos.getCollisionBoundingBox(getMetaTileEntity().getWorld(), offset) != Block.NULL_AABB &&
                    !GTUtility.tryBreakSnow(metaTileEntity.getWorld(), offset, blockOnPos, false)) {
                arr[i] = null;
                count--;
            }
        }
        if (count == 0) {
            setVentingStuck(true);
            return;
        }
        WorldServer world = (WorldServer) metaTileEntity.getWorld();
        float damage = getSupport().getVentingDamage() / count;
        for (FacingPos facingPos : arr) {
            if (facingPos == null) continue;
            if (damage > 0) {
                for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(facingPos.offset()),
                        EntitySelectors.CAN_AI_TARGET)) {
                    entity.attackEntityFrom(DamageSources.getHeatDamage(), damage);
                    if (entity instanceof EntityPlayerMP) {
                        AdvancementTriggers.STEAM_VENT_DEATH.trigger((EntityPlayerMP) entity);
                    }
                }
            }
            double posX = facingPos.getPos().getX() + 0.5 + facingPos.getFacing().getXOffset() * 0.6;
            double posY = facingPos.getPos().getY() + 0.5 + facingPos.getFacing().getYOffset() * 0.6;
            double posZ = facingPos.getPos().getZ() + 0.5 + facingPos.getFacing().getZOffset() * 0.6;

            world.spawnParticle(EnumParticleTypes.CLOUD, posX, posY, posZ,
                    (6 + world.rand.nextInt(2)) / count + 1 + world.rand.nextInt(2),
                    facingPos.getFacing().getXOffset() / 2.0,
                    facingPos.getFacing().getYOffset() / 2.0,
                    facingPos.getFacing().getZOffset() / 2.0, 0.1);
            if (ConfigHolder.machines.machineSounds && !metaTileEntity.isMuffled()) {
                world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0f,
                        1.0f);
            }
        }
        setNeedsVenting(false);
    }

    @Override
    public boolean isVentingStuck() {
        return ventingStuck;
    }

    public void setVentingStuck(boolean ventingStuck) {
        if (ventingStuck != this.ventingStuck) {
            this.ventingStuck = ventingStuck;
            if (!metaTileEntity.getWorld().isRemote) {
                metaTileEntity.markDirty();
                writeCustomData(GregtechDataCodes.VENTING_STUCK, buf -> buf.writeBoolean(ventingStuck));
            }
        }
    }

    @Override
    public void setNeedsVenting(boolean needsVenting) {
        if (needsVenting != this.needsVenting) {
            this.needsVenting = needsVenting;
            if (!needsVenting && isVentingStuck()) {
                setVentingStuck(false);
            }
            if (!metaTileEntity.getWorld().isRemote) {
                metaTileEntity.markDirty();
                writeCustomData(GregtechDataCodes.NEEDS_VENTING, buf -> buf.writeBoolean(needsVenting));
            }
        }
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.NEEDS_VENTING) {
            this.needsVenting = buf.readBoolean();
        } else if (dataId == GregtechDataCodes.VENTING_STUCK) {
            this.ventingStuck = buf.readBoolean();
        }
    }

    @NotNull
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        compound.setBoolean("NeedsVenting", needsVenting);
        compound.setBoolean("VentingStuck", ventingStuck);
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.needsVenting = compound.getBoolean("NeedsVenting");
        this.ventingStuck = compound.getBoolean("VentingStuck");
    }

    public interface ISupportsRecipeSteamWorkable extends ISupportsRecipeWorkable {

        @NotNull
        Collection<FacingPos> getVentingBlockFacings();

        float getVentingDamage();
    }
}
