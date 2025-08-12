package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IRotorHolder;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.items.behaviors.AbstractMaterialPartBehavior;
import gregtech.common.items.behaviors.TurbineRotorBehavior;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine;
import gregtech.core.advancement.AdvancementTriggers;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityRotorHolder extends MetaTileEntityMultiblockNotifiablePart
        implements IMultiblockAbilityPart<IRotorHolder>, IRotorHolder {

    static final int SPEED_INCREMENT = 1;
    static final int SPEED_DECREMENT = 3;
    // 最小推动转速
    private static final double MIN_SPEED_TO_PUSH = 2000;
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
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ?
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.inventory) :
                super.getCapability(capability, side);
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        var pos = getPos();
        if (!inventory.getStackInSlot(0).isEmpty()) {
            getWorld().spawnEntity(new EntityItem(getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    inventory.getStackInSlot(0)));
            inventory.extractItem(0, 1, false);
        }
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
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        guiSyncManager.registerSlotGroup("item_inv", 1);
        // TODO: Change the position of the name when it's standardized.
        return GTGuis.createPanel(this, 176, 166)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(SlotGroupWidget.playerInventory().left(7).bottom(7))
                .child(new ItemSlot()
                        .slot(SyncHandlers.itemSlot(inventory, 0)
                                .slotGroup("item_inv")
                                .changeListener(
                                        (newItem, onlyAmountChanged, client, init) -> inventory.onContentsChanged(0)))
                        .background(GTGuiTextures.SLOT, GTGuiTextures.TURBINE_OVERLAY)
                        .left(79).top(36));
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

            // 实现将实体往远离转子的方向推动（检测范围3x3）
            if (ConfigHolder.machines.enableRotorDamage && getOffsetTimer() % 5 == 0 && currentSpeed > MIN_SPEED_TO_PUSH) {
                // 每5tick执行一次推动（避免过于频繁）

                // 获取转子前方方向（推动的反方向）
                EnumFacing rotorFacing = getFrontFacing();
                // 计算推动方向（远离转子）
                EnumFacing pushDirection = rotorFacing.getOpposite();

                // 获取当前位置并创建检测区域
                AxisAlignedBB pushArea = getAxisAlignedBB(rotorFacing);

                // 获取区域内的所有实体
                List<Entity> entities = getWorld().getEntitiesWithinAABB(Entity.class, pushArea);
                if (!entities.isEmpty()) {
                    // 计算推力（基于转速的线性关系）
                    double pushStrength = 0.10 * (currentSpeed * 1.0 / 2000);
                    Vec3d pushVector = new Vec3d(
                            pushDirection.getXOffset() * pushStrength,
                            pushDirection.getYOffset() * pushStrength,
                            pushDirection.getZOffset() * pushStrength
                    );

                    // 对每个实体施加推力
                    for (Entity entity : entities) {
                        // 防止推动自身（机械方块等）
                        if (entity instanceof IGregTechTileEntity) continue;

                        // 应用推动力并标记速度变化
                        entity.addVelocity(pushVector.x, pushVector.y, pushVector.z);
                        entity.velocityChanged = true;

                        applyDamage(entity);
                        damageRotor(500);
                    }
                }

            }
        } else if (!hasRotor()) {
            setCurrentSpeed(0);
        } else if (currentSpeed > 0) {
            setCurrentSpeed(Math.max(0, currentSpeed - SPEED_DECREMENT));
        }
    }

    private @NotNull AxisAlignedBB getAxisAlignedBB(EnumFacing rotorFacing) {
        BlockPos centerPos = getPos();
        // 获取转子正面位置（转子前方第一格）
        BlockPos frontPos = centerPos.offset(rotorFacing);

        // 沿着转子前方方向延伸7格（包括正面位置）
        BlockPos farPos = frontPos.offset(rotorFacing, 6);

        // 创建3x3×7的检测区域（沿着转子前方方向）
        return new AxisAlignedBB(
                Math.min(frontPos.getX(), farPos.getX()) - (rotorFacing.getAxis() != EnumFacing.Axis.X ? 1 : 0),
                Math.min(frontPos.getY(), farPos.getY()) - (rotorFacing.getAxis() != EnumFacing.Axis.Y ? 1 : 0),
                Math.min(frontPos.getZ(), farPos.getZ()) - (rotorFacing.getAxis() != EnumFacing.Axis.Z ? 1 : 0),
                Math.max(frontPos.getX(), farPos.getX()) + 1 + (rotorFacing.getAxis() != EnumFacing.Axis.X ? 1 : 0),
                Math.max(frontPos.getY(), farPos.getY()) + 1 + (rotorFacing.getAxis() != EnumFacing.Axis.Y ? 1 : 0),
                Math.max(frontPos.getZ(), farPos.getZ()) + 1 + (rotorFacing.getAxis() != EnumFacing.Axis.Z ? 1 : 0)
        );
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
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(this);
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
                if (left == 0 && up == 0) continue;
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
            applyDamage(player);
            AdvancementTriggers.ROTOR_HOLDER_DEATH.trigger((EntityPlayerMP) player);
            return true;
        }
        return isRotorSpinning;
    }

    private void applyDamage(Entity  entity) {
        float damageApplied = Math.min(1, currentSpeed / 1000);
        entity.attackEntityFrom(DamageSources.getTurbineDamage(), damageApplied);
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

    protected int getRotorColor() {
        return rotorColor;
    }

    protected void setRotorColor(int color) {
        this.rotorColor = color;
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
            getTurbineBehavior();
            return AbstractMaterialPartBehavior.getPartMaterial(getStackInSlot(0)).getMaterialRGB();
        }

        private int getRotorDurabilityPercent() {
            if (!hasRotor()) return 0;

            // noinspection ConstantConditions
            return getTurbineBehavior().getRotorDurabilityPercent(getStackInSlot(0));
        }

        private int getRotorEfficiency() {
            if (!hasRotor()) return -1;

            // noinspection ConstantConditions
            getTurbineBehavior();
            return TurbineRotorBehavior.getRotorEfficiency(getTurbineStack());
        }

        private int getRotorPower() {
            if (!hasRotor()) return -1;

            // noinspection ConstantConditions
            getTurbineBehavior();
            return TurbineRotorBehavior.getRotorPower(getTurbineStack());
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
