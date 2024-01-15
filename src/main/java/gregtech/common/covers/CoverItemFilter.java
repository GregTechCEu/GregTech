package gregtech.common.covers;

import gregtech.api.capability.impl.ItemHandlerDelegate;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.covers.filter.FilterTypeRegistry;
import gregtech.common.covers.filter.ItemFilter;
import gregtech.common.covers.filter.ItemFilterContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class CoverItemFilter extends CoverBase implements CoverWithUI {

    protected final String titleLocale;
    protected final SimpleOverlayRenderer texture;
    protected final ItemFilterContainer itemFilter;
    protected ItemFilterMode filterMode = ItemFilterMode.FILTER_INSERT;
    protected ItemHandlerFiltered itemHandler;

    public CoverItemFilter(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                           @NotNull EnumFacing attachedSide, String titleLocale, SimpleOverlayRenderer texture) {
        super(definition, coverableView, attachedSide);
        this.titleLocale = titleLocale;
        this.texture = texture;
        this.itemFilter = new ItemFilterContainer(this);
    }

    @Override
    public void onAttachment(@NotNull CoverableView coverableView, @NotNull EnumFacing side,
                             @Nullable EntityPlayer player, @NotNull ItemStack itemStack) {
        super.onAttachment(coverableView, side, player, itemStack);
        this.itemFilter.setItemFilter(FilterTypeRegistry.getItemFilterForStack(itemStack.copy()));
        this.itemFilter.setMaxStackSize(1);
    }

    @Override
    public @NotNull ItemStack getPickItem() {
        return this.getItemFilter() == null ? super.getPickItem() : this.getItemFilter().getContainerStack();
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        packetBuffer.writeBoolean(itemFilter.hasItemFilter());
        if (itemFilter.hasItemFilter()) {
            packetBuffer.writeItemStack(getItemFilter().getContainerStack());
        }
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        if (!packetBuffer.readBoolean()) return;
        try {
            this.itemFilter.setItemFilter(FilterTypeRegistry.getItemFilterForStack(packetBuffer.readItemStack()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFilterMode(ItemFilterMode filterMode) {
        this.filterMode = filterMode;
        getCoverableView().markDirty();
    }

    public ItemFilterMode getFilterMode() {
        return filterMode;
    }

    public ItemFilter getItemFilter() {
        return this.itemFilter.getItemFilter();
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return coverable.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getAttachedSide()) != null;
    }

    @Override
    public boolean canPipePassThrough() {
        return true;
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        if (!playerIn.world.isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    public boolean testItemStack(ItemStack stack) {
        return itemFilter.testItemStack(stack);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        gregtech.api.gui.widgets.WidgetGroup filterGroup = new gregtech.api.gui.widgets.WidgetGroup();
        filterGroup.addWidget(new gregtech.api.gui.widgets.LabelWidget(10, 5, titleLocale));
        filterGroup.addWidget(new gregtech.api.gui.widgets.CycleButtonWidget(10, 20, 110, 20,
                GTUtility.mapToString(ItemFilterMode.values(), it -> it.localeName),
                () -> filterMode.ordinal(), (newMode) -> setFilterMode(ItemFilterMode.values()[newMode])));
        this.itemFilter.initFilterUI(45, filterGroup::addWidget);
        this.itemFilter.blacklistUI(45, filterGroup::addWidget, () -> true);
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 105 + 82)
                .widget(filterGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 105)
                .build(this, player);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, GuiSyncManager guiSyncManager) {
        var filteringMode = new EnumSyncValue<>(ItemFilterMode.class, this::getFilterMode, this::setFilterMode);

        guiSyncManager.syncValue("filtering_mode", filteringMode);

        return getItemFilter().createPanel(guiSyncManager)
                .size(176, 194).padding(7)
                .child(CoverWithUI.createTitleRow(getPickItem()).left(4))
                .child(new Column().widthRel(1f).align(Alignment.TopLeft).top(22).coverChildrenHeight()
                        .child(new Row().coverChildrenHeight()
                                .widthRel(1f).left(0)
                                .child(createFilterModeButton(filteringMode, ItemFilterMode.FILTER_INSERT))
                                .child(createFilterModeButton(filteringMode, ItemFilterMode.FILTER_EXTRACT))
                                .child(createFilterModeButton(filteringMode, ItemFilterMode.FILTER_BOTH))
                                .child(IKey.str("Filter Mode").asWidget().align(Alignment.CenterRight)))
                        .child(new Rectangle().setColor(UI_TEXT_COLOR).asWidget()
                                .height(1).widthRel(0.95f).margin(0, 4))
                        .child(getItemFilter().createWidgets(guiSyncManager).left(0)))
                .child(SlotGroupWidget.playerInventory(0).bottom(7).left(7));
    }

    private Widget<ToggleButton> createFilterModeButton(EnumSyncValue<ItemFilterMode> value, ItemFilterMode mode) {
        return new ToggleButton().size(18)
                .value(boolValueOf(value, mode))
                .background(GTGuiTextures.MC_BUTTON_DISABLED)
                .selectedBackground(GTGuiTextures.MC_BUTTON)
                .marginRight(2)
//                .overlay(GTGuiTextures.MANUAL_IO_OVERLAY[mode.ordinal()]) todo new overlays
                .addTooltipLine(switch (mode) {
                    case FILTER_INSERT -> IKey.lang("cover.universal.manual_import_export.mode.disabled");
                    case FILTER_EXTRACT -> IKey.lang("cover.universal.manual_import_export.mode.unfiltered");
                    case FILTER_BOTH -> IKey.lang("cover.universal.manual_import_export.mode.filtered");
                });
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                            Cuboid6 plateBox, BlockRenderLayer layer) {
        this.texture.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("FilterMode", filterMode.ordinal());
        tagCompound.setTag("Filter", getItemFilter().getContainerStack().serializeNBT());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.filterMode = ItemFilterMode.values()[tagCompound.getInteger("FilterMode")];
        var stack = new ItemStack(tagCompound.getCompoundTag("Filter"));
        this.itemFilter.setItemFilter(FilterTypeRegistry.getItemFilterForStack(stack));
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, T defaultValue) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (defaultValue == null) {
                return null;
            }
            IItemHandler delegate = (IItemHandler) defaultValue;
            if (itemHandler == null || itemHandler.delegate != delegate) {
                this.itemHandler = new ItemHandlerFiltered(delegate);
            }
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
        }
        return defaultValue;
    }

    private class ItemHandlerFiltered extends ItemHandlerDelegate {

        public ItemHandlerFiltered(IItemHandler delegate) {
            super(delegate);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (getFilterMode() == ItemFilterMode.FILTER_EXTRACT || !itemFilter.testItemStack(stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (getFilterMode() != ItemFilterMode.FILTER_INSERT) {
                ItemStack result = super.extractItem(slot, amount, true);
                if (result.isEmpty() || !itemFilter.testItemStack(result)) {
                    return ItemStack.EMPTY;
                }
                return simulate ? result : super.extractItem(slot, amount, false);
            }
            return super.extractItem(slot, amount, simulate);
        }
    }
}
