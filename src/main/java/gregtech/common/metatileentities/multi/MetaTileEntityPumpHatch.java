package gregtech.common.metatileentities.multi;

import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.sync.GTFluidSyncHandler;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;
import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
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
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityPumpHatch extends MetaTileEntityMultiblockPart
                                     implements IMultiblockAbilityPart<IFluidTank> {

    private static final int FLUID_TANK_SIZE = 1000;

    public MetaTileEntityPumpHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 0);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityPumpHatch(metaTileEntityId);
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
            pushFluidsIntoNearbyHandlers(getFrontFacing());
            fillContainerFromInternalTank();
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay())
            Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, new FluidTank(FLUID_TANK_SIZE));
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
        return MultiblockAbility.PUMP_FLUID_HATCH;
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        return Textures.PRIMITIVE_PUMP;
    }

    @Override
    public void registerAbilities(
                                  @NotNull MultiblockAbility<IFluidTank> multiblockAbility,
                                  @NotNull List<IFluidTank> abilities) {
        abilities.add(exportFluids.getTankAt(0));
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        guiSyncManager.registerSlotGroup("item_inv", 2);

        GTFluidSyncHandler tankSyncHandler = GTFluidSlot.sync(this.exportFluids.getTankAt(0))
                .showAmount(false)
                .accessibility(true, false);

        // TODO: Change the position of the name when it's standardized.
        return GTGuis.createPanel(this, 176, 166)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(SlotGroupWidget.playerInventory().left(7).bottom(7))
                .child(GTGuiTextures.DISPLAY.asWidget()
                        .left(7).top(16)
                        .size(81, 55))
                .child(GTGuiTextures.TANK_ICON.asWidget()
                        .left(92).top(36)
                        .size(14, 15))
                .child(new RichTextWidget()
                        .size(75, 47)
                        .pos(10, 20)
                        .textColor(Color.WHITE.main)
                        .alignment(Alignment.TopLeft)
                        .autoUpdate(true)
                        .textBuilder(richText -> {
                            richText.addLine(IKey.lang("gregtech.gui.fluid_amount"));
                            String name = tankSyncHandler.getFluidLocalizedName();
                            if (name == null) return;

                            richText.addLine(IKey.str(name));
                            richText.addLine(IKey.str(tankSyncHandler.getFormattedFluidAmount()));
                        }))
                .child(new GTFluidSlot().syncHandler(tankSyncHandler)
                        .pos(69, 52)
                        .disableBackground())
                .child(new ItemSlot().slot(SyncHandlers.itemSlot(this.importItems, 0)
                        .slotGroup("item_inv")
                        .filter(itemStack -> FluidUtil.getFluidHandler(itemStack) != null))
                        .background(GTGuiTextures.SLOT, GTGuiTextures.IN_SLOT_OVERLAY)
                        .pos(90, 16))
                .child(new ItemSlot().slot(SyncHandlers.itemSlot(this.exportItems, 0)
                        .slotGroup("item_inv")
                        .accessibility(false, true))
                        .background(GTGuiTextures.SLOT, GTGuiTextures.OUT_SLOT_OVERLAY)
                        .pos(90, 53));
    }

    @Override
    public GTGuiTheme getUITheme() {
        return GTGuiTheme.PRIMITIVE;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", FLUID_TANK_SIZE));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }
}
