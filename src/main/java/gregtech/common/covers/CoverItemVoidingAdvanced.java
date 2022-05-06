package gregtech.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.widget.ButtonWidget;
import com.cleanroommc.modularui.common.widget.CycleButtonWidget;
import com.cleanroommc.modularui.common.widget.Row;
import com.cleanroommc.modularui.common.widget.TextWidget;
import com.cleanroommc.modularui.common.widget.textfield.TextFieldWidget;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiFunctions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.guiOld.ModularUI;
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
        this.filterHolder.setMaxStackSize(1);
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
            if (getFilter() == null) {
                itemToVoidAmount = typeItemInfo.totalCount - filterHolder.getTransferStackSize();
            } else {
                if (filterHolder.test(typeItemInfo.itemStack)) {
                    Object matchedSlot = filterHolder.matchItemStack(typeItemInfo.itemStack);
                    itemToVoidAmount = typeItemInfo.totalCount - filterHolder.getSlotTransferLimit(matchedSlot);
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
        /*gregtech.api.guiOld.widgets.WidgetGroup primaryGroup = new gregtech.api.guiOld.widgets.WidgetGroup();
        primaryGroup.addWidget(new gregtech.api.guiOld.widgets.LabelWidget(10, 5, getUITitle()));

        this.initFilterUI(20, primaryGroup::addWidget);

        gregtech.api.guiOld.widgets.WidgetGroup filterGroup = new gregtech.api.guiOld.widgets.WidgetGroup();
        filterGroup.addWidget(new gregtech.api.guiOld.widgets.CycleButtonWidget(91, 14, 75, 20,
                VoidingMode.class, this::getVoidingMode, this::setVoidingMode)
                .setTooltipHoverString("cover.voiding.voiding_mode.description"));

        ModularUI.Builder builder = ModularUI.builder(gregtech.api.guiOld.GuiTextures.BACKGROUND, 176, 125 + 82)
                .widget(primaryGroup)
                .widget(filterGroup)
                .bindPlayerInventory(player.inventory, gregtech.api.guiOld.GuiTextures.SLOT, 7, 125);
        return buildUI(builder, player);*/
        return null;
    }

    //Basically the item filter container GUI code, with different Y widget positioning
    public void initFilterUI(int y, Consumer<Widget> widgetGroup) {
        /*widgetGroup.accept(new gregtech.api.guiOld.widgets.LabelWidget(10, y, "cover.conveyor.item_filter.title"));
        widgetGroup.accept(new gregtech.api.guiOld.widgets.SlotWidget(filterHolder.getFilterInventory(), 0, 10, y + 15)
                .setBackgroundTexture(gregtech.api.guiOld.GuiTextures.SLOT, gregtech.api.guiOld.GuiTextures.FILTER_SLOT_OVERLAY));

        gregtech.api.guiOld.widgets.ServerWidgetGroup stackSizeGroup = new gregtech.api.guiOld.widgets.ServerWidgetGroup(() -> getFilter() == null && voidingMode == VoidingMode.VOID_OVERFLOW);
        stackSizeGroup.addWidget(new gregtech.api.guiOld.widgets.ImageWidget(111, 34, 35, 20, gregtech.api.guiOld.GuiTextures.DISPLAY));

        stackSizeGroup.addWidget(new gregtech.api.guiOld.widgets.IncrementButtonWidget(146, 34, 20, 20, 1, 8, 64, 512, filterHolder::adjustTransferStackSize)
                .setDefaultTooltip()
                .setTextScale(0.7f)
                .setShouldClientCallback(false));
        stackSizeGroup.addWidget(new gregtech.api.guiOld.widgets.IncrementButtonWidget(91, 34, 20, 20, -1, -8, -64, -512, filterHolder::adjustTransferStackSize)
                .setDefaultTooltip()
                .setTextScale(0.7f)
                .setShouldClientCallback(false));

        stackSizeGroup.addWidget(new gregtech.api.guiOld.widgets.TextFieldWidget2(113, 41, 31, 20, () -> String.valueOf(filterHolder.getTransferStackSize()), val -> {
                    if (val != null && !val.isEmpty())
                        filterHolder.setTransferStackSize(MathHelper.clamp(Integer.parseInt(val), 1, voidingMode.maxStackSize));
                })
                        .setCentered(true)
                        .setNumbersOnly(1, Integer.MAX_VALUE)
                        .setMaxLength(10)
                        .setScale(0.9f)
        );

        widgetGroup.accept(stackSizeGroup);*/

        //this.itemFilterContainer.getFilterWrapper().initUI(y + 38, widgetGroup);

        //this.itemFilterContainer.getFilterWrapper().blacklistUI(y + 38, widgetGroup, () -> voidingMode != VoidingMode.VOID_OVERFLOW);
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        return ModularWindow.builder(176, 166)
                .bindPlayerInventory(buildContext.getPlayer())
                .setBackground(GuiTextures.BACKGROUND)
                .widget(new TextWidget(Text.localised(getUITitle()))
                        .setPos(10, 5))
                .widget(new CycleButtonWidget()
                        .setForEnum(VoidingMode.class, this::getVoidingMode, this::setVoidingMode)
                        .setTextureGetter(GuiFunctions.enumStringTextureGetter(VoidingMode.class))
                        .setBackground(GuiTextures.BASE_BUTTON)
                        .setPos(91, 16)
                        .setSize(75, 20))
                .widget(new Row()
                        .widget(new ButtonWidget()
                                .setOnClick(GuiFunctions.getIncrementer(1, 8, 64, 512, filterHolder::adjustTransferStackSize))
                                .setBackground(gregtech.api.gui.GuiTextures.BASE_BUTTON, new Text("+").color(0xFFFFFF))
                                .setSize(12, 12))
                        .widget(new TextFieldWidget()
                                .setGetterInt(filterHolder::getTransferStackSize)
                                .setSetterInt(val -> filterHolder.setTransferStackSize(MathHelper.clamp(val, 1, voidingMode.maxStackSize)))
                                .setNumbers(1, Integer.MAX_VALUE)
                                .setTextAlignment(Alignment.Center)
                                .setTextColor(0xFFFFFF)
                                .setBackground(gregtech.api.gui.GuiTextures.DISPLAY_SMALL)
                                .setSize(56, 12))
                        .widget(new ButtonWidget()
                                .setOnClick(GuiFunctions.getIncrementer(-1, -8, -64, 512, filterHolder::adjustTransferStackSize))
                                .setBackground(gregtech.api.gui.GuiTextures.BASE_BUTTON, new Text("-").color(0xFFFFFF))
                                .setSize(12, 12))
                        .setTicker(this::checkShowLimitSlider)
                        .setPos(7, 20))
                .widget(filterHolder.createFilterUI(buildContext)
                        .setPos(7, 42))
                .build();
    }

    private void checkShowLimitSlider(Widget widget) {
        boolean show = getFilter() == null && voidingMode == VoidingMode.VOID_OVERFLOW;
        if (show != widget.isEnabled()) {
            widget.setEnabled(show);
        }
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.ITEM_VOIDING_ADVANCED.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    public void setVoidingMode(VoidingMode voidingMode) {
        this.voidingMode = voidingMode;
        this.filterHolder.setMaxStackSize(voidingMode.maxStackSize);
        this.coverHolder.markDirty();
    }

    public VoidingMode getVoidingMode() {
        return voidingMode;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("VoidMode", voidingMode.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.voidingMode = VoidingMode.values()[tagCompound.getInteger("VoidMode")];
    }
}
