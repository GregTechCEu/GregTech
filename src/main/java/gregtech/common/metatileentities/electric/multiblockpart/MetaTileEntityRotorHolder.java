package gregtech.common.metatileentities.electric.multiblockpart;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.api.unification.material.Material;
import gregtech.common.advancement.GTTriggers;
import gregtech.common.items.behaviors.TurbineRotorBehavior;
import gregtech.common.metatileentities.multi.electric.generator.RotorHolderMultiblockController;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.IS_ROTOR_LOOPING;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_ROTOR_COLOR;

public class MetaTileEntityRotorHolder extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<MetaTileEntityRotorHolder> {

    private static final int NORMAL_MAXIMUM_SPEED = 6000;
    private static final float DAMAGE_PER_INTERACT = 40.0f;

    private final InventoryRotorHolder rotorInventory;
    private final int maxRotorSpeed;
    private int currentRotorSpeed;

    private boolean isRotorLooping;
    private int rotorColor = -1;
    private boolean frontFaceFree;

    public MetaTileEntityRotorHolder(ResourceLocation metaTileEntityId, int tier, int maxSpeed) {
        super(metaTileEntityId, tier);
        this.maxRotorSpeed = maxSpeed;
        this.currentRotorSpeed = 0;
        this.rotorInventory = new InventoryRotorHolder();
    }

    public MetaTileEntityRotorHolder(ResourceLocation metaTileEntityId, int tier, float speedMultiplier) {
        this(metaTileEntityId, tier, (int) (NORMAL_MAXIMUM_SPEED * speedMultiplier));
    }

    public ItemStackHandler getRotorInventory() {
        return rotorInventory;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityRotorHolder(metaTileEntityId, getTier(), maxRotorSpeed);
    }

    @Override
    public void update() {
        super.update();

        if (getWorld().isRemote) {
            return;
        }
        if (getOffsetTimer() % 10 == 0) {
            this.frontFaceFree = checkTurbineFaceFree();
        }

        RotorHolderMultiblockController controller = (RotorHolderMultiblockController) getController();

        if (!isHasRotor()) {
            resetRotorSpeed();
        } else if (controller != null) {
            boolean isControllerActive = controller.isActive();

            if (isControllerActive && currentRotorSpeed < maxRotorSpeed) {
                incrementSpeed(controller.getRotorSpeedIncrement());
            } else if (!isControllerActive && currentRotorSpeed > 0) {
                incrementSpeed(controller.getRotorSpeedDecrement());
            }
        }
    }

    /**
     * @return true if front face is free and contains only air blocks in 3x3 area
     */
    public boolean isFrontFaceFree() {
        return frontFaceFree;
    }

    private boolean checkTurbineFaceFree() {
        EnumFacing facing = getFrontFacing();
        //TODO, this also needs to check Y for freedom wrench implementation
        boolean permuteXZ = facing.getAxis() == Axis.Z;
        BlockPos centerPos = getPos().offset(facing);
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                BlockPos blockPos = centerPos.add(permuteXZ ? x : 0, y, permuteXZ ? 0 : x);
                IBlockState blockState = getWorld().getBlockState(blockPos);
                if (!blockState.getBlock().isAir(blockState, getWorld(), blockPos)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean onRotorHolderInteract(EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            return false;
        }
        if (!getWorld().isRemote && isRotorLooping) {
            double relativeSpeed = getRelativeRotorSpeed();
            float damageApplied = (float) (DAMAGE_PER_INTERACT * relativeSpeed);
            player.attackEntityFrom(DamageSources.getTurbineDamage(), damageApplied);
            GTTriggers.ROTOR_HOLDER_DEATH.trigger((EntityPlayerMP) player);
            return true;
        }
        return isRotorLooping;
    }

    /**
     * @return current rotor speed, relative to NORMAL_MAXIMUM_SPEED
     * used for scaling stats like produced EU, rotor damage and interact damage
     */
    public double getRelativeRotorSpeed() {
        return currentRotorSpeed == 0 ? 0.01 : currentRotorSpeed / (NORMAL_MAXIMUM_SPEED * 1.0);
    }

    /**
     * @return true if current rotor is still looping, i.e it's current speed > 0
     * this can return true even if multiblock is not formed
     */
    public boolean isRotorLooping() {
        return isRotorLooping;
    }

    /**
     * @return true if this holder has rotor in it's inventory
     * this will return true even if it's client side and used for rendering
     */
    public boolean isHasRotor() {
        return rotorColor != -1;
    }

    public void resetRotorSpeed() {
        currentRotorSpeed = 0;
        markDirty();
    }

    public int getRotorColor() {
        return rotorColor;
    }

    public double getRotorEfficiency() {
        return rotorInventory.getRotorEfficiency();
    }

    public boolean hasRotorInInventory() {
        return !rotorInventory.getStackInSlot(0).isEmpty();
    }

    public boolean applyDamageToRotor(int damageAmount, boolean simulate) {
        return rotorInventory.applyDamageToRotor(damageAmount, simulate);
    }

    public double getRotorDurability() {
        return 1.0 - rotorInventory.getRotorDamagePercentage();
    }

    public int getMaxRotorSpeed() {
        return maxRotorSpeed;
    }

    public int getCurrentRotorSpeed() {
        return currentRotorSpeed;
    }

    protected void incrementSpeed(int incrementSpeed) {
        boolean lastIsLooping = currentRotorSpeed > 0;
        this.currentRotorSpeed = MathHelper.clamp(currentRotorSpeed + incrementSpeed, 0, maxRotorSpeed);
        this.isRotorLooping = currentRotorSpeed > 0;
        if (isRotorLooping != lastIsLooping && !getWorld().isRemote) {
            writeCustomData(IS_ROTOR_LOOPING, writer -> writer.writeBoolean(isRotorLooping));
            markDirty();
        }
    }

    private void setRotorColor(int hasRotor1) {
        int lastHasRotor = rotorColor;
        this.rotorColor = hasRotor1;
        if (rotorColor != lastHasRotor && (getWorld() != null && !getWorld().isRemote)) {
            writeCustomData(UPDATE_ROTOR_COLOR, writer -> writer.writeInt(rotorColor));
            markDirty();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == IS_ROTOR_LOOPING) {
            this.isRotorLooping = buf.readBoolean();
            getHolder().scheduleChunkForRenderUpdate();
        } else if (dataId == UPDATE_ROTOR_COLOR) {
            this.rotorColor = buf.readInt();
            getHolder().scheduleChunkForRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isRotorLooping);
        buf.writeInt(rotorColor);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isRotorLooping = buf.readBoolean();
        this.rotorColor = buf.readInt();
        getHolder().scheduleChunkForRenderUpdate();
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, rotorInventory);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("RotorInventory", rotorInventory.serializeNBT());
        data.setInteger("CurrentSpeed", currentRotorSpeed);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.rotorInventory.deserializeNBT(data.getCompoundTag("RotorInventory"));
        this.currentRotorSpeed = data.getInteger("CurrentSpeed");
        this.isRotorLooping = currentRotorSpeed > 0;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ROTOR_HOLDER_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.LARGE_TURBINE_ROTOR_RENDERER.renderSided(renderState, translation, pipeline, getFrontFacing(),
                getController() != null, isHasRotor(), isRotorLooping(), rotorColor);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return ModularUI.defaultBuilder()
                .label(6, 6, getMetaFullName())
                .slot(rotorInventory, 0, 79, 36, GuiTextures.SLOT, GuiTextures.TURBINE_OVERLAY)
                .bindPlayerInventory(entityPlayer.inventory)
                .build(getHolder(), entityPlayer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.rotor_holder.tooltip1"));
        tooltip.add(I18n.format("gregtech.machine.rotor_holder.tooltip2"));
        tooltip.add(I18n.format("gregtech.universal.disabled"));
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        return onRotorHolderInteract(playerIn) ||
                super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        return onRotorHolderInteract(playerIn) ||
                super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        return onRotorHolderInteract(playerIn);
    }

    @Override
    public void onLeftClick(EntityPlayer player, EnumFacing facing, CuboidRayTraceResult hitResult) {
        onRotorHolderInteract(player);
    }

    @Override
    public MultiblockAbility<MetaTileEntityRotorHolder> getAbility() {
        return MultiblockAbility.ABILITY_ROTOR_HOLDER;
    }

    @Override
    public void registerAbilities(List<MetaTileEntityRotorHolder> abilityList) {
        abilityList.add(this);
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    private class InventoryRotorHolder extends ItemStackHandler {

        public InventoryRotorHolder() {
            super(1);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onLoad() {
            rotorColor = getRotorColor();
        }

        @Override
        protected void onContentsChanged(int slot) {
            setRotorColor(getRotorColor());
        }

        private int getRotorColor() {
            ItemStack itemStack = getStackInSlot(0);
            if (itemStack.isEmpty()) {
                return -1;
            }
            TurbineRotorBehavior behavior = TurbineRotorBehavior.getInstanceFor(itemStack);
            if (behavior == null) {
                return -1;
            }
            Material material = behavior.getPartMaterial(itemStack);
            return material.getMaterialRGB();
        }

        public double getRotorEfficiency() {
            ItemStack itemStack = getStackInSlot(0);
            if (itemStack.isEmpty()) {
                return 0.0;
            }
            TurbineRotorBehavior turbineRotorBehavior = TurbineRotorBehavior.getInstanceFor(itemStack);
            if (turbineRotorBehavior != null) {
                return turbineRotorBehavior.getRotorEfficiency(itemStack);
            }
            return 0.0;
        }

        public double getRotorDamagePercentage() {
            ItemStack itemStack = getStackInSlot(0);
            if (itemStack.isEmpty()) {
                return 0.0;
            }
            TurbineRotorBehavior turbineRotorBehavior = TurbineRotorBehavior.getInstanceFor(itemStack);
            if (turbineRotorBehavior != null) {
                return turbineRotorBehavior.getPartDamage(itemStack) / (turbineRotorBehavior.getPartMaxDurability(itemStack) * 1.0);
            }
            return 0.0;
        }

        public boolean applyDamageToRotor(int damageAmount, boolean simulate) {
            ItemStack itemStack = getStackInSlot(0);
            if (itemStack.isEmpty()) {
                return false;
            }
            TurbineRotorBehavior turbineRotorBehavior = TurbineRotorBehavior.getInstanceFor(itemStack);
            if (turbineRotorBehavior != null) {
                if (!simulate) {
                    turbineRotorBehavior.applyRotorDamage(itemStack, damageAmount);
                }
                return true;
            }
            return false;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (TurbineRotorBehavior.getInstanceFor(stack) != null) {
                return super.insertItem(slot, stack, simulate);
            }
            return stack;
        }
    }

}
