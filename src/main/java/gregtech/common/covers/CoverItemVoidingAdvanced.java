package gregtech.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public class CoverItemVoidingAdvanced extends CoverItemVoiding {

    protected VoidingMode voidingMode;

    public CoverItemVoidingAdvanced(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        this.voidingMode = VoidingMode.VOID_ANY;
        this.itemFilterContainer.setMaxStackSize(1);
    }

    @Override
    protected void doTransferItems() {
        IItemHandler myItemHandler = coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, attachedSide);
        if (myItemHandler == null) {
            return;
        }

        switch (voidingMode) {
            case VOID_ANY:
                voidAny(myItemHandler);
            case VOID_OVERFLOW:
                voidExact(myItemHandler);
        }
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

    protected void voidExact(IItemHandler myItemHandler) {
        Map<Object, GroupItemInfo> sourceItemAmounts = countInventoryItemsByMatchSlot(myItemHandler);
        Iterator<Object> iterator = sourceItemAmounts.keySet().iterator();
        while (iterator.hasNext()) {
            Object filterSlotIndex = iterator.next();
            GroupItemInfo sourceInfo = sourceItemAmounts.get(filterSlotIndex);
            int itemToKeepAmount = itemFilterContainer.getSlotTransferLimit(sourceInfo.filterSlot, sourceInfo.itemStackTypes);

            if (sourceInfo.totalCount <= itemToKeepAmount) {
                iterator.remove();
            } else {
                int itemToVoidAmount = sourceInfo.totalCount - itemToKeepAmount;
                for (int srcIndex = 0; srcIndex < myItemHandler.getSlots(); srcIndex++) {
                    ItemStack is = myItemHandler.getStackInSlot(srcIndex);
                    if (!is.isEmpty() && itemFilterContainer.testItemStack(is)) {
                        ItemStack extracted = myItemHandler.extractItem(srcIndex, itemToVoidAmount, false);
                        if (!extracted.isEmpty()) {
                            itemToVoidAmount -= extracted.getCount();
                        }
                    }
                    if (itemToVoidAmount == 0) {
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected String getUITitle() {
        return "cover.item.voiding.advanced.title";
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup primaryGroup = new WidgetGroup();
        primaryGroup.addWidget(new LabelWidget(10, 5, getUITitle()));

        this.initFilterUI(20, primaryGroup::addWidget);

        WidgetGroup filterGroup = new WidgetGroup();
        filterGroup.addWidget(new CycleButtonWidget(91, 14, 75, 20,
                VoidingMode.class, this::getVoidingMode, this::setVoidingMode)
                .setTooltipHoverString("cover.voiding.voiding_mode.description"));

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 125 + 82)
                .widget(primaryGroup)
                .widget(filterGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 125);
        return buildUI(builder, player);
    }

    //Basically the item filter container GUI code, with different Y widget positioning
    public void initFilterUI(int y, Consumer<Widget> widgetGroup) {
        widgetGroup.accept(new LabelWidget(10, y, "cover.conveyor.item_filter.title"));
        widgetGroup.accept(new SlotWidget(itemFilterContainer.getFilterInventory(), 0, 10, y + 15)
                .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));

        ServerWidgetGroup stackSizeGroup = new ServerWidgetGroup(itemFilterContainer::showGlobalTransferLimitSlider);
        stackSizeGroup.addWidget(new ImageWidget(111, 34, 35, 20, GuiTextures.DISPLAY));

        stackSizeGroup.addWidget(new IncrementButtonWidget(146, 34, 20, 20, 1, 8, 64, 512, itemFilterContainer::adjustTransferStackSize)
                .setDefaultTooltip()
                .setTextScale(0.7f)
                .setShouldClientCallback(false));
        stackSizeGroup.addWidget(new IncrementButtonWidget(91, 34, 20, 20, -1, -8, -64, -512, itemFilterContainer::adjustTransferStackSize)
                .setDefaultTooltip()
                .setTextScale(0.7f)
                .setShouldClientCallback(false));

        stackSizeGroup.addWidget(new TextFieldWidget2(113, 41, 31, 20, () -> String.valueOf(itemFilterContainer.getTransferStackSize()), val -> {
                    if (val != null && !val.isEmpty())
                        itemFilterContainer.setTransferStackSize(Integer.parseInt(val));
                })
                        .setCentered(true)
                        .setAllowedChars("0123456789")
                        .setMaxLength(4)
                        .setValidator(itemFilterContainer.getTextFieldValidator(() -> Integer.MAX_VALUE))
                        .setScale(0.9f)
        );


        widgetGroup.accept(stackSizeGroup);

        this.itemFilterContainer.getFilterWrapper().initUI(y + 38, widgetGroup);
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.ITEM_VOIDING_ADVANCED.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    public void setVoidingMode(VoidingMode voidingMode) {
        this.voidingMode = voidingMode;
        this.itemFilterContainer.setMaxStackSize(voidingMode.maxStackSize);
        this.coverHolder.markDirty();
    }

    public VoidingMode getVoidingMode() {
        return voidingMode;
    }

}
