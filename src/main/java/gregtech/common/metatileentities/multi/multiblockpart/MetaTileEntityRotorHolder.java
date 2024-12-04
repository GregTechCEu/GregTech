package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IRotorHolder;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.items.behaviors.AbstractMaterialPartBehavior;
import gregtech.common.items.behaviors.TurbineRotorBehavior;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine;
import gregtech.core.advancement.AdvancementTriggers;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityRotorHolder extends MetaTileEntityMultiblockNotifiablePart
                                       implements IMultiblockAbilityPart<IRotorHolder>, IRotorHolder {

    static final int SPEED_INCREMENT = 1;
    static final int SPEED_DECREMENT = 3;

    private final InventoryRotorHolder inventory;

    private final int maxSpeed;
    private int currentSpeed;
    private int rotorColor = -1;
    private boolean isRotorSpinning;
    private boolean frontFaceFree;

    public MetaTileEntityRotorHolder(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier, false);
        this.inventory = new InventoryRotorHolder();
        this.maxSpeed = 2000 + 1000 * tier;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityRotorHolder(metaTileEntityId, getTier());
    }

    @Override
    public IItemHandlerModifiable getImportItems() {
        return this.inventory;
    }

    @Override
    protected ModularUI createUI(@NotNull EntityPlayer entityPlayer) {
        return ModularUI.defaultBuilder()
                .label(6, 6, getMetaFullName())
                .slot(inventory, 0, 79, 36, GuiTextures.SLOT, GuiTextures.TURBINE_OVERLAY)
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
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public MultiblockAbility<IRotorHolder> getAbility() {
        return MultiblockAbility.ROTOR_HOLDER;
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) return;

        if (getOffsetTimer() % 20 == 0) {
            boolean isFrontFree = checkTurbineFaceFree();
            if (isFrontFree != this.frontFaceFree) {
                this.frontFaceFree = isFrontFree;
                writeCustomData(GregtechDataCodes.FRONT_FACE_FREE, buf -> buf.writeBoolean(this.frontFaceFree));
            }
        }

        MetaTileEntityLargeTurbine controller = (MetaTileEntityLargeTurbine) getController();

        if (controller != null && controller.isActive()) {
            if (currentSpeed < maxSpeed) {
                setCurrentSpeed(currentSpeed + SPEED_INCREMENT);
            }
            if (getOffsetTimer() % 20 == 0) {
                damageRotor(1 + controller.getNumMaintenanceProblems());
            }
        } else if (!hasRotor()) {
            setCurrentSpeed(0);
        } else if (currentSpeed > 0) {
            setCurrentSpeed(Math.max(0, currentSpeed - SPEED_DECREMENT));
        }
    }

    void setCurrentSpeed(int speed) {
        if (currentSpeed != speed) {
            currentSpeed = speed;
            setRotorSpinning(currentSpeed > 0);
            markDirty();
        }
    }

    void setRotorSpinning(boolean spinning) {
        if (isRotorSpinning != spinning) {
            isRotorSpinning = spinning;
            writeCustomData(GregtechDataCodes.IS_ROTOR_LOOPING, buf -> buf.writeBoolean(isRotorSpinning));
        }
    }

    @Override
    public void registerAbilities(@NotNull List<IRotorHolder> abilityList) {
        abilityList.add(this);
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    /**
     * @return true if front face is free and contains only air blocks in 3x3 area
     */
    public boolean isFrontFaceFree() {
        return frontFaceFree;
    }

    private boolean checkTurbineFaceFree() {
        final EnumFacing front = getFrontFacing();
        // this can be anything really, as long as it is not up/down when on Y axis
        final EnumFacing upwards = front.getAxis() == EnumFacing.Axis.Y ? EnumFacing.NORTH : EnumFacing.UP;

        for (int left = -1; left <= 1; left++) {
            for (int up = -1; up <= 1; up++) {
                // flip doesn't affect anything here since we are checking a square anyway
                final BlockPos checkPos = RelativeDirection.offsetPos(
                        getPos(), front, upwards, false, up, left, 1);
                final IBlockState state = getWorld().getBlockState(checkPos);
                if (!state.getBlock().isAir(state, getWorld(), checkPos)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean onRotorHolderInteract(@NotNull EntityPlayer player) {
        if (player.isCreative()) return false;

        if (!getWorld().isRemote && isRotorSpinning) {
            float damageApplied = Math.min(1, currentSpeed / 1000);
            player.attackEntityFrom(DamageSources.getTurbineDamage(), damageApplied);
            AdvancementTriggers.ROTOR_HOLDER_DEATH.trigger((EntityPlayerMP) player);
            return true;
        }
        return isRotorSpinning;
    }

    /**
     * returns true on both the Client and Server
     *
     * @return whether there is a rotor in the holder
     */
    @Override
    public boolean hasRotor() {
        return rotorColor != -1;
    }

    protected void setRotorColor(int color) {
        this.rotorColor = color;
    }

    protected int getRotorColor() {
        return rotorColor;
    }

    @Override
    public int getRotorSpeed() {
        return this.currentSpeed;
    }

    @Override
    public int getRotorEfficiency() {
        return inventory.getRotorEfficiency();
    }

    @Override
    public int getRotorPower() {
        return inventory.getRotorPower();
    }

    @Override
    public int getRotorDurabilityPercent() {
        return inventory.getRotorDurabilityPercent();
    }

    @Override
    public void damageRotor(int amount) {
        inventory.damageRotor(amount);
    }

    @Override
    public int getMaxRotorHolderSpeed() {
        return this.maxSpeed;
    }

    /**
     * calculates the holder's power multiplier: 2x per tier above the multiblock controller
     *
     * @return the power multiplier provided by the rotor holder
     */
    @Override
    public int getHolderPowerMultiplier() {
        int tierDifference = getTierDifference();
        if (tierDifference == -1) return -1;

        return (int) Math.pow(2, getTierDifference());
    }

    @Override
    public int getHolderEfficiency() {
        int tierDifference = getTierDifference();
        if (tierDifference == -1)
            return -1;

        return 100 + 10 * tierDifference;
    }

    private int getTierDifference() {
        if (getController() instanceof ITieredMetaTileEntity) {
            return getTier() - ((ITieredMetaTileEntity) getController()).getTier();
        }
        return -1;
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        return onRotorHolderInteract(playerIn) || super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                 CuboidRayTraceResult hitResult) {
        return onRotorHolderInteract(playerIn) || super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        return onRotorHolderInteract(playerIn);
    }

    @Override
    public void onLeftClick(EntityPlayer player, EnumFacing facing, CuboidRayTraceResult hitResult) {
        onRotorHolderInteract(player);
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, inventory);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("inventory", inventory.serializeNBT());
        data.setInteger("currentSpeed", currentSpeed);
        data.setBoolean("Spinning", isRotorSpinning);
        data.setBoolean("FrontFree", frontFaceFree);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.inventory.deserializeNBT(data.getCompoundTag("inventory"));
        this.currentSpeed = data.getInteger("currentSpeed");
        this.isRotorSpinning = data.getBoolean("Spinning");
        this.frontFaceFree = data.getBoolean("FrontFree");
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.IS_ROTOR_LOOPING) {
            this.isRotorSpinning = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.FRONT_FACE_FREE) {
            this.frontFaceFree = buf.readBoolean();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isRotorSpinning);
        buf.writeInt(rotorColor);
        buf.writeBoolean(frontFaceFree);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isRotorSpinning = buf.readBoolean();
        this.rotorColor = buf.readInt();
        this.frontFaceFree = buf.readBoolean();
        scheduleRenderUpdate();
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ROTOR_HOLDER_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.LARGE_TURBINE_ROTOR_RENDERER.renderSided(renderState, translation, pipeline, getFrontFacing(),
                getController() != null, hasRotor(), isRotorSpinning, getRotorColor());
    }

    private class InventoryRotorHolder extends NotifiableItemStackHandler {

        public InventoryRotorHolder() {
            super(MetaTileEntityRotorHolder.this, 1, null, false);
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
        public void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setRotorColor(getRotorColor());
            scheduleRenderUpdate();
        }

        @Nullable
        private ItemStack getTurbineStack() {
            if (!hasRotor())
                return null;
            return getStackInSlot(0);
        }

        @Nullable
        private TurbineRotorBehavior getTurbineBehavior() {
            ItemStack stack = getStackInSlot(0);
            if (stack.isEmpty()) return null;

            return TurbineRotorBehavior.getInstanceFor(stack);
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private boolean hasRotor() {
            return getTurbineBehavior() != null;
        }

        private int getRotorColor() {
            if (!hasRotor()) return -1;
            // noinspection ConstantConditions
            return getTurbineBehavior().getPartMaterial(getStackInSlot(0)).getMaterialRGB();
        }

        private int getRotorDurabilityPercent() {
            if (!hasRotor()) return 0;

            // noinspection ConstantConditions
            return getTurbineBehavior().getRotorDurabilityPercent(getStackInSlot(0));
        }

        private int getRotorEfficiency() {
            if (!hasRotor()) return -1;

            // noinspection ConstantConditions
            return getTurbineBehavior().getRotorEfficiency(getTurbineStack());
        }

        private int getRotorPower() {
            if (!hasRotor()) return -1;

            // noinspection ConstantConditions
            return getTurbineBehavior().getRotorPower(getTurbineStack());
        }

        private void damageRotor(int damageAmount) {
            if (!hasRotor()) return;

            if (getTurbineBehavior().getPartMaxDurability(getTurbineStack()) <=
                    AbstractMaterialPartBehavior.getPartDamage(getTurbineStack()) + damageAmount) {
                var holder = (MultiblockFuelRecipeLogic) getController().getRecipeLogic();
                if (holder != null && holder.isWorking()) {
                    holder.invalidate();
                }
            }

            // noinspection ConstantConditions
            getTurbineBehavior().applyRotorDamage(getStackInSlot(0), damageAmount);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return TurbineRotorBehavior.getInstanceFor(stack) != null && super.isItemValid(slot, stack);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack itemStack = super.extractItem(slot, amount, simulate);
            if (!simulate && itemStack != ItemStack.EMPTY) setRotorColor(-1);
            return itemStack;
        }
    }
}
