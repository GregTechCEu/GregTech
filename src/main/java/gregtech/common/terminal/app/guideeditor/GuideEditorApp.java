package gregtech.common.terminal.app.guideeditor;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.common.terminal.app.guideeditor.widget.GuideConfigEditor;
import gregtech.common.terminal.app.guideeditor.widget.GuidePageEditorWidget;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.common.terminal.component.ClickComponent;
import gregtech.api.terminal.os.menu.IMenuComponent;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;
import java.util.List;

public class GuideEditorApp extends AbstractApplication {
    public static final TextureArea ICON = TextureArea.fullImage("textures/gui/terminal/guide_editor/icon.png");


    private GuideConfigEditor configEditor;


    public GuideEditorApp() {
        super("guide_editor", ICON);
    }

    @Override
    public AbstractApplication createApp(TerminalOSWidget os, boolean isClient, NBTTagCompound nbt) {
        GuideEditorApp app = new GuideEditorApp();
        if (isClient) {
            app.configEditor = new GuideConfigEditor(0, 0, 133, 232, app);
            GuidePageEditorWidget pageEditor = new GuidePageEditorWidget(133, 0, 200, 232, 5);
            app.configEditor.setGuidePageEditorWidget(pageEditor);
            pageEditor.setGuideConfigEditor(app.configEditor);
            app.addWidget(pageEditor);
            app.addWidget(app.configEditor);
        }
        return app;
    }

    @Override
    public List<IMenuComponent> getMenuComponents() {
        ClickComponent newPage = new ClickComponent().setIcon(GuiTextures.ICON_NEW_PAGE).setHoverText("terminal.component.new_page").setClickConsumer(cd->{
            if (configEditor != null) {
                configEditor.newPage(cd);
            }
        });
        ClickComponent importPage = new ClickComponent().setIcon(GuiTextures.ICON_LOAD).setHoverText("terminal.component.load_file").setClickConsumer(cd->{
            if (configEditor != null) {
                configEditor.loadJson(cd);
            }
        });
        ClickComponent exportPage = new ClickComponent().setIcon(GuiTextures.ICON_SAVE).setHoverText("terminal.component.save_file").setClickConsumer(cd->{
            if (configEditor != null) {
                configEditor.saveJson(cd);
            }
        });
        return Arrays.asList(newPage, importPage, exportPage);
    }

    @Override
    public boolean isClientSideApp() {
        return true;
    }

}
