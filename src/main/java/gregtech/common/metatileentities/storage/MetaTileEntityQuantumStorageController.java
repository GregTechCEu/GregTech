package gregtech.common.metatileentities.storage;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IQuantumController;
import gregtech.api.capability.IQuantumStorage;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

public class MetaTileEntityQuantumStorageController extends MetaTileEntity implements IQuantumController {

    private static final int MAX_DISTANCE_RADIUS = 16;

    // somewhat lazily initialized, make sure to call checkStoragePos() before trying to access anything in this
    private Map<BlockPos, WeakReference<IQuantumStorage<?>>> storageInstances = new HashMap<>();

    // the "definitive" set of positions of storage instances
    private Set<BlockPos> storagePositions = new HashSet<>();

    private final QuantumControllerHandler handler = new QuantumControllerHandler();

    private boolean isDead = false;

    public MetaTileEntityQuantumStorageController(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityQuantumStorageController(metaTileEntityId);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.SOLID_STEEL_CASING.render(renderState, translation, pipeline);
        Textures.QUANTUM_CHEST_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    private IQuantumStorage<?> getStorage(BlockPos pos, boolean rebuild) {
        if (getWorld().isRemote) return null;
        if (storageInstances.containsKey(pos)) {
            WeakReference<IQuantumStorage<?>> storageRef = storageInstances.get(pos);
            IQuantumStorage<?> storage = storageRef.get();
            if (storage != null) {
                return storage;
            }
        }
        // need to make sure it still exists
        MetaTileEntity mte = GTUtility.getMetaTileEntity(getWorld(), pos);
        if (mte instanceof IQuantumStorage) {
            WeakReference<IQuantumStorage<?>> storageRef = new WeakReference<>((IQuantumStorage<?>) mte);
            storageInstances.put(pos, storageRef);
            return (IQuantumStorage<?>) mte;
        } else if (rebuild) {
            // need to remove and rebuild
            storagePositions.remove(pos);
            rebuildNetwork();
            return null;
        }
        return null;
    }

    @Override
    public boolean canConnect(IQuantumStorage<?> storage) {
        return !isDead && isInRange(storage.getPos());
    }

    private boolean isInRange(BlockPos pos) {
        boolean isXValid = Math.abs(getPos().getX() - pos.getX()) <= MAX_DISTANCE_RADIUS;
        boolean isYValid = Math.abs(getPos().getY() - pos.getY()) <= MAX_DISTANCE_RADIUS;
        boolean isZValid = Math.abs(getPos().getZ() - pos.getZ()) <= MAX_DISTANCE_RADIUS;
        return isXValid && isYValid && isZValid;
    }

    @Override
    public void onRemoval() {
        if (getWorld().isRemote) return;
        isDead = true;
        for (BlockPos pos : storagePositions) {
            IQuantumStorage<?> storage = getStorage(pos, false);
            if (storage != null) storage.setDisconnected();
        }
    }

    @Override
    public void onPlacement() {
        rebuildNetwork();
    }

    // Used when this controller is initially placed. Try to find all possible
    // storage instances that are connected and within our distance radius
    @Override
    public void rebuildNetwork() {
        if (getWorld().isRemote) return;
        Map<BlockPos, WeakReference<IQuantumStorage<?>>> oldInstances = storageInstances;
        Set<BlockPos> oldPositions = storagePositions;

        storageInstances = new HashMap<>();
        storagePositions = new HashSet<>();

        Queue<BlockPos> searchQueue = new LinkedList<>();
        Set<BlockPos> checked = new HashSet<>();

        // check the posses around the controller
        for (EnumFacing facing : EnumFacing.VALUES) {
            searchQueue.add(getPos().offset(facing));
        }

        // while there are blocks to search
        while (!searchQueue.isEmpty()) {
            BlockPos pos = searchQueue.remove();
            checked.add(pos);

            if (!isInRange(pos) || !getWorld().isBlockLoaded(pos, false)) continue;

            if (getWorld().getBlockState(pos).getBlock() == Blocks.AIR) continue;
            MetaTileEntity mte = GTUtility.getMetaTileEntity(getWorld(), pos);
            if (!(mte instanceof IQuantumStorage<?> storage)) continue;

            // connected to some other network already, ignore
            if (storage.isConnected() && !storage.getControllerPos().equals(getPos())) continue;

            // valid chest/tank located, add it
            storageInstances.put(pos, new WeakReference<>(storage));
            storagePositions.add(pos);
            storage.setConnected(this);
            oldInstances.remove(pos);
            oldPositions.remove(pos);

            // check against already check posses so we don't recheck a checked pos
            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos offsetPos = pos.offset(facing);
                if (!checked.contains(offsetPos) && !getPos().equals(offsetPos)) {

                    // add a new pos to search
                    searchQueue.add(offsetPos);
                }
            }
        }

        // check old posses to disconnect the storages
        for (BlockPos pos : oldPositions) {

            // if we already checked this pos before, don't check it again
            if (checked.contains(pos)) continue;

            // if the pos is air, there's nothing to check
            if (getWorld().getBlockState(pos).getBlock() == Blocks.AIR) continue;

            IQuantumStorage<?> storage = null;
            if (oldInstances.containsKey(pos)) {
                storage = oldInstances.get(pos).get();
            } else {
                MetaTileEntity mte = GTUtility.getMetaTileEntity(getWorld(), pos);
                if (mte instanceof IQuantumStorage<?> quantumStorage) {
                    storage = quantumStorage;
                }
            }
            if (storage != null) storage.setDisconnected();
        }
        handler.rebuildCache();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tagCompound = super.writeToNBT(data);
        NBTTagList list = new NBTTagList();
        for (BlockPos pos : storagePositions) {
            list.appendTag(new NBTTagLong(pos.toLong()));
        }
        tagCompound.setTag("StorageInstances", list);
        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        NBTTagList list = data.getTagList("StorageInstances", Constants.NBT.TAG_LONG);
        for (int i = 0; i < list.tagCount(); i++) {
            storagePositions.add(BlockPos.fromLong(((NBTTagLong) list.get(i)).getLong()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
            capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
        ) {
            return (T) handler;
        }
        return super.getCapability(capability, side);
    }

    private final class QuantumControllerHandler implements IItemHandler, IFluidHandler {

        // IFluidHandler saved values
        private FluidTankList fluidTanks = null;

        // IItemHandler saved values
        private List<IItemHandler> itemHandlers = null;

        int lastSlot = -1;

        private void invalidate() {
            fluidTanks = null;
            itemHandlers = null;
        }

        private void rebuildCache() {
            this.invalidate();

            List<IItemHandler> itemHandlerList = new ArrayList<>();
            List<IFluidTank> fluidTankList = new ArrayList<>();
            for (BlockPos pos : storagePositions) {
                IQuantumStorage<?> storage = getStorage(pos, false);
                if (storage == null) continue;
                switch (storage.getType()) {
                    case ITEM -> itemHandlerList.add((IItemHandler) storage.getTypeValue());
                    case FLUID -> fluidTankList.add((IFluidTank) storage.getTypeValue());
                }
            }

            // todo allow this "allowSameFluidFill" to be configured in this controller?
            this.fluidTanks = new FluidTankList(false, fluidTankList);
            this.itemHandlers = itemHandlerList;
        }

        // IFluidHandler

        private FluidTankList getFluidTanks() {
            if (fluidTanks == null) {
                rebuildCache();
            }
            return fluidTanks;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return getFluidTanks().getTankProperties();
        }

        @Override
        public int fill(FluidStack stack, boolean doFill) {
            return getFluidTanks().fill(stack, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack stack, boolean doFill) {
            return getFluidTanks().drain(stack, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doFill) {
            return getFluidTanks().drain(maxDrain, doFill);
        }

        // IItemHandler

        private List<IItemHandler> getItemHandlers() {
            if (itemHandlers == null) {
                rebuildCache();
            }
            return itemHandlers;
        }

        @Override
        public int getSlots() {
            if (this.lastSlot != -1) {
                return 1;
            } else {
                return getItemHandlers().size();
            }
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (this.lastSlot != -1) {
                return getItemHandlers().get(this.lastSlot).getStackInSlot(0);
            } else {
                return getItemHandlers().get(slot).getStackInSlot(0);
            }
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (getItemHandlers().isEmpty()) return stack;
            ItemStack remainder = stack;

            if (this.lastSlot != -1) {
                remainder = getItemHandlers().get(this.lastSlot).insertItem(0, remainder, simulate);
                if (!remainder.isEmpty() && remainder.getCount() == stack.getCount()) this.lastSlot = -1;
            } else {
                for (IItemHandler handler : getItemHandlers()) {
                    remainder = handler.insertItem(0, remainder, simulate);
                    this.lastSlot++;
                    if (remainder.isEmpty()) return remainder;
                }
            }

            return remainder;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack extracted = ItemStack.EMPTY;
            if (this.lastSlot != -1) {
                extracted = getItemHandlers().get(this.lastSlot).extractItem(1, amount, simulate);
                if (extracted.isEmpty()) this.lastSlot = -1;
            } else {
                for (IItemHandler handler : getItemHandlers()) {
                    extracted = handler.extractItem(0, amount, simulate);
                    this.lastSlot++;
                    if (!extracted.isEmpty()) return extracted;
                }
            }
            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot >= getItemHandlers().size()) return 0;
            return getItemHandlers().get(slot).getSlotLimit(0);
        }
    }
}
