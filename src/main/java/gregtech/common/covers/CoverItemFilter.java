package gregtech.common.covers;

import gregtech.api.capability.impl.ItemHandlerDelegate;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.cover.filter.CoverWithItemFilter;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.cover.CoverRenderer;
import gregtech.client.renderer.pipe.cover.CoverRendererBuilder;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.covers.filter.BaseFilter;
import gregtech.common.covers.filter.BaseFilterContainer;
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class CoverItemFilter extends CoverBase implements CoverWithUI, CoverWithItemFilter {

    protected final String titleLocale;
    protected final SimpleOverlayRenderer texture;
    protected final ItemFilterContainer itemFilterContainer;
    protected ItemFilterMode filterMode = ItemFilterMode.FILTER_INSERT;
    protected ItemHandlerFiltered itemHandler;

    public CoverItemFilter(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                           @NotNull EnumFacing attachedSide, String titleLocale, SimpleOverlayRenderer texture) {
        super(definition, coverableView, attachedSide);
        this.titleLocale = titleLocale;
        this.texture = texture;
        this.itemFilterContainer = new ItemFilterContainer(this);
    }

    @Override
    public @NotNull ItemFilterContainer getItemFilter() {
        return itemFilterContainer;
    }

    @Override
    public ManualImportExportMode getManualMode() {
        return ManualImportExportMode.FILTERED;
    }

    @Override
    public void onAttachment(@NotNull CoverableView coverableView, @NotNull EnumFacing side,
                             @Nullable EntityPlayer player, @NotNull ItemStack itemStack) {
        super.onAttachment(coverableView, side, player, itemStack);
        var dropStack = GTUtility.copy(1, itemStack);
        this.itemFilterContainer.setFilterStack(dropStack);
    }

    @Override
    public @NotNull ItemStack getPickItem() {
        if (TooltipHelper.isCtrlDown())
            return getCoverableView().getStackForm();

        return this.itemFilterContainer.getFilterStack();
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        packetBuffer.writeBoolean(itemFilterContainer.hasFilter());
        if (itemFilterContainer.hasFilter()) {
            packetBuffer.writeByte(this.filterMode.ordinal());
            packetBuffer.writeItemStack(this.itemFilterContainer.getFilterStack());
        }
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        if (!packetBuffer.readBoolean()) return;
        this.filterMode = ItemFilterMode.VALUES[packetBuffer.readByte()];
        try {
            this.itemFilterContainer.setFilterStack(packetBuffer.readItemStack());
        } catch (IOException e) {
            GTLog.logger.error("Failed to read filter for CoverItemFilter! %s", getPos().toString());
        }
    }

    public void setFilterMode(ItemFilterMode filterMode) {
        this.filterMode = filterMode;
        getCoverableView().markDirty();
    }

    public ItemFilterMode getFilterMode() {
        return filterMode;
    }

    public @NotNull BaseFilterContainer getFilterContainer() {
        return this.itemFilterContainer;
    }

    @SuppressWarnings("DataFlowIssue") // this cover should always have a filter
    public @NotNull BaseFilter getFilter() {
        return this.itemFilterContainer.hasFilter() ?
                this.itemFilterContainer.getFilter() :
                BaseFilter.ERROR_FILTER;
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
                                                        @NotNull RayTraceResult hitResult) {
        if (!playerIn.world.isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    public boolean testItemStack(ItemStack stack) {
        return itemFilterContainer.test(stack);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager) {
        var filteringMode = new EnumSyncValue<>(ItemFilterMode.class, this::getFilterMode, this::setFilterMode);

        guiSyncManager.syncValue("filtering_mode", filteringMode);
        getFilter().getFilterReader().readStack(this.itemFilterContainer.getFilterStack());

        return getFilter().createPanel(guiSyncManager)
                .size(176, 194).padding(7)
                .child(CoverWithUI.createTitleRow(getPickItem()).left(4))
                .child(new Column().widthRel(1f).align(Alignment.TopLeft).top(22).coverChildrenHeight()
                        .child(new EnumRowBuilder<>(ItemFilterMode.class)
                                .value(filteringMode)
                                .lang("cover.filter.mode.title")
                                .overlay(16, GTGuiTextures.FILTER_MODE_OVERLAY)
                                .build())
                        .child(new Rectangle().setColor(UI_TEXT_COLOR).asWidget()
                                .height(1).widthRel(0.95f).margin(0, 4))
                        .child(getFilter().createWidgets(guiSyncManager).left(0)))
                .child(SlotGroupWidget.playerInventory().bottom(7).left(7));
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                            Cuboid6 plateBox, BlockRenderLayer layer) {
        this.texture.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    protected CoverRenderer buildRenderer() {
        return new CoverRendererBuilder(this.texture).build();
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("FilterMode", filterMode.ordinal());
        tagCompound.setTag("Filter", this.itemFilterContainer.serializeNBT());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.filterMode = ItemFilterMode.VALUES[tagCompound.getInteger("FilterMode")];
        if (tagCompound.hasKey("IsBlacklist")) {
            this.itemFilterContainer.setFilterStack(getDefinition().getDropItemStack());
            this.itemFilterContainer.handleLegacyNBT(tagCompound);
            this.itemFilterContainer.setBlacklistFilter(tagCompound.getBoolean("IsBlacklist"));
        } else {
            this.itemFilterContainer.deserializeNBT(tagCompound.getCompoundTag("Filter"));
        }
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
            if (getFilterMode() == ItemFilterMode.FILTER_EXTRACT || !itemFilterContainer.test(stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (getFilterMode() != ItemFilterMode.FILTER_INSERT) {
                ItemStack result = super.extractItem(slot, amount, true);
                if (result.isEmpty() || !itemFilterContainer.test(result)) {
                    return ItemStack.EMPTY;
                }
                return simulate ? result : super.extractItem(slot, amount, false);
            }
            return super.extractItem(slot, amount, simulate);
        }
    }
}
