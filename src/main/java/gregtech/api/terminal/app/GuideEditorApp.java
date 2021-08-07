package gregtech.api.terminal.app;

import gregtech.api.gui.resources.TextureArea;
import gregtech.api.terminal.gui.widgets.guide.GuideConfigEditor;
import gregtech.api.terminal.gui.widgets.guide.GuidePageEditorWidget;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.util.function.Consumer;

public class GuideEditorApp extends AbstractApplication{
    public static final TextureArea ICON = TextureArea.fullImage("textures/gui/terminal/guide_editor/icon.png");

    public GuideEditorApp() {
        super("Guide Editor", ICON);
    }

    @Override
    public AbstractApplication createApp(boolean isClient, NBTTagCompound nbt) {
        GuideEditorApp app = new GuideEditorApp();
        if (isClient) {
            GuideConfigEditor configEditor = new GuideConfigEditor(0, 0, 133, 232, app);
            GuidePageEditorWidget pageEditor = new GuidePageEditorWidget(133, 0, 200, 232, 5);
            configEditor.setGuidePageEditorWidget(pageEditor);
            pageEditor.setGuideConfigEditor(configEditor);
            app.addWidget(pageEditor);
            app.addWidget(configEditor);
        }
        return app;
    }

    @Override
    protected void writeClientAction(int id, Consumer<PacketBuffer> packetBufferWriter) {
    }
}
