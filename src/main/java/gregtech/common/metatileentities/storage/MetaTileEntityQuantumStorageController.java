package gregtech.common.metatileentities.storage;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IDualHandler;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IQuantumController;
import gregtech.api.capability.IQuantumStorage;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class MetaTileEntityQuantumStorageController extends MetaTileEntity implements IQuantumController {

    private static final int MAX_DISTANCE_RADIUS = 16;

    private IEnergyContainer energyContainer;
    /** Somewhat lazily initialized, make sure to call {@code getStorage()} before trying to access anything in this */
    private Map<BlockPos, WeakReference<IQuantumStorage<?>>> storageInstances = new HashMap<>();

    /** The "definitive" set of positions of storage instances */
    private Set<BlockPos> storagePositions = new HashSet<>();
    private long energyConsumption = 0;
    private final QuantumControllerHandler handler = new QuantumControllerHandler();

    private boolean isDead = false;

    public MetaTileEntityQuantumStorageController(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        reinitializeEnergyContainer();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityQuantumStorageController(metaTileEntityId);
    }

    @Override
    public void update() {
        if (isPowered()) {
            energyContainer.removeEnergy(energyConsumption);
        }
        super.update();
    }

    public boolean isPowered() {
        return energyContainer.getEnergyStored() >= energyConsumption;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            //todo add inactive texture for unpowered state
            if (facing == getFrontFacing()) {
                Textures.QUANTUM_CONTROLLER_FRONT.renderSided(facing, renderState, translation, pipeline);
                Textures.QUANTUM_CHEST_OVERLAY.renderSided(facing, renderState, translation, pipeline);
            } else {
                Textures.QUANTUM_CONTROLLER_ACTIVE.renderSided(facing, renderState, translation, pipeline);
            }
        }
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.QUANTUM_CONTROLLER_ACTIVE.getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @SuppressWarnings("SameParameterValue")
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
        if (mte instanceof IQuantumStorage<?>storage) {
            storageInstances.put(pos, new WeakReference<>(storage));
            return storage;
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
        handler.invalidate();
    }

    @Override
    public void onPlacement() {
        rebuildNetwork();
    }

    // Used when this controller is initially placed. Try to find all possible
    // storage instances that are connected and within our distance radius
    // todo rework this to use neighbor cache somehow
    @Override
    public void rebuildNetwork() {
        if (getWorld().isRemote) return;
        var oldInstances = storageInstances;
        var oldPositions = storagePositions;

        storageInstances = new HashMap<>();
        storagePositions = new HashSet<>();
        energyConsumption = 0;

        Queue<BlockPos> searchQueue = new LinkedList<>();
        Set<BlockPos> checked = new HashSet<>();

        // check the posses around the controller
        for (EnumFacing facing : EnumFacing.VALUES) {
            searchQueue.add(getPos().offset(facing));
        }

        // while there are blocks to search
        while (!searchQueue.isEmpty()) {
            BlockPos pos = searchQueue.remove();

            if (checked.contains(pos)) continue;
            checked.add(pos);

            if (!isInRange(pos) || !getWorld().isBlockLoaded(pos, false)) continue;

            if (getWorld().getBlockState(pos).getBlock() == Blocks.AIR) continue;
            MetaTileEntity mte = GTUtility.getMetaTileEntity(getWorld(), pos);
            if (!(mte instanceof IQuantumStorage<?>storage)) continue;

            // connected to some other network already, ignore
            if (storage.isConnected() && !storage.getControllerPos().equals(getPos())) continue;

            // valid chest/tank located, add it
            storageInstances.put(pos, new WeakReference<>(storage));
            storagePositions.add(pos);
            storage.setConnected(this);
            oldInstances.remove(pos);
            oldPositions.remove(pos);

            energyConsumption += switch (storage.getType()) {
                case ITEM, FLUID -> 8L;
                case PROXY -> 2L;
                case EXTENDER -> 1L;
                default -> 0L;
            };

            // check against already check posses so we don't recheck a checked pos
            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos offsetPos = pos.offset(facing);
                if (checked.contains(offsetPos) || getPos().equals(offsetPos)) continue;

                // add a new pos to search
                searchQueue.add(offsetPos);
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
                if (mte instanceof IQuantumStorage<?>quantumStorage) {
                    storage = quantumStorage;
                }
            }
            if (storage != null) storage.setDisconnected();
        }
        handler.rebuildCache();
        markDirty();
    }

    private void reinitializeEnergyContainer() {
        long stored = energyContainer == null ? 0L : energyContainer.getEnergyStored();

        energyContainer = EnergyContainerHandler.receiverContainer(this, this.energyConsumption * 16L,
                Integer.MAX_VALUE, Integer.MAX_VALUE);

        if (stored > 0) energyContainer.addEnergy(stored);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tagCompound = super.writeToNBT(data);
        NBTTagList list = new NBTTagList();
        for (BlockPos pos : storagePositions) {
            list.appendTag(new NBTTagLong(pos.toLong()));
        }
        tagCompound.setTag("StorageInstances", list);
        tagCompound.setLong("EnergyConsumption", energyConsumption);
        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        NBTTagList list = data.getTagList("StorageInstances", Constants.NBT.TAG_LONG);
        for (int i = 0; i < list.tagCount(); i++) {
            storagePositions.add(BlockPos.fromLong(((NBTTagLong) list.get(i)).getLong()));
        }
        energyConsumption = data.getLong("EnergyConsumption");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, EnumFacing side) {
        if (isPowered()) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && handler.hasItemHandlers()) {
                return (T) handler;
            } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && handler.hasFluidTanks()) {
                return (T) handler;
            }
        }

        // do not allow the controller to accept energy from other blocks
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER && side != null) return null;

        return super.getCapability(capability, side);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.quantum_chest.tooltip"));
    }

    @Override
    public IDualHandler getHandler() {
        return this.handler;
    }

    @Override
    public IEnergyContainer getEnergyContainer() {
        return this.energyContainer;
    }

    private class QuantumControllerHandler implements IDualHandler {

        // IFluidHandler saved values
        private FluidTankList fluidTanks;

        // IItemHandler saved values
        private ItemHandlerList itemHandlers;

        private void invalidate() {
            fluidTanks = new FluidTankList(false);
            itemHandlers = new ItemHandlerList(new ArrayList<>());
        }

        private void rebuildCache() {
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
            this.itemHandlers = new ItemHandlerList(itemHandlerList);
        }

        @Override
        public boolean hasFluidTanks() {
            return getFluidTanks().getTanks() > 0;
        }

        @Override
        public boolean hasItemHandlers() {
            return !getItemHandlers().getBackingHandlers().isEmpty();
        }

        // IFluidHandler

        protected FluidTankList getFluidTanks() {
            if (fluidTanks == null) {
                rebuildCache();
            }
            return fluidTanks;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return fluidTanks.getTankProperties();
        }

        @Override
        public int fill(FluidStack stack, boolean doFill) {
            return fluidTanks.fill(stack, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack stack, boolean doFill) {
            return fluidTanks.drain(stack, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doFill) {
            return fluidTanks.drain(maxDrain, doFill);
        }

        // IItemHandler

        protected ItemHandlerList getItemHandlers() {
            if (itemHandlers == null) {
                rebuildCache();
            }
            return itemHandlers;
        }

        @Override
        public int getSlots() {
            return getItemHandlers().getSlots();
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return itemHandlers.getStackInSlot(slot);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return itemHandlers.insertItem(slot, stack, simulate);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return itemHandlers.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return getItemHandlers().getSlotLimit(slot);
        }
    }
}
