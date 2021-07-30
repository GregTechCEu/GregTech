package gregtech.api.terminal.app;

import gregtech.api.gui.resources.TextureArea;
import gregtech.api.terminal.gui.widgets.guide.GuideEditor;
import gregtech.api.terminal.gui.widgets.guide.GuideEditorPageWidget;
import net.minecraft.nbt.NBTTagCompound;

public class GuideEditorApp extends AbstractApplication{
    public static final TextureArea ICON = TextureArea.fullImage("textures/gui/terminal/guide_editor/icon.png");

    public GuideEditorApp() {
        super("Guide Editor", ICON);
    }

    @Override
    public AbstractApplication createApp(boolean isClient, NBTTagCompound nbt) {
        GuideEditorApp app = new GuideEditorApp();
        app.addWidget(new GuideEditor(0, 0, 133, 232));
        app.addWidget(new GuideEditorPageWidget(133, 0, 200, 232));
        return app;
    }
}
