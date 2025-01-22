package gregtech.common.metatileentities.steam.multiblockpart;

import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.sync.GTFluidSyncHandler;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;
import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntitySteamHatch extends MetaTileEntityMultiblockPart
                                      implements IMultiblockAbilityPart<IFluidTank> {

    private static final int INVENTORY_SIZE = 64000;
    private static final boolean IS_STEEL = ConfigHolder.machines.steelSteamMultiblocks;

    public MetaTileEntitySteamHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 0);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySteamHatch(metaTileEntityId);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("ContainerInventory")) {
            MetaTileEntityQuantumTank.legacyTankItemHandlerNBTReading(this, data.getCompoundTag("ContainerInventory"),
                    0, 1);
        }
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            fillContainerFromInternalTank(importFluids);
            fillInternalTankFromFluidContainer();
            pullFluidsFromNearbyHandlers(getFrontFacing());
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = Textures.PUMP_OVERLAY;
            renderer.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        MultiblockControllerBase controller = getController();
        if (controller == null)
            return IS_STEEL ? Textures.STEAM_CASING_STEEL : Textures.STEAM_CASING_BRONZE;
        return controller.getBaseTexture(this);
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, new FilteredFluidHandler(INVENTORY_SIZE)
                .setFilter(CommonFluidFilters.STEAM));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new FilteredItemHandler(this, 1).setFillPredicate(
                FilteredItemHandler.getCapabilityFilter(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY));
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this, 1);
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return MultiblockAbility.STEAM;
    }

    @Override
    public void registerAbilities(List<IFluidTank> abilityList) {
        abilityList.addAll(this.importFluids.getFluidTanks());
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        guiSyncManager.registerSlotGroup("item_inv", 1);

        GTFluidSyncHandler tankSyncHandler = new GTFluidSyncHandler(this.importFluids.getTankAt(0));

        return GTGuis.createPanel(this, 176, 166)
                .background(IS_STEEL ? GTGuiTextures.BACKGROUND_STEEL : GTGuiTextures.BACKGROUND_BRONZE)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(SlotGroupWidget
                        .playerInventory((index, slot) -> slot
                                .background(IS_STEEL ? GTGuiTextures.SLOT_STEEL : GTGuiTextures.SLOT_BRONZE))
                        .left(7).bottom(7))
                .child((IS_STEEL ? GTGuiTextures.DISPLAY_STEEL : GTGuiTextures.DISPLAY_BRONZE).asWidget()
                        .left(7).top(16)
                        .size(81, 55))
                .child(GTGuiTextures.TANK_ICON.asWidget()
                        .left(92).top(36)
                        .size(14, 15))
                .child(IKey.lang("gregtech.gui.fluid_amount").color(0xFFFFFF).asWidget().pos(11, 20))
                .child(IKey.dynamic(() -> getFluidAmountFormatted(tankSyncHandler))
                        .color(0xFFFFFF)
                        .asWidget().pos(11, 30))
                .child(IKey.dynamic(() -> getFluidNameTranslated(tankSyncHandler))
                        .color(0xFFFFFF)
                        .asWidget().pos(11, 40))
                .child(new GTFluidSlot().syncHandler(tankSyncHandler)
                        .pos(69, 52))
                .child(new ItemSlot().slot(SyncHandlers.itemSlot(this.importItems, 0)
                        .slotGroup("item_inv")
                        .filter(itemStack -> FluidUtil.getFluidHandler(itemStack) != null))
                        .background(IS_STEEL ? GTGuiTextures.SLOT_STEEL : GTGuiTextures.SLOT_BRONZE,
                                IS_STEEL ? GTGuiTextures.IN_SLOT_OVERLAY_STEEL : GTGuiTextures.IN_SLOT_OVERLAY_BRONZE)
                        .pos(90, 16))
                .child(new ItemSlot().slot(SyncHandlers.itemSlot(this.exportItems, 0)
                        .accessibility(false, true))
                        .background(IS_STEEL ? GTGuiTextures.SLOT_STEEL : GTGuiTextures.SLOT_BRONZE,
                                IS_STEEL ? GTGuiTextures.OUT_SLOT_OVERLAY_STEEL : GTGuiTextures.OUT_SLOT_OVERLAY_BRONZE)
                        .pos(90, 53));
    }

    private String getFluidNameTranslated(GTFluidSyncHandler tankSyncHandler) {
        if (tankSyncHandler.getFluid() == null) {
            return "";
        } else {
            return tankSyncHandler.getFluid().getLocalizedName();
        }
    }

    private String getFluidAmountFormatted(GTFluidSyncHandler tankSyncHandler) {
        if (tankSyncHandler.getFluid() == null) {
            return "0";
        } else {
            return TextFormattingUtil.formatNumbers(tankSyncHandler.getFluid().amount);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", INVENTORY_SIZE));
        tooltip.add(I18n.format("gregtech.machine.steam.steam_hatch.tooltip"));
    }
}
