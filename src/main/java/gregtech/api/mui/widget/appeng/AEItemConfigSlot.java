package gregtech.api.mui.widget.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.storage.data.IAEItemStack;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class AEItemConfigSlot extends AEConfigSlot<IAEItemStack> implements Interactable,
                              JeiGhostIngredientSlot<ItemStack>,
                              JeiIngredientProvider {

    public AEItemConfigSlot(ExportOnlyAEItemList itemList, int index) {
        super(itemList.getInventory()[index], itemList.isStocking());
        setSyncHandler(new AEItemConfigSyncHandler(backingSlot));
        size(18, 18);
    }

    @Override
    public void onInit() {
        super.onInit();
        getContext().getJeiSettings().addJeiGhostIngredientSlot(this);
    }

    @Override
    public @NotNull AEItemConfigSyncHandler getSyncHandler() {
        return (AEItemConfigSyncHandler) super.getSyncHandler();
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof AEItemConfigSyncHandler;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (ModularUIJeiPlugin.hasDraggingGhostIngredient() || ModularUIJeiPlugin.hoveringOverIngredient(this)) {
            GlStateManager.colorMask(true, true, true, false);
            drawHighlight(getArea(), isHovering());
            GlStateManager.colorMask(true, true, true, true);
        }
    }

    @Override
    public void setGhostIngredient(@NotNull ItemStack ingredient) {
        getSyncHandler().sendJEIDrop(ingredient);
    }

    @Override
    public @Nullable ItemStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return ingredient instanceof ItemStack stack ? stack : null;
    }

    @Override
    public @Nullable Object getIngredient() {
        return backingSlot.getConfig();
    }

    public static class AEItemConfigSyncHandler extends SyncHandler {

        private final IConfigurableSlot<IAEItemStack> config;

        public AEItemConfigSyncHandler(IConfigurableSlot<IAEItemStack> config) {
            this.config = config;
        }

        public void sendJEIDrop(ItemStack stack) {
            syncToServer(jeiDropSyncID, buf -> ByteBufUtils.writeTag(buf, stack.serializeNBT()));
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) throws IOException {}

        @Override
        public void readOnServer(int id, PacketBuffer buf) throws IOException {
            if (id == jeiDropSyncID) {
                config.setConfig(WrappedItemStack.fromPacket(buf));
            }
        }
    }
}
