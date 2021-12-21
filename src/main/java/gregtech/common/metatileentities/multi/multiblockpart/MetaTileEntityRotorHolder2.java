package gregtech.common.metatileentities.electric.multiblockpart;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IRotorHolder;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.advancement.GTTriggers;
import gregtech.common.items.behaviors.TurbineRotorBehavior2;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityRotorHolder2 extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IRotorHolder>, IRotorHolder {

    private final InventoryRotorHolder inventory;

    private final int maxSpeed;

    private boolean isRotorSpinning;
    private int currentSpeed;
    private int rotorColor = -1;

    public MetaTileEntityRotorHolder2(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.inventory = new InventoryRotorHolder();
        this.maxSpeed = 10 * tier;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityRotorHolder2(metaTileEntityId, getTier());
    }

    @Override
    protected ModularUI createUI(@Nonnull EntityPlayer entityPlayer) {
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
    public MultiblockAbility<IRotorHolder> getAbility() {
        return MultiblockAbility.ABILITY_ROTOR_HOLDER_2;
    }

    @Override
    public void registerAbilities(@Nonnull List<IRotorHolder> abilityList) {
        abilityList.add(this);
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    private boolean onRotorHolderInteract(@Nonnull EntityPlayer player) {
        if (player.isCreative())
            return false;

        if (!getWorld().isRemote && isRotorSpinning) {
            float damageApplied = Math.min(1, currentSpeed / 1000);
            player.attackEntityFrom(DamageSources.getTurbineDamage(), damageApplied);
            GTTriggers.ROTOR_HOLDER_DEATH.trigger((EntityPlayerMP) player);
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

    /**
     * returns true on only the Server
     *
     * @return whether there is a rotor in the holder
     */
    @SideOnly(Side.SERVER)
    @Override
    public boolean hasRotorServer() {
        return inventory.hasRotor();
    }

    protected void setRotorColor(int color) {
        this.rotorColor = color;
    }

    protected int getRotorColor() {
        return rotorColor;
    }

    @Override
    public boolean isRotorSpinning() {
        return isRotorSpinning;
    }

    @Override
    public int getRotorSpeed() {
        return this.currentSpeed;
    }

    @Override
    public boolean isRotorMaxSpeed() {
        return this.maxSpeed - this.currentSpeed < 2;
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
    public boolean damageRotor(int amount, boolean simulate) {
        return inventory.damageRotor(amount, simulate);
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
        if (tierDifference == -1)
            return -1;

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
        if (getController() instanceof ITieredMetaTileEntity)
            return getTier() - ((ITieredMetaTileEntity) getController()).getTier();
        return -1;
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        return onRotorHolderInteract(playerIn) || super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        return onRotorHolderInteract(playerIn) || super.onWrenchClick(playerIn, hand, facing, hitResult);
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
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, inventory);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("inventory", inventory.serializeNBT());
        data.setInteger("currentSpeed", currentSpeed);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.inventory.deserializeNBT(data.getCompoundTag("inventory"));
        this.currentSpeed = data.getInteger("currentSpeed");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isRotorSpinning);
        buf.writeInt(rotorColor);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isRotorSpinning = buf.readBoolean();
        this.rotorColor = buf.readInt();
        getHolder().scheduleChunkForRenderUpdate();
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ROTOR_HOLDER_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.LARGE_TURBINE_ROTOR_RENDERER.renderSided(renderState, translation, pipeline, getFrontFacing(),
                getController() != null, hasRotor(), isRotorSpinning, getRotorColor());
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
            scheduleRenderUpdate();
        }

        @Nullable
        private ItemStack getTurbineStack() {
            if (!hasRotor())
                return null;
            return getStackInSlot(0);
        }

        @Nullable
        private TurbineRotorBehavior2 getTurbineBehavior() {
            ItemStack stack = getStackInSlot(0);
            if (stack.isEmpty())
                return null;

            return TurbineRotorBehavior2.getInstanceFor(stack);
        }

        private boolean hasRotor() {
            return getTurbineBehavior() != null;
        }

        private int getRotorColor() {
            if (!hasRotor())
                return -1;
            //noinspection ConstantConditions
            return getTurbineBehavior().getPartMaterial(getStackInSlot(0)).getMaterialRGB();

        }

        private int getRotorEfficiency() {
            if (!hasRotor())
                return -1;

            //noinspection ConstantConditions
            return getTurbineBehavior().getRotorEfficiency(getTurbineStack());
        }

        private int getRotorPower() {
            if (!hasRotor())
                return -1;

            //noinspection ConstantConditions
            return getTurbineBehavior().getRotorPower(getTurbineStack());
        }

        private boolean damageRotor(int damageAmount, boolean simulate) {
            if (!hasRotor())
                return false;

            if (!simulate) {
                //noinspection ConstantConditions
                getTurbineBehavior().applyRotorDamage(getStackInSlot(0), damageAmount);
            }

            return true;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return TurbineRotorBehavior2.getInstanceFor(stack) != null && super.isItemValid(slot, stack);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack itemStack = super.extractItem(slot, amount, simulate);
            if (!simulate && itemStack != ItemStack.EMPTY)
                setRotorColor(-1);
            return itemStack;
        }
    }
}
