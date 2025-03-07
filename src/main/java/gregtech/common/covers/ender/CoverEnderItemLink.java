package gregtech.common.covers.ender;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.virtualregistry.EntryTypes;
import gregtech.api.util.virtualregistry.VirtualChest;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.CoverConveyor.ConveyorMode;
import gregtech.common.covers.filter.ItemFilterContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;

public class CoverEnderItemLink extends CoverAbstractEnderLink<VirtualChest> {

    private static final IDrawable CHEST = new ItemDrawable(new ItemStack(Blocks.CHEST)).asIcon();
    protected final ItemFilterContainer container;
    private ConveyorMode conveyorMode = ConveyorMode.IMPORT;

    public CoverEnderItemLink(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                              @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
        container = new ItemFilterContainer(this);
    }

    @Override
    protected EntryTypes<VirtualChest> getType() {
        return EntryTypes.ENDER_ITEM;
    }

    @Override
    protected String identifier() {
        return "EILink#";
    }

    @Override
    protected IWidget createEntrySlot(ModularPanel panel, PanelSyncManager syncManager) {
        IPanelHandler panelHandler = IPanelHandler.simple(panel, this::createChestPanel, true);
        return new ButtonWidget<>()
                // todo lang
                .addTooltipLine(IKey.str("Open Active Entry View"))
                .overlay(CHEST)
                .onMousePressed(mouseButton -> {
                    if (panelHandler.isPanelOpen()) {
                        panelHandler.deleteCachedPanel();
                        panelHandler.closePanel();
                    } else {
                        panelHandler.openPanel();
                    }
                    return true;
                });
    }

    private ModularPanel createChestPanel(ModularPanel parentPanel, EntityPlayer player) {
        return createChestPanel("active", parentPanel, player);
    }

    private ModularPanel createChestPanel(String color, ModularPanel parentPanel, EntityPlayer player) {
        IntFunction<ItemStack> getStack = activeEntry::getStackInSlot;
        return GTGuis.createPopupPanel("chest_panel#" + color, 100, 100)
                .padding(4)
                .paddingRight(16)
                .coverChildren()
                .child(new Grid().coverChildren()
                        .mapTo(3, activeEntry.getSlots(), value -> {
                            var item = new ItemDrawable(); // todo this doesn't draw amount until next mui2 version
                            // fake item slot because i don't want to deal with syncing
                            return new Widget<>()
                                    .size(18)
                                    .background(GTGuiTextures.SLOT)
                                    .tooltipAutoUpdate(true)
                                    .tooltipBuilder(tooltip -> {
                                        ItemStack stack = getStack.apply(value);
                                        if (stack.isEmpty()) return;
                                        tooltip.addFromItem(stack);
                                        tooltip.add(IKey.lang("gregtech.item_list.item_stored", stack.getCount()));
                                    })
                                    .overlay(new DynamicDrawable(() -> item.setItem(getStack.apply(value)))
                                            .asIcon()
                                            .alignment(Alignment.Center)
                                            .size(16));
                        }));
    }

    @Override
    protected IWidget createSlotWidget(VirtualChest entry, ModularPanel panel, PanelSyncManager syncManager) {
        IPanelHandler panelHandler = IPanelHandler.simple(panel,
                (parentPanel, player) -> createChestPanel(entry.getColorStr(), parentPanel, player), true);
        return new ButtonWidget<>()
                // todo lang
                .addTooltipLine(IKey.str("Open Entry [#%s]'s View", entry.getColorStr()))
                .overlay(CHEST)
                .onMousePressed(mouseButton -> {
                    if (panelHandler.isPanelOpen()) {
                        panelHandler.deleteCachedPanel();
                        panelHandler.closePanel();
                    } else {
                        panelHandler.openPanel();
                    }
                    return true;
                });
    }

    @Override
    protected Flow createWidgets(ModularPanel modularPanel, PanelSyncManager syncManager) {
        getItemFilterContainer().setMaxTransferSize(1);

        var conveyorMode = new EnumSyncValue<>(ConveyorMode.class, this::getConveyorMode, this::setConveyorMode);
        syncManager.syncValue("conveyor_mode", conveyorMode);

        return super.createWidgets(modularPanel, syncManager)
                .child(getItemFilterContainer().initUI(modularPanel, syncManager))
                .child(new EnumRowBuilder<>(ConveyorMode.class)
                        .value(conveyorMode)
                        .overlay(GTGuiTextures.CONVEYOR_MODE_OVERLAY)
                        .lang("cover.generic.io")
                        .build());
    }

    public ConveyorMode getConveyorMode() {
        return conveyorMode;
    }

    private void setConveyorMode(ConveyorMode mode) {
        this.conveyorMode = mode;
    }

    public ItemFilterContainer getItemFilterContainer() {
        return container;
    }

    @Override
    protected boolean shouldDeleteEntry(VirtualChest activeEntry) {
        for (int i = 0; i < activeEntry.getSlots(); i++) {
            if (!activeEntry.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return coverable.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            @NotNull IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox,
                            @NotNull BlockRenderLayer layer) {
        // todo new texture
        Textures.ENDER_FLUID_LINK.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public void onRemoval() {
        dropInventoryContents(this.container);
    }

    @Override
    public void update() {
        if (isWorkingEnabled() && isIoEnabled()) {
            transferItems();
        }
    }

    private void transferItems() {
        IItemHandler handler = getCoverableView().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                getAttachedSide());
        if (handler == null) return;
        if (getConveyorMode().isImport()) {
            GTTransferUtils.moveInventoryItems(handler, this.activeEntry, this.container::test);
        } else {
            GTTransferUtils.moveInventoryItems(this.activeEntry, handler, this.container::test);
        }
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("ConveyorMode", this.conveyorMode.ordinal());
        nbt.setTag("Filter", this.container.serializeNBT());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.conveyorMode = ConveyorMode.VALUES[nbt.getInteger("ConveyorMode")];
        this.container.deserializeNBT(nbt.getCompoundTag("Filter"));
    }
}
