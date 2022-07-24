package gregtech.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.common.widget.TextWidget;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class CoverItemVoiding extends CoverConveyor {

    protected final NullItemHandler nullItemHandler = new NullItemHandler();

    public CoverItemVoiding(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide, 0, Integer.MAX_VALUE);
        this.isWorkingAllowed = false;
    }

    @Override
    public void update() {
        long timer = coverHolder.getOffsetTimer();
        if (isWorkingAllowed && timer % 20 == 0) {
            doTransferItems();
        }
    }

    protected void doTransferItems() {
        IItemHandler myItemHandler = coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, attachedSide);
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
            if (!this.filterHolder.test(sourceStack)) {
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
    public ModularWindow createWindow(UIBuildContext buildContext) {
        return ModularWindow.builder(176, 126)
                .bindPlayerInventory(buildContext.getPlayer())
                .setBackground(GuiTextures.VANILLA_BACKGROUND)
                .widget(new TextWidget(Text.localised(getUITitle()))
                        .setPos(10, 5))
                .widget(filterHolder.createFilterUI(buildContext)
                        .setPos(7, 20))
                .build();
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.ITEM_VOIDING.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
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

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (!filterHolder.test(stack)) {
                return stack;
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
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
