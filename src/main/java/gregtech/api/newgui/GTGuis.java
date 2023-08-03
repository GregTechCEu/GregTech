package gregtech.api.newgui;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.manager.GuiInfo;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.theme.ReloadThemeEvent;
import com.cleanroommc.modularui.utils.JsonBuilder;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.EnumMap;

public class GTGuis {

    private static final JsonBuilder gregtechTheme = new JsonBuilder();
    private static final JsonBuilder bronzeTheme = new JsonBuilder();

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
                return getGuiHolder(itemStack).buildUI(context.with(EnumHand.MAIN_HAND), guiSyncHandler, context.getWorld().isRemote);
            })
            .build();

    public static final GuiInfo PLAYER_META_ITEM_OFF_HAND = GuiInfo.builder()
            .clientGui((context, mainPanel) -> {
                ItemStack itemStack = context.getOffHandItem();
                return getGuiHolder(itemStack).createScreen(context.with(EnumHand.OFF_HAND), mainPanel);

            })
            .commonGui((context, guiSyncHandler) -> {
                ItemStack itemStack = context.getOffHandItem();
                return getGuiHolder(itemStack).buildUI(context.with(EnumHand.OFF_HAND), guiSyncHandler, context.getWorld().isRemote);
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

    public static ModularPanel createPanel(CoverBehavior cover, int width, int height) {
        return createPanel(cover.getCoverDefinition().getCoverId().getPath(), width, height);
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
                    ICoverable coverable = te.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, facing);
                    if (coverable == null) throw new IllegalStateException();
                    CoverBehavior cover = coverable.getCoverAtSide(facing);
                    if (!(cover instanceof CoverWithUI)) throw new IllegalStateException();
                    return ((CoverWithUI) cover).createScreen(context, mainPanel);
                })
                .commonGui((context, syncHandler) -> {
                    TileEntity te = context.getTileEntity();
                    if (te == null) throw new IllegalStateException();
                    ICoverable coverable = te.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, facing);
                    if (coverable == null) throw new IllegalStateException();
                    CoverBehavior cover = coverable.getCoverAtSide(facing);
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

    public static void initThemes() {
        MinecraftForge.EVENT_BUS.register(GTGuis.class);
        IThemeApi.get().registerTheme("gregtech", gregtechTheme);
        IThemeApi.get().registerTheme("gregtech:bronze", bronzeTheme);
    }

    @SubscribeEvent
    public static void onReloadThemes(ReloadThemeEvent.Pre event) {
        gregtechTheme.add("color", ConfigHolder.client.defaultUIColor);
        bronzeTheme.add("parent", "gregtech")
                .add("color", 0xFA9D23);
    }
}
