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
import com.cleanroommc.modularui.common.widget.*;
import com.cleanroommc.modularui.common.widget.textfield.TextFieldWidget;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiFunctions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.util.ItemStackKey;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Map;

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
    public ModularWindow createWindow(UIBuildContext buildContext) {
        return ModularWindow.builder(176, 149)
                .bindPlayerInventory(buildContext.getPlayer())
                .setBackground(GuiTextures.VANILLA_BACKGROUND)
                .widget(new TextWidget(Text.localised(getUITitle()))
                        .setPos(10, 5))
                .widget(new Column()
                        .widget(new TextWidget(Text.localised("cover.voiding.voiding_mode.name"))
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(80, 12))
                        .widget(new TextWidget(Text.localised("cover.voiding.voiding_amount.name"))
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(80, 12))
                        .setPos(7, 18)
                        .setSize(80, 24))
                .widget(new Column()
                        .widget(new CycleButtonWidget()
                                .setForEnum(VoidingMode.class, this::getVoidingMode, this::setVoidingMode)
                                .setTextureGetter(GuiFunctions.enumStringTextureGetter(VoidingMode.class))
                                .addTooltip(0, Text.localised(VoidingMode.values()[0].localeTooltip))
                                .addTooltip(1, Text.localised(VoidingMode.values()[1].localeTooltip))
                                .setBackground(GuiTextures.BASE_BUTTON)
                                .setSize(80, 12))
                        .widget(new Row()
                                .widget(new ButtonWidget()
                                        .setOnClick(GuiFunctions.getIncrementer(-1, -8, -64, -512, filterHolder::adjustTransferStackSize))
                                        .addTooltip(Text.localised("modularui.decrement.tooltip", 1, 8, 64, 512))
                                        .setBackground(GuiTextures.BASE_BUTTON, new Text("-").color(0xFFFFFF))
                                        .setSize(12, 12))
                                .widget(new TextFieldWidget()
                                        .setGetterInt(filterHolder::getTransferStackSize)
                                        .setSetterInt(val -> filterHolder.setTransferStackSize(MathHelper.clamp(val, 1, voidingMode.maxStackSize)))
                                        .setNumbers(1, Integer.MAX_VALUE)
                                        .setTextAlignment(Alignment.Center)
                                        .setTextColor(0xFFFFFF)
                                        .setBackground(GuiTextures.DISPLAY_SMALL)
                                        .setSize(56, 12))
                                .widget(new ButtonWidget()
                                        .setOnClick(GuiFunctions.getIncrementer(1, 8, 64, 512, filterHolder::adjustTransferStackSize))
                                        .addTooltip(Text.localised("modularui.increment.tooltip", 1, 8, 64, 512))
                                        .setBackground(GuiTextures.BASE_BUTTON, new Text("+").color(0xFFFFFF))
                                        .setSize(12, 12))
                                .setTicker(this::checkShowLimitSlider)
                                .setPos(7, 20))
                        .setPos(89, 18)
                        .setSize(80, 24))
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
