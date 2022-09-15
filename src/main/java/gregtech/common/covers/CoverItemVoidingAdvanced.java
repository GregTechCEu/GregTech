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
import gregtech.api.util.ItemStackKey;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

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
                break;
            case VOID_OVERFLOW:
                voidOverflow(myItemHandler);
        }
    }

    protected void voidOverflow(IItemHandler myItemHandler) {
        Map<ItemStackKey, TypeItemInfo> itemTypeCount = countInventoryItemsByType(myItemHandler);
        for (TypeItemInfo typeItemInfo : itemTypeCount.values()) {

            int itemToVoidAmount = 0;
            if (getItemFilterContainer().getFilterWrapper().getItemFilter() == null) {
                itemToVoidAmount = typeItemInfo.totalCount - itemFilterContainer.getTransferStackSize();
            } else {
                if (itemFilterContainer.testItemStack(typeItemInfo.itemStack)) {
                    Object matchedSlot = itemFilterContainer.matchItemStack(typeItemInfo.itemStack);
                    itemToVoidAmount = typeItemInfo.totalCount - itemFilterContainer.getSlotTransferLimit(matchedSlot);
                }
            }

            if (itemToVoidAmount <= 0) {
                continue;
            }

            for (int srcIndex = 0; srcIndex < myItemHandler.getSlots(); srcIndex++) {
                ItemStack is = myItemHandler.getStackInSlot(srcIndex);
                if (!is.isEmpty() && ItemStack.areItemsEqual(is, typeItemInfo.itemStack) && ItemStack.areItemStackTagsEqual(is, typeItemInfo.itemStack)) {
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

        ServerWidgetGroup stackSizeGroup = new ServerWidgetGroup(() -> itemFilterContainer.getFilterWrapper().getItemFilter() == null && voidingMode == VoidingMode.VOID_OVERFLOW);
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
                        itemFilterContainer.setTransferStackSize(MathHelper.clamp(Integer.parseInt(val), 1, voidingMode.maxStackSize));
                })
                        .setCentered(true)
                        .setNumbersOnly(1, Integer.MAX_VALUE)
                        .setMaxLength(10)
                        .setScale(0.9f)
        );

        widgetGroup.accept(stackSizeGroup);

        this.itemFilterContainer.getFilterWrapper().initUI(y + 38, widgetGroup);

        this.itemFilterContainer.getFilterWrapper().blacklistUI(y + 38, widgetGroup, () -> voidingMode != VoidingMode.VOID_OVERFLOW);
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

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("VoidMode", voidingMode.ordinal());

        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.voidingMode = VoidingMode.values()[tagCompound.getInteger("VoidMode")];
    }
}
