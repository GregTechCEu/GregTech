package gregtech.api.gui;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaItem;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Function;

public abstract class GregTechGuiScreen extends ModularScreen {

    public static GregTechGuiScreen simple(@Nonnull MetaItem<?>.MetaValueItem metaValueItem, Function<GuiContext, ModularPanel> uiBuilder) {
        return simple(metaValueItem.getMetaItem().getRegistryName().getNamespace(), metaValueItem.unlocalizedName, uiBuilder);
    }

    public static GregTechGuiScreen simple(@Nonnull ResourceLocation loc, Function<GuiContext, ModularPanel> uiBuilder) {
        return simple(loc.getNamespace(), loc.getPath(), uiBuilder);
    }

    public static GregTechGuiScreen simple(@Nonnull String name, Function<GuiContext, ModularPanel> uiBuilder) {
        return simple(GTValues.MODID, name, uiBuilder);
    }

    public static GregTechGuiScreen simple(@Nonnull String owner, @Nonnull String name, Function<GuiContext, ModularPanel> uiBuilder) {
        return new GregTechGuiScreen(owner, name) {
            @Override
            public ModularPanel buildUI(GuiContext guiContext) {
                guiContext.useTheme("gregtech");
                return uiBuilder.apply(guiContext);
            }
        };
    }

    public GregTechGuiScreen(String owner, String name) {
        super(owner, name);
    }
}
