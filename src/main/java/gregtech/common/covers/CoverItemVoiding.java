package gregtech.common.covers;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;

public class CoverItemVoiding extends CoverConveyor {

    protected final NullItemHandler nullItemHandler = new NullItemHandler();

    public CoverItemVoiding(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                            @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide, 0, Integer.MAX_VALUE);
        this.isWorkingAllowed = false;
    }

    @Override
    public void update() {
        if (isWorkingAllowed && getCoverableView().getOffsetTimer() % 20 == 0) {
            doTransferItems();
        }
    }

    protected void doTransferItems() {
        IItemHandler myItemHandler = getCoverableView().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                getAttachedSide());
        if (myItemHandler == null) {
            return;
        }
        voidAny(myItemHandler);
    }

    void voidAny(IItemHandler myItemHandler) {
        for (int srcIndex = 0; srcIndex < myItemHandler.getSlots(); srcIndex++) {
            ItemStack sourceStack = myItemHandler.extractItem(srcIndex, Integer.MAX_VALUE, true);
            if (sourceStack.isEmpty()) {
                continue;
            }
            if (!itemFilterContainer.testItemStack(sourceStack)) {
                continue;
            }
            myItemHandler.extractItem(srcIndex, Integer.MAX_VALUE, false);
        }
    }

    @Override
    protected String getUITitle() {
        return "cover.item.voiding.title";
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup primaryGroup = new WidgetGroup();
        primaryGroup.addWidget(new LabelWidget(10, 5, getUITitle()));
        this.itemFilterContainer.initUI(20, primaryGroup::addWidget);

        primaryGroup
                .addWidget(new CycleButtonWidget(10, 92 + 23, 80, 18, this::isWorkingEnabled, this::setWorkingEnabled,
                        "cover.voiding.label.disabled", "cover.voiding.label.enabled")
                                .setTooltipHoverString("cover.voiding.tooltip"));

        primaryGroup.addWidget(new CycleButtonWidget(10, 112 + 23, 116, 18,
                ManualImportExportMode.class, this::getManualImportExportMode, this::setManualImportExportMode)
                        .setTooltipHoverString("cover.universal.manual_import_export.mode.description"));

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 125 + 82 + 16 + 24)
                .widget(primaryGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 125 + 16 + 24);
        return builder.build(this, player);
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                            Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.ITEM_VOIDING.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public @NotNull EnumActionResult onSoftMalletClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                       @NotNull CuboidRayTraceResult hitResult) {
        this.isWorkingAllowed = !this.isWorkingAllowed;
        if (!playerIn.world.isRemote) {
            playerIn.sendStatusMessage(new TextComponentTranslation(isWorkingEnabled() ?
                    "cover.voiding.message.enabled" : "cover.voiding.message.disabled"), true);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(nullItemHandler);
        }
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return defaultValue;
    }

    class NullItemHandler implements IItemHandler {

        @Override
        public int getSlots() {
            return 9;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!itemFilterContainer.testItemStack(stack)) {
                return stack;
            }
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }
    }
}
