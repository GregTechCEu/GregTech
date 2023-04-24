package gregtech.api.gui;

import com.cleanroommc.modularui.api.IItemGuiHolder;
import com.cleanroommc.modularui.manager.GuiInfo;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

import java.util.EnumMap;

public class GTGuis {

    private static final EnumMap<EnumFacing, GuiInfo> COVERS = new EnumMap<>(EnumFacing.class);

    public static final GuiInfo MTE = GuiInfo.builder()
            .clientGui(context -> {
                MetaTileEntity mte = GTUtility.getMetaTileEntity(context.getWorld(), context.getBlockPos());
                if (mte != null) {
                    return mte.createClientGui(context.getPlayer());
                }
                throw new UnsupportedOperationException();
            })
            .serverGui((context, syncHandler) -> {
                MetaTileEntity mte = GTUtility.getMetaTileEntity(context.getWorld(), context.getBlockPos());
                if (mte != null) {
                    mte.buildSyncHandler(syncHandler, context.getPlayer());
                }
            })
            .build();

    public static final GuiInfo PLAYER_META_ITEM_MAIN_HAND = GuiInfo.builder()
            .clientGui(context -> {
                ItemStack itemStack = context.getMainHandItem();
                return getGuiHolder(itemStack).createGuiScreen(context.getPlayer(), itemStack);

            })
            .serverGui((context, guiSyncHandler) -> {
                ItemStack itemStack = context.getMainHandItem();
                getGuiHolder(itemStack).buildSyncHandler(guiSyncHandler, context.getPlayer(), itemStack);
            })
            .build();

    public static final GuiInfo PLAYER_META_ITEM_OFF_HAND = GuiInfo.builder()
            .clientGui(context -> {
                ItemStack itemStack = context.getOffHandItem();
                return getGuiHolder(itemStack).createGuiScreen(context.getPlayer(), itemStack);
            })
            .serverGui((context, guiSyncHandler) -> {
                ItemStack itemStack = context.getOffHandItem();
                getGuiHolder(itemStack).buildSyncHandler(guiSyncHandler, context.getPlayer(), itemStack);
            })
            .build();

    public static GuiInfo getMetaItemUiInfo(EnumHand hand) {
        return hand == EnumHand.MAIN_HAND ? PLAYER_META_ITEM_MAIN_HAND : PLAYER_META_ITEM_OFF_HAND;
    }

    public static GuiInfo getCoverUiInfo(EnumFacing facing) {
        return COVERS.get(facing);
    }

    static {
        for (EnumFacing facing : EnumFacing.values()) {
            COVERS.put(facing, makeCoverUiInfo(facing));
        }
    }

    private static GuiInfo makeCoverUiInfo(EnumFacing facing) {
        return GuiInfo.builder()
                .clientGui(context -> {
                    TileEntity te = context.getTileEntity();
                    if (te == null) throw new IllegalStateException();
                    ICoverable coverable = te.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, facing);
                    if (coverable == null) throw new IllegalStateException();
                    CoverBehavior cover = coverable.getCoverAtSide(facing);
                    if (!(cover instanceof CoverWithUI)) throw new IllegalStateException();
                    return ((CoverWithUI) cover).createClientGui(context.getPlayer());
                })
                .serverGui((context, syncHandler) -> {
                    TileEntity te = context.getTileEntity();
                    if (te == null) throw new IllegalStateException();
                    ICoverable coverable = te.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, facing);
                    if (coverable == null) throw new IllegalStateException();
                    CoverBehavior cover = coverable.getCoverAtSide(facing);
                    if (!(cover instanceof CoverWithUI)) throw new IllegalStateException();
                    ((CoverWithUI) cover).buildSyncHandler(syncHandler, context.getPlayer());
                })
                .build();
    }

    private static IItemGuiHolder getGuiHolder(ItemStack stack) {
        if (stack.getItem() instanceof MetaItem) {
            MetaItem<?>.MetaValueItem valueItem = ((MetaItem<?>) stack.getItem()).getItem(stack);
            if (valueItem != null && valueItem.getUIManager() != null) {
                return valueItem.getUIManager();
            }
        }
        throw new IllegalStateException();
    }
}
