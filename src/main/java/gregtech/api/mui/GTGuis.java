package gregtech.api.mui;

import gregtech.api.cover.Cover;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.mui.factory.CoverGuiFactory;
import gregtech.api.mui.factory.MetaItemGuiFactory;
import gregtech.api.mui.factory.MetaTileEntityGuiFactory;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.screen.ModularPanel;
import org.jetbrains.annotations.ApiStatus;

public class GTGuis {

    @ApiStatus.Internal
    public static void registerFactories() {
        GuiManager.registerFactory(MetaTileEntityGuiFactory.INSTANCE);
        GuiManager.registerFactory(MetaItemGuiFactory.INSTANCE);
        GuiManager.registerFactory(CoverGuiFactory.INSTANCE);
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
}
