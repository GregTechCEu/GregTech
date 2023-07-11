package gregtech.common.metatileentities.storage;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.ImmutableList;
import gregtech.api.capability.IQuantumController;
import gregtech.api.capability.IQuantumStorage;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
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
import org.apache.commons.lang3.ArrayUtils;

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
    public void postBreakBlock() {
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

        BlockPos u = getPos().up(), d = getPos().down(), n = getPos().north(), s = getPos().south(), e = getPos().east(), w = getPos().west();
        Queue<BlockPos> searchQueue = new LinkedList<>(ImmutableList.of(u, d, n, s, e, w));
        Set<BlockPos> discovered = new HashSet<>(ImmutableList.of(u, d, n, s, e, w));

        while (!searchQueue.isEmpty()) {
            BlockPos pos = searchQueue.remove();

            if (!isInRange(pos)) continue;
            if (!getWorld().isBlockLoaded(pos, false)) continue;

//            TileEntity te = getWorld().getTileEntity(pos);
//            if (!(te instanceof IGregTechTileEntity) || te.isInvalid()) continue;
//            MetaTileEntity mte = ((IGregTechTileEntity) te).getMetaTileEntity();
            MetaTileEntity mte = GTUtility.getMetaTileEntity(getWorld(), pos);
            if (!(mte instanceof IQuantumStorage<?> storage)) continue;

            // connected to some other network already, ignore
            if (storage.isConnected() && !storage.getControllerPos().equals(getPos())) continue;
            storageInstances.put(pos, new WeakReference<>(storage));
            storagePositions.add(pos);
            storage.setConnected(this);
            oldInstances.remove(pos);
            oldPositions.remove(pos);


            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos offsetPos = pos.offset(facing);
                if (!discovered.contains(offsetPos)) {
                    searchQueue.add(offsetPos);
                    discovered.add(offsetPos);
                }
            }
        }

        for (BlockPos pos : oldPositions) {
            IQuantumStorage<?> storage = null;
            if (oldInstances.containsKey(pos)) {
                storage = oldInstances.get(pos).get();
            } else {
                MetaTileEntity mte = GTUtility.getMetaTileEntity(getWorld(), pos);
                if (mte instanceof IQuantumStorage) {
                    storage = (IQuantumStorage<?>) mte;
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
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) handler;
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) handler;
        }
        return super.getCapability(capability, side);
    }

    private final class QuantumControllerHandler implements IItemHandler, IFluidHandler {

        // IFluidHandler saved values
        private FluidTankList fluidTanks = null;

        // IItemHandler saved values
        private List<IItemHandler> itemHandlers = null;

        private void invalidate() {
            fluidTanks = null;
            itemHandlers = null;
        }

        private void rebuildCache() {
            List<IItemHandler> itemHandlerList = new ArrayList<>();
            List<IFluidTank> fluidTankList = new ArrayList<>();
            for (BlockPos pos : storagePositions) {
                IQuantumStorage<?> storage = getStorage(pos, false);
                if (storage == null) continue;
                switch (storage.getType()) {
                    case ITEM:
                        itemHandlerList.add((IItemHandler) storage.getTypeValue());
                        break;
                    case FLUID:
                        fluidTankList.add((IFluidTank) storage.getTypeValue());
                        break;
                }
            }
            // todo allow this "allowSameFluidFill" to be configured in this controller?
            fluidTanks = new FluidTankList(false, fluidTankList);
            itemHandlers = itemHandlerList;
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
            return getItemHandlers().size();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot >= getSlots()) return ItemStack.EMPTY;
            return getItemHandlers().get(slot).getStackInSlot(0);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (getItemHandlers().isEmpty()) return stack;
            ItemStack remainder = stack;
            int lastSlot = slot;
            do {
                IItemHandler handler = getItemHandlers().get(slot);
                remainder = handler.insertItem(0, remainder, simulate);
                if (remainder == ItemStack.EMPTY) break;

                slot++;
                if (slot >= getItemHandlers().size()) slot = 0;
            } while (slot != lastSlot);
            return remainder;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot >= getItemHandlers().size()) return ItemStack.EMPTY;
            return getItemHandlers().get(slot).extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot >= getItemHandlers().size()) return 0;
            return getItemHandlers().get(slot).getSlotLimit(0);
        }
    }
}
