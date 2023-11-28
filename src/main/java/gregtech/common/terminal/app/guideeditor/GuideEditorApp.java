package gregtech.common.terminal.app.guideeditor;

import gregtech.api.gui.GuiTextures;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.menu.IMenuComponent;
import gregtech.common.terminal.app.guideeditor.widget.GuideConfigEditor;
import gregtech.common.terminal.app.guideeditor.widget.GuidePageEditorWidget;
import gregtech.common.terminal.component.ClickComponent;

import java.util.Arrays;
import java.util.List;

public class GuideEditorApp extends AbstractApplication {

    private GuideConfigEditor configEditor;

    public GuideEditorApp() {
        super("guide_editor");
    }

    @Override
    public AbstractApplication initApp() {
        if (isClient) {
            this.configEditor = new GuideConfigEditor(0, 0, 133, 232, this);
            GuidePageEditorWidget pageEditor = new GuidePageEditorWidget(133, 0, 200, 232, 5);
            this.configEditor.setGuidePageEditorWidget(pageEditor);
            pageEditor.setGuideConfigEditor(this.configEditor);
            this.addWidget(pageEditor);
            this.addWidget(this.configEditor);
        }
        return this;
    }

    @Override
    public List<IMenuComponent> getMenuComponents() {
        ClickComponent newPage = new ClickComponent().setIcon(GuiTextures.ICON_NEW_PAGE)
                .setHoverText("terminal.component.new_page").setClickConsumer(cd -> {
                    if (configEditor != null) {
                        configEditor.newPage();
                    }
                });
        ClickComponent importPage = new ClickComponent().setIcon(GuiTextures.ICON_LOAD)
                .setHoverText("terminal.component.load_file").setClickConsumer(cd -> {
                    if (configEditor != null) {
                        configEditor.loadJson();
                    }
                });
        ClickComponent exportPage = new ClickComponent().setIcon(GuiTextures.ICON_SAVE)
                .setHoverText("terminal.component.save_file").setClickConsumer(cd -> {
                    if (configEditor != null) {
                        configEditor.saveJson();
                    }
                });
        return Arrays.asList(newPage, importPage, exportPage);
    }

    @Override
    public boolean isClientSideApp() {
        return true;
    }
}
