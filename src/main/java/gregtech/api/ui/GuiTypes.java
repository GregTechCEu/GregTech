package gregtech.api.ui;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.IItemGuiHolder;
import com.cleanroommc.modularui.manager.GuiInfo;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public final class GuiTypes {

    /**
     * Type of GUI being opened from a TileEntity or Block
     */
    public static final GuiType TILE = new GuiType(GuiInfo.builder()
            .clientGui(context -> {
                TileEntity tile = context.getTileEntity();
                if (tile instanceof IGuiHolder holder) {
                    return holder.createClientGui(context.getPlayer());
                }

                if (tile instanceof IGregTechTileEntity gregTechTile) {
                    return gregTechTile.getMetaTileEntity().createClientGui(context.getPlayer());
                }
                throw new UnsupportedOperationException("No valid tile type for tile entity: " + tile);
            })
            .serverGui((context, syncHandler) -> {
                TileEntity tile = context.getTileEntity();
                if (tile instanceof IGuiHolder holder) {
                    holder.buildSyncHandler(syncHandler, context.getPlayer());
                    return;
                }

                if (tile instanceof IGregTechTileEntity gregTechTile) {
                    gregTechTile.getMetaTileEntity().buildSyncHandler(syncHandler, context.getPlayer());
                    return;
                }
                throw new UnsupportedOperationException("Not valid tile type for tile entity: " + tile);
            })
            .build());

    /**
     * Type of GUI being opened from an Item in the player's Main Hand
     */
    public static final GuiType ITEM_MAIN_HAND = new GuiType(GuiInfo.builder()
            .clientGui(context -> {
                ItemStack itemStack = context.getMainHandItem();
                if (itemStack.getItem() instanceof IItemGuiHolder holder) {
                    return holder.createGuiScreen(context.getPlayer(), itemStack);
                }
                throw new UnsupportedOperationException("ItemStack is not an IItemGuiHolder");
            })
            .serverGui((context, guiSyncHandler) -> {
                ItemStack itemStack = context.getMainHandItem();
                if (itemStack.getItem() instanceof IItemGuiHolder holder) {
                    holder.buildSyncHandler(guiSyncHandler, context.getPlayer(), itemStack);
                    return;
                }
                throw new UnsupportedOperationException("ItemStack is not an IItemGuiHolder");
            })
            .build());

    /**
     * Type of GUI being opened from an Item in the player's Main Hand
     */
    public static final GuiType ITEM_OFF_HAND = new GuiType(GuiInfo.builder()
            .clientGui(context -> {
                ItemStack itemStack = context.getOffHandItem();
                if (itemStack.getItem() instanceof IItemGuiHolder holder) {
                    return holder.createGuiScreen(context.getPlayer(), itemStack);
                }
                throw new UnsupportedOperationException("ItemStack is not an IItemGuiHolder");
            })
            .serverGui((context, guiSyncHandler) -> {
                ItemStack itemStack = context.getOffHandItem();
                if (itemStack.getItem() instanceof IItemGuiHolder holder) {
                    holder.buildSyncHandler(guiSyncHandler, context.getPlayer(), itemStack);
                    return;
                }
                throw new UnsupportedOperationException("ItemStack is not an IItemGuiHolder");
            })
            .build());

    private GuiTypes() {}
}
