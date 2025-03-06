package gregtech.common.covers.ender;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.virtualregistry.EntryTypes;
import gregtech.api.util.virtualregistry.VirtualChest;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.filter.ItemFilterContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoverEnderItemLink extends CoverAbstractEnderLink<VirtualChest> {

    protected final ItemFilterContainer container;
    @Nullable
    private IPanelHandler chestPanel;

    public CoverEnderItemLink(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                              @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
        container = new ItemFilterContainer(this);
    }

    private ModularPanel createChestPanel(ModularPanel parentPanel, EntityPlayer player) {
        return GTGuis.createPopupPanel(getDefinition().getResourceLocation().getPath(), 100, 100);
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
        IPanelHandler panelHandler = getChestPanel(panel);
        return new ButtonWidget<>()
                .onMousePressed(mouseButton -> {
                    if (panelHandler.isPanelOpen()) {
                        panelHandler.closePanel();
                    } else {
                        panelHandler.openPanel();
                    }
                    return true;
                });
    }

    @NotNull
    private IPanelHandler getChestPanel(ModularPanel modularPanel) {
        if (chestPanel == null) {
            chestPanel = IPanelHandler.simple(modularPanel, this::createChestPanel, true);
        }
        return chestPanel;
    }

    @Override
    protected IWidget createSlotWidget(VirtualChest entry) {
        return null;
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
        GTTransferUtils.moveInventoryItems(handler, this.activeEntry, this.container::test);
    }
}
