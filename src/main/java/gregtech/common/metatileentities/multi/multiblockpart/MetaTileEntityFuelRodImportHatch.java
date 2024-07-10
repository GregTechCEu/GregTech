package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IFuelRodHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.items.itemhandlers.LockableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IFissionReactorHatch;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.nuclear.fission.components.FuelRod;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.blocks.BlockFissionCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import java.io.IOException;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.LOCK_UPDATE;

public class MetaTileEntityFuelRodImportHatch extends MetaTileEntityMultiblockNotifiablePart
                                              implements IMultiblockAbilityPart<IFuelRodHandler>, IFuelRodHandler,
                                              IControllable, IFissionReactorHatch {

    private boolean workingEnabled;
    private Material mat;
    public MetaTileEntityFuelRodExportHatch pairedHatch;
    private Material partialFuel;
    private FuelRod internalFuelRod;

    public MetaTileEntityFuelRodImportHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 4, false);
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.workingEnabled = isWorkingAllowed;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFuelRodImportHatch(metaTileEntityId);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new LockableItemStackHandler(this, false);
    }

    private ModularUI.Builder createUITemplate(EntityPlayer player) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 143)
                .label(10, 5, getMetaFullName());

        builder.widget(new SlotWidget(importItems, 0, 79, 18, true, true)
                .setBackgroundTexture(GuiTextures.SLOT));

        return builder.bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 60);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && getOffsetTimer() % 5 == 0) {
            pullItemsFromNearbyHandlers(getFrontFacing());
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createUITemplate(entityPlayer).build(getHolder(), entityPlayer);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = isExportHatch ? Textures.PIPE_OUT_OVERLAY : Textures.PIPE_IN_OVERLAY;
            renderer.renderSided(getFrontFacing(), renderState, translation, pipeline);
            SimpleOverlayRenderer overlay = isExportHatch ? Textures.ITEM_HATCH_OUTPUT_OVERLAY :
                    Textures.ITEM_HATCH_INPUT_OVERLAY;
            overlay.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public MultiblockAbility<IFuelRodHandler> getAbility() {
        return MultiblockAbility.IMPORT_FUEL_ROD;
    }

    @Override
    public void registerAbilities(List<IFuelRodHandler> abilityList) {
        abilityList.add(this);
    }

    @Override
    public boolean checkValidity(int depth) {
        this.pairedHatch = getExportHatch(depth);
        return pairedHatch != null;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        getLockedImport().setLock(data.getBoolean("locked"));
        if (data.hasKey("partialFuel"))
            this.partialFuel = GregTechAPI.materialManager.getMaterial(data.getString("partialFuel"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setBoolean("locked", getLockedImport().isLocked());
        if (partialFuel != null)
            data.setString("partialFuel", this.partialFuel.toString());
        return super.writeToNBT(data);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeItemStack(getLockedImport().getStackInSlot(0));
        buf.writeBoolean(getLockedImport().isLocked());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        try {
            getLockedImport().setStackInSlot(0, buf.readItemStack());
        } catch (IOException e) { // ignored
        }
        getLockedImport().setLock(buf.readBoolean());
    }

    private LockableItemStackHandler getLockedImport() {
        return (LockableItemStackHandler) importItems;
    }

    @Override
    public void setLock(boolean isLocked) {
        getLockedImport().setLock(isLocked);
        writeCustomData(LOCK_UPDATE, (packetBuffer -> {
            packetBuffer.writeBoolean(isLocked);
        }));
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == LOCK_UPDATE) {
            getLockedImport().setLock(buf.readBoolean());
        }
    }

    @Override
    public boolean isLocked() {
        return getLockedImport().isLocked();
    }

    @Override
    public ItemStack getLockedObject() {
        return getLockedImport().getLockedObject();
    }

    @Override
    public Material getFuel() {
        return this.mat;
    }

    @Override
    public void setFuel(Material material) {
        this.mat = material;
    }

    @Override
    public Material getPartialFuel() {
        return this.partialFuel;
    }

    @Override
    public boolean setPartialFuel(Material material) {
        if (partialFuel != null && partialFuel.equals(material)) {
            return false;
        }
        if (!material.hasProperty(PropertyKey.FISSION_FUEL)) {
            return false;
        }
        this.partialFuel = material;
        if (this.internalFuelRod != null) {
            this.internalFuelRod.setFuel(partialFuel.getProperty(PropertyKey.FISSION_FUEL));
        }
        return true;
    }

    @Override
    public void setInternalFuelRod(FuelRod rod) {
        this.internalFuelRod = rod;
    }

    @Override
    public LockableItemStackHandler getStackHandler() {
        return this.getLockedImport();
    }

    public MetaTileEntityFuelRodExportHatch getExportHatch(int depth) {
        BlockPos pos = this.getPos();
        for (int i = 1; i < depth; i++) {
            if (getWorld().getBlockState(pos.offset(this.frontFacing.getOpposite(), i)) !=
                    MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.FUEL_CHANNEL)) {
                return null;
            }
        }
        if (getWorld().getTileEntity(pos.offset(this.frontFacing.getOpposite(), depth)) instanceof IGregTechTileEntity gtTe) {
            MetaTileEntity mte = gtTe.getMetaTileEntity();
            if (mte instanceof MetaTileEntityFuelRodExportHatch) {
                return (MetaTileEntityFuelRodExportHatch) mte;
            }
        }
        return null;
    }
}
