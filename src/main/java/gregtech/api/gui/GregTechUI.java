package gregtech.api.gui;

import com.cleanroommc.modularui.api.screen.ModularUIContext;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.common.builder.UIBuilder;
import com.cleanroommc.modularui.common.builder.UIInfo;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import com.cleanroommc.modularui.common.internal.wrapper.ModularUIContainer;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.function.Function;

public class GregTechUI {

    private static final UIInfo<?, ?>[] SIDED_COVER_UI = new UIInfo[6];
    private static final UIInfo<?, ?>[] PLAYER_ITEM_UI = new UIInfo[2];

    public static UIInfo<?, ?> getCoverUi(EnumFacing facing) {
        return SIDED_COVER_UI[facing.getIndex()];
    }

    public static UIInfo<?, ?> getPlayerItemUi(EnumHand hand) {
        return PLAYER_ITEM_UI[hand.ordinal()];
    }

    private static ModularUIContainer createModularContainer(EntityPlayer player, Function<UIBuildContext, ModularWindow> windowCreator) {
        UIBuildContext buildContext = new UIBuildContext(player);
        ModularWindow window = windowCreator.apply(buildContext);
        return new ModularUIContainer(new ModularUIContext(buildContext), window);
    }

    public static final UIInfo<?, ?> MTE_UI = UIBuilder.of()
            .gui(((player, world, x, y, z) -> {
                TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                if (te instanceof MetaTileEntityHolder && !te.isInvalid()) {
                    MetaTileEntity mte = ((MetaTileEntityHolder) te).getMetaTileEntity();
                    if (mte != null) {
                        return new ModularGui(createModularContainer(player, mte::createWindow));
                    }
                }
                return null;
            }))
            .container((player, world, x, y, z) -> {
                TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                if (te instanceof MetaTileEntityHolder && !te.isInvalid()) {
                    MetaTileEntity mte = ((MetaTileEntityHolder) te).getMetaTileEntity();
                    if (mte != null) {
                        return createModularContainer(player, mte::createWindow);
                    }
                }
                return null;
            })
            .build();

    public static void init() {
        for (EnumHand hand : EnumHand.values()) {
            PLAYER_ITEM_UI[hand.ordinal()] = UIBuilder.of()
                    .gui((player, world, x, y, z) -> {
                        ItemStack item = player.getHeldItem(hand);
                        if (item.getItem() instanceof MetaItem) {
                            for (IItemBehaviour behaviour : ((MetaItem<?>) item.getItem()).getBehaviours(item)) {
                                if (behaviour instanceof ItemUIFactory) {
                                    ItemUIFactory uiFactory = (ItemUIFactory) behaviour;
                                    return new ModularGui(createModularContainer(player, uiFactory::createWindow));
                                }
                            }
                        }
                        return null;
                    })
                    .container((player, world, x, y, z) -> {
                        ItemStack item = player.getHeldItem(hand);
                        if (item.getItem() instanceof MetaItem) {
                            for (IItemBehaviour behaviour : ((MetaItem<?>) item.getItem()).getBehaviours(item)) {
                                if (behaviour instanceof ItemUIFactory) {
                                    ItemUIFactory uiFactory = (ItemUIFactory) behaviour;
                                    return createModularContainer(player, uiFactory::createWindow);
                                }
                            }
                        }
                        return null;
                    }).build();
        }
        for (EnumFacing facing : EnumFacing.VALUES) {
            SIDED_COVER_UI[facing.getIndex()] = UIBuilder.of()
                    .gui(((player, world, x, y, z) -> {
                        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                        if (te == null) {
                            return null;
                        }
                        ICoverable coverable = te.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, facing);
                        if (coverable == null) {
                            return null;
                        }
                        CoverBehavior cover = coverable.getCoverAtSide(facing);
                        if (cover instanceof CoverWithUI) {
                            CoverWithUI coverWithUI = (CoverWithUI) cover;
                            return new ModularGui(createModularContainer(player, coverWithUI::createWindow));
                        }
                        return null;
                    }))
                    .container((player, world, x, y, z) -> {
                        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                        if (te == null) {
                            return null;
                        }
                        ICoverable coverable = te.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, facing);
                        if (coverable == null) {
                            return null;
                        }
                        CoverBehavior cover = coverable.getCoverAtSide(facing);
                        if (cover instanceof CoverWithUI) {
                            CoverWithUI coverWithUI = (CoverWithUI) cover;
                            return createModularContainer(player, coverWithUI::createWindow);
                        }
                        return null;
                    })
                    .build();
        }
    }
}
