package gregtech.common.metatileentities.storage;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IDualHandler;
import gregtech.api.capability.IQuantumController;
import gregtech.api.capability.IQuantumStorage;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
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
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

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
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.SOLID_STEEL_CASING.getParticleSprite(), getPaintingColorForRendering());
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
        handler.invalidate();
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
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && handler.hasItemHandlers()) {
            return (T) handler;
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && handler.hasFluidTanks()) {
            return (T) handler;
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.quantum_chest.tooltip"));
    }

    @Override
    public IDualHandler getHandler() {
        return this.handler;
    }

    private class QuantumControllerHandler implements IDualHandler {

        // IFluidHandler saved values
        private FluidTankList fluidTanks;

        // IItemHandler saved values
        private ItemHandlerList itemHandlers;

//        private int insertIndex = -1;
//        private int extractIndex = -1;

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

        private FluidTankList getFluidTanks() {
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

        private ItemHandlerList getItemHandlers() {
            if (itemHandlers == null) {
                rebuildCache();
            }
            return itemHandlers;
        }

        @Override
        public int getSlots() {
            // gotta lie to GTTransferUtils
            return itemHandlers.getSlots();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            // make GTTransferUtils always think it can insert items
            return itemHandlers.getStackInSlot(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return itemHandlers.insertItem(slot, stack, simulate);

//            // check item handlers if the stack to insert matches a stack already in the item handler
//            if (simulate) {
//                ItemStack checkStack;
//                boolean canInsert = false;
//                for (int i = 0; i < getItemHandlers().getSlots(); i++) {
//                    IItemHandler handler = getItemHandlers().get(i);
//                    // try to get a stack in output slot
//                    checkStack = handler.getStackInSlot(2);
//
//                    // if the check stack is empty, or equal to the incoming stack
//                    if (checkStack.isItemEqual(stack) || checkStack.isEmpty()) {
//                        canInsert = true;
//                        this.insertIndex = i;
//                        GTTransferUtils.insertItem(itemHandlers, stack, simulate) = handler.insertItem(1, GTTransferUtils.insertItem(itemHandlers, stack, simulate), true);
//                        if (GTTransferUtils.insertItem(itemHandlers, stack, simulate).isEmpty() || GTTransferUtils.insertItem(itemHandlers, stack, simulate).getCount() < stack.getCount()) break;
//                    }
//                }
//                if (!canInsert) this.insertIndex = -1;
//            } else if (this.insertIndex != -1) {
//                GTTransferUtils.insertItem(itemHandlers, stack, simulate) = getItemHandlers().get(this.insertIndex).insertItem(1, GTTransferUtils.insertItem(itemHandlers, stack, simulate), false);
//            }
//
//            return GTTransferUtils.insertItem(itemHandlers, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return itemHandlers.extractItem(slot, amount, simulate);

//            ItemStack extracted = ItemStack.EMPTY;
//
//            if (simulate){
//                boolean canExtract = false;
//                for (int i = 0; i < getItemHandlers().size(); i++) {
//                    IItemHandler handler = getItemHandlers().get(i);
//                    extracted = handler.extractItem(0, amount, true);
//                    if (!extracted.isEmpty()){
//                        canExtract = true;
//                        this.extractIndex = i;
//                        break;
//                    }
//                }
//                if (!canExtract) this.extractIndex = -1;
//            } else if (this.extractIndex != -1) {
//                extracted = getItemHandlers().get(this.extractIndex).extractItem(0, amount, false);
//            }
//
//            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            return getItemHandlers().getSlotLimit(slot);
        }
    }

}
