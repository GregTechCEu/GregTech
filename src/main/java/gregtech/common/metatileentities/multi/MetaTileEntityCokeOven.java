package gregtech.common.metatileentities.multi;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipes.CokeOvenRecipe;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Predicate;

public class MetaTileEntityCokeOven extends MultiblockControllerBase {

    private int maxProgressDuration;
    private int currentProgress;
    private ItemStack outputStack;
    private FluidStack outputFluid;
    private boolean isActive;
    private boolean wasActiveAndNeedUpdate;

    private ItemStack lastInputStack = ItemStack.EMPTY;
    private CokeOvenRecipe previousRecipe;

    public MetaTileEntityCokeOven(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        if (maxProgressDuration == 0) {
            if (tryPickNewRecipe()) {
                if (wasActiveAndNeedUpdate) {
                    this.wasActiveAndNeedUpdate = false;
                } else setActive(true);
            }
        } else if (++currentProgress >= maxProgressDuration) {
            finishCurrentRecipe();
            this.wasActiveAndNeedUpdate = true;
            return;
        }
        if (wasActiveAndNeedUpdate) {
            this.wasActiveAndNeedUpdate = false;
            setActive(false);
        }
    }

    private void finishCurrentRecipe() {
        this.maxProgressDuration = 0;
        this.currentProgress = 0;
        ItemHandlerHelper.insertItemStacked(exportItems, outputStack, false);
        this.exportFluids.fill(outputFluid, true);
        markDirty();
    }

    private CokeOvenRecipe getOrRefreshRecipe(ItemStack inputStack) {
        CokeOvenRecipe currentRecipe = null;
        if (previousRecipe != null &&
            previousRecipe.getInput().getIngredient().apply(inputStack)) {
            currentRecipe = previousRecipe;
        } else if (!areItemStacksEqual(inputStack, lastInputStack)) {
            this.lastInputStack = inputStack.isEmpty() ? ItemStack.EMPTY : inputStack.copy();
            currentRecipe = RecipeMaps.COKE_OVEN_RECIPES.stream()
                .filter(it -> it.getInput().getIngredient().test(inputStack))
                .findFirst().orElse(null);
            if (currentRecipe != null) {
                this.previousRecipe = currentRecipe;
            }
        }
        return currentRecipe;
    }

    private static boolean areItemStacksEqual(ItemStack stackA, ItemStack stackB) {
        return (stackA.isEmpty() && stackB.isEmpty()) ||
            (ItemStack.areItemsEqual(stackA, stackB) &&
                ItemStack.areItemStackTagsEqual(stackA, stackB));
    }

    private boolean setupRecipe(ItemStack inputStack, CokeOvenRecipe recipe) {
        return inputStack.getCount() >= recipe.getInput().getCount() &&
            ItemHandlerHelper.insertItemStacked(exportItems, recipe.getOutput(), true).isEmpty() &&
            exportFluids.fill(recipe.getFluidOutput(), false) == recipe.getFluidOutput().amount;
    }

    private boolean tryPickNewRecipe() {
        ItemStack inputStack = importItems.getStackInSlot(0);
        if (inputStack.isEmpty()) {
            return false;
        }
        CokeOvenRecipe currentRecipe = getOrRefreshRecipe(inputStack);
        if (currentRecipe != null && setupRecipe(inputStack, currentRecipe)) {
            inputStack.shrink(currentRecipe.getInput().getCount());
            this.maxProgressDuration = currentRecipe.getDuration();
            this.currentProgress = 0;
            this.outputStack = currentRecipe.getOutput().copy();
            this.outputFluid = currentRecipe.getFluidOutput().copy();
            markDirty();
            return true;
        }
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("Active", isActive);
        data.setBoolean("WasActive", wasActiveAndNeedUpdate);
        data.setInteger("MaxProgress", maxProgressDuration);
        if (maxProgressDuration > 0) {
            data.setInteger("Progress", currentProgress);
            data.setTag("OutputItem", outputStack.writeToNBT(new NBTTagCompound()));
            data.setTag("OutputFluid", outputFluid.writeToNBT(new NBTTagCompound()));
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isActive = data.getBoolean("Active");
        this.wasActiveAndNeedUpdate = data.getBoolean("WasActive");
        this.maxProgressDuration = data.getInteger("MaxProgress");
        if (maxProgressDuration > 0) {
            this.currentProgress = data.getInteger("Progress");
            this.outputStack = new ItemStack(data.getCompoundTag("OutputItem"));
            this.outputFluid = FluidStack.loadFluidStackFromNBT(data.getCompoundTag("OutputFluid"));
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 100) {
            this.isActive = buf.readBoolean();
            getWorld().checkLight(getPos());
            getHolder().scheduleChunkForRenderUpdate();
        }
    }

    public void setActive(boolean active) {
        this.isActive = active;
        if (!getWorld().isRemote) {
            writeCustomData(100, b -> b.writeBoolean(isActive));
            getWorld().checkLight(getPos());
        }
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public int getLightValueForPart(IMultiblockPart sourcePart) {
        return sourcePart == null && isActive ? 15 : 0;
    }

    public double getProgressScaled() {
        return maxProgressDuration == 0 ? 0.0 : (currentProgress / (maxProgressDuration * 1.0));
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.COKE_BRICKS);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.COKE_BRICKS;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.COKE_OVEN_OVERLAY.render(renderState, translation, pipeline, getFrontFacing(), isActive());
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
            capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, new FluidTank(32000));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        Predicate<BlockWorldState> hatchPredicate = tilePredicate((state, tile) -> tile instanceof MetaTileEntityCokeOvenHatch);
        return FactoryBlockPattern.start()
            .aisle("XXX", "XZX", "XXX")
            .aisle("XZX", "Z#Z", "XZX")
            .aisle("XXX", "XYX", "XXX")
            .where('Z', statePredicate(getCasingState()).or(hatchPredicate))
            .where('X', statePredicate(getCasingState()))
            .where('#', isAirPredicate())
            .where('Y', selfPredicate())
            .build();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityCokeOven(metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 166)
            .widget(new SlotWidget(importItems, 0, 33, 30, true, true)
                .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FURNACE_OVERLAY))
            .progressBar(this::getProgressScaled, 58, 30, 20, 15, GuiTextures.BRONZE_BLAST_FURNACE_PROGRESS_BAR, MoveType.HORIZONTAL)
            .widget(new SlotWidget(exportItems, 0, 85, 30, true, false)
                .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FURNACE_OVERLAY))
            .widget(new TankWidget(exportFluids.getTankAt(0), 133, 13, 20, 58)
                .setBackgroundTexture(GuiTextures.FLUID_TANK_BACKGROUND)
                .setOverlayTexture(GuiTextures.FLUID_TANK_OVERLAY)
                .setContainerClicking(true, false))
            .bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 0)
            .build(getHolder(), entityPlayer);
    }
}
