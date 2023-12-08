package gregtech.api.mui;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverHolder;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.manager.GuiInfo;
import com.cleanroommc.modularui.screen.ModularPanel;

import java.util.EnumMap;

public class GTGuis {

    private static final EnumMap<EnumFacing, GuiInfo> COVERS = new EnumMap<>(EnumFacing.class);

    public static final GuiInfo MTE = GuiInfo.builder()
            .clientGui((context, mainPanel) -> {
                MetaTileEntity mte = GTUtility.getMetaTileEntity(context.getWorld(), context.getBlockPos());
                if (mte != null) {
                    return mte.createScreen(context, mainPanel);
                }
                throw new UnsupportedOperationException();
            })
            .commonGui((context, syncHandler) -> {
                MetaTileEntity mte = GTUtility.getMetaTileEntity(context.getWorld(), context.getBlockPos());
                if (mte != null) {
                    return mte.buildUI(context, syncHandler, context.getWorld().isRemote);
                }
                throw new UnsupportedOperationException();
            })
            .build();

    public static final GuiInfo PLAYER_META_ITEM_MAIN_HAND = GuiInfo.builder()
            .clientGui((context, mainPanel) -> {
                ItemStack itemStack = context.getMainHandItem();
                return getGuiHolder(itemStack).createScreen(context.with(EnumHand.MAIN_HAND), mainPanel);

            })
            .commonGui((context, guiSyncHandler) -> {
                ItemStack itemStack = context.getMainHandItem();
                return getGuiHolder(itemStack).buildUI(context.with(EnumHand.MAIN_HAND), guiSyncHandler,
                        context.getWorld().isRemote);
            })
            .build();

    public static final GuiInfo PLAYER_META_ITEM_OFF_HAND = GuiInfo.builder()
            .clientGui((context, mainPanel) -> {
                ItemStack itemStack = context.getOffHandItem();
                return getGuiHolder(itemStack).createScreen(context.with(EnumHand.OFF_HAND), mainPanel);

            })
            .commonGui((context, guiSyncHandler) -> {
                ItemStack itemStack = context.getOffHandItem();
                return getGuiHolder(itemStack).buildUI(context.with(EnumHand.OFF_HAND), guiSyncHandler,
                        context.getWorld().isRemote);
            })
            .build();

    public static GuiInfo getMetaItemUiInfo(EnumHand hand) {
        return hand == EnumHand.MAIN_HAND ? PLAYER_META_ITEM_MAIN_HAND : PLAYER_META_ITEM_OFF_HAND;
    }

    public static GuiInfo getCoverUiInfo(EnumFacing facing) {
        return COVERS.get(facing);
    }

    public static ModularPanel createPanel(String name, int width, int height) {
        return ModularPanel.defaultPanel(name, width, height);
    }

    public static ModularPanel createPanel(MetaTileEntity mte, int width, int height) {
        return createPanel(mte.metaTileEntityId.getPath(), width, height);
    }

    public static ModularPanel createPanel(Cover cover, int width, int height) {
        return createPanel(cover.getDefinition().getResourceLocation().getPath(), width, height);
    }

    public static ModularPanel createPanel(ItemStack stack, int width, int height) {
        MetaItem<?>.MetaValueItem valueItem = ((MetaItem<?>) stack.getItem()).getItem(stack);
        if (valueItem == null) throw new IllegalArgumentException("Item must be a meta item!");
        return createPanel(valueItem.unlocalizedName, width, height);
    }

    static {
        for (EnumFacing facing : EnumFacing.values()) {
            COVERS.put(facing, makeCoverUiInfo(facing));
        }
    }

    private static GuiInfo makeCoverUiInfo(EnumFacing facing) {
        return GuiInfo.builder()
                .clientGui((context, mainPanel) -> {
                    TileEntity te = context.getTileEntity();
                    if (te == null) throw new IllegalStateException();
                    CoverHolder coverHolder = te.getCapability(GregtechTileCapabilities.CAPABILITY_COVER_HOLDER,
                            facing);
                    if (coverHolder == null) throw new IllegalStateException();
                    Cover cover = coverHolder.getCoverAtSide(facing);
                    if (!(cover instanceof CoverWithUI)) throw new IllegalStateException();
                    return ((CoverWithUI) cover).createScreen(context, mainPanel);
                })
                .commonGui((context, syncHandler) -> {
                    TileEntity te = context.getTileEntity();
                    if (te == null) throw new IllegalStateException();
                    CoverHolder coverHolder = te.getCapability(GregtechTileCapabilities.CAPABILITY_COVER_HOLDER,
                            facing);
                    if (coverHolder == null) throw new IllegalStateException();
                    Cover cover = coverHolder.getCoverAtSide(facing);
                    if (!(cover instanceof CoverWithUI)) throw new IllegalStateException();
                    return ((CoverWithUI) cover).buildUI(context, syncHandler, context.getWorld().isRemote);
                })
                .build();
    }

    private static IGuiHolder getGuiHolder(ItemStack stack) {
        if (stack.getItem() instanceof MetaItem) {
            MetaItem<?>.MetaValueItem valueItem = ((MetaItem<?>) stack.getItem()).getItem(stack);
            if (valueItem != null && valueItem.getUIManager() != null) {
                return valueItem.getUIManager();
            }
        }
        throw new IllegalStateException();
    }
}
