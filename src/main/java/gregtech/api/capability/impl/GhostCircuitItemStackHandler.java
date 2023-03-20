package gregtech.api.capability.impl;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GhostCircuitItemStackHandler implements IItemHandlerModifiable, INotifiableHandler {

    public static final int NO_CONFIG = -1;

    private final List<MetaTileEntity> notifiableEntities = new ArrayList<>();

    private int circuitConfig = NO_CONFIG;
    private ItemStack circuitStack = ItemStack.EMPTY;

    public int getCircuitConfig() {
        return this.circuitConfig;
    }

    public boolean hasCircuitConfig() {
        return this.circuitConfig != NO_CONFIG;
    }

    public void setCircuitConfig(int config) {
        if (config < IntCircuitIngredient.CIRCUIT_MIN || config > IntCircuitIngredient.CIRCUIT_MAX)
            config = NO_CONFIG;
        if (this.circuitConfig == config) return;
        this.circuitConfig = config;
        this.circuitStack = config == NO_CONFIG ? ItemStack.EMPTY : IntCircuitIngredient.getIntegratedCircuit(config);
        for (MetaTileEntity mte : notifiableEntities) {
            if (mte != null && mte.isValid()) {
                addToNotifiedList(mte, this, false);
            }
        }
    }

    public void setCircuitConfigFromStack(@Nonnull ItemStack stack) {
        setCircuitConfig(!stack.isEmpty() && IntCircuitIngredient.isIntegratedCircuit(stack) ?
                IntCircuitIngredient.getCircuitConfiguration(stack) : NO_CONFIG);
    }

    public void addCircuitConfig(int configDelta) {
        if (hasCircuitConfig()) {
            setCircuitConfig(MathHelper.clamp(getCircuitConfig() + configDelta,
                    IntCircuitIngredient.CIRCUIT_MIN, IntCircuitIngredient.CIRCUIT_MAX));
        }
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        setCircuitConfigFromStack(stack);
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.circuitStack;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return stack; // reject all item insertions
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!simulate) {
            setCircuitConfig(NO_CONFIG);
        }
        return this.circuitStack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        if (metaTileEntity == null) return;
        this.notifiableEntities.add(metaTileEntity);
    }

    @Override
    public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntities.remove(metaTileEntity);
    }

    public void write(@Nonnull NBTTagCompound tag) {
        if (this.circuitConfig != NO_CONFIG) {
            tag.setByte("GhostCircuit", (byte) this.circuitConfig);
        }
    }

    public void read(@Nonnull NBTTagCompound tag) {
        setCircuitConfig(tag.hasKey("GhostCircuit", Constants.NBT.TAG_ANY_NUMERIC) ? tag.getInteger("GhostCircuit") : NO_CONFIG);
    }
}
