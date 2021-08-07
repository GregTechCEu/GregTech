package gregtech.api.terminal.gui.widgets.guide;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.*;
import gregtech.api.gui.widgets.tab.IGuiTextureTabInfo;
import gregtech.api.terminal.app.GuideEditorApp;
import gregtech.api.terminal.gui.CustomTabListRenderer;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.gui.widgets.TextEditorWidget;
import gregtech.api.terminal.gui.widgets.os.TerminalDialogWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;

public class GuideConfigEditor extends TabGroup {
    public String json;
    private IGuideWidget selected;
    private GuidePageEditorWidget pageEditor;
    private TextEditorWidget titleEditor;
    private final DraggableScrollableWidgetGroup widgetSelector;
    private final DraggableScrollableWidgetGroup widgetConfigurator;
    private final CircleButtonWidget[] addButton;
    private final GuideEditorApp app;

    public GuideConfigEditor(int x, int y, int width, int height, GuideEditorApp app) {
        super(x, y + 10, new CustomTabListRenderer(
                new ColorRectTexture(new Color(175, 0, 0, 131)),
                new ColorRectTexture(new Color(246, 120, 120, 190)), 30, 10));
        setSize(new Size(width, height));
        addButton = new CircleButtonWidget[2];
        widgetSelector = createWidgetSelector();
        widgetConfigurator = createConfigurator();
        this.addTab(new IGuiTextureTabInfo(new TextTexture("P", -1), "Page Config"), createPageConfig());
        this.addTab(new IGuiTextureTabInfo(new TextTexture("W", -1), "Widgets Box"), widgetSelector);
        this.addTab(new IGuiTextureTabInfo(new TextTexture("C", -1), "Widget Config"), widgetConfigurator);
        this.setOnTabChanged((oldIndex, newIndex)->{
           if (newIndex == 1) {
               addButton[0].setVisible(true);
               addButton[1].setVisible(true);
           } else {
               addButton[0].setVisible(false);
               addButton[1].setVisible(false);
           }
        });
        this.addWidget(new CircleButtonWidget(100, -5, 5, 1, 3)
                .setColors(new Color(255, 255, 255, 0).getRGB(),
                        new Color(255, 255, 255).getRGB(),
                        new Color(243, 208, 116).getRGB())
                .setIcon(GuiTextures.TERMINAL_ADD)
                .setHoverText("Import Guide")
                .setClickListener(this::loadJson));
        this.addWidget(new CircleButtonWidget(120, -5, 5, 1, 3)
                .setColors(new Color(255, 255, 255, 0).getRGB(),
                        new Color(255, 255, 255).getRGB(),
                        new Color(146, 253, 118).getRGB())
                .setIcon(GuiTextures.TERMINAL_ADD)
                .setHoverText("Export Config")
                .setClickListener(this::saveJson));
        addButton[0] = new CircleButtonWidget(115, 15, 8, 1, 8)
                .setColors(new Color(255, 255, 255, 0).getRGB(),
                        new Color(255, 255, 255).getRGB(),
                        new Color(0, 115, 255).getRGB())
                .setIcon(GuiTextures.TERMINAL_ADD)
                .setHoverText("add stream")
                .setClickListener(this::addStream);
        addButton[1] = new CircleButtonWidget(115, 35, 8, 1, 8)
                .setColors(new Color(255, 255, 255, 0).getRGB(),
                        new Color(255, 255, 255).getRGB(),
                        new Color(113, 27, 217).getRGB())
                .setIcon(GuiTextures.TERMINAL_ADD)
                .setHoverText("add fixed")
                .setClickListener(this::addFixed);
        addButton[0].setVisible(false);
        addButton[1].setVisible(false);
        this.app = app;
        this.addWidget(addButton[0]);
        this.addWidget(addButton[1]);
    }

    public void setGuidePageEditorWidget(GuidePageEditorWidget pageEditor) {
        this.pageEditor = pageEditor;
    }

    public GuidePageEditorWidget getPageEditor() {
        return pageEditor;
    }

    private DraggableScrollableWidgetGroup createPageConfig() {
        DraggableScrollableWidgetGroup group = new DraggableScrollableWidgetGroup(0, 0, getSize().width, getSize().height - 10)
                .setBackground(new ColorRectTexture(new Color(246, 120, 120, 190)))
                .setYScrollBarWidth(4)
                .setYBarStyle(null, new ColorRectTexture(new Color(148, 226, 193)));
        group.addWidget(new LabelWidget(5, 5, "section", -1).setShadow(true));
        group.addWidget(new TextFieldWidget(5, 15, 116, 20, new ColorRectTexture(0x9f000000), null, null)
                .setTextResponder(s->{
                    if (pageEditor != null) {
                        pageEditor.setSection(s);
                    }
                }, true)
                .setTextSupplier(()-> getPageEditor().getSection(), true)
                .setMaxStringLength(Integer.MAX_VALUE)
                .setValidator(s->true));
        group.addWidget(new ImageWidget(5, 48,116, 1, new ColorRectTexture(-1)));
        group.addWidget(new LabelWidget(5, 55, "title", -1).setShadow(true));
        titleEditor = new TextEditorWidget(5, 65, 116, 70, s->{
            if (pageEditor != null) {
                pageEditor.setTitle(s);
            }
        }, true).setContent("Template").setBackground(new ColorRectTexture(0xA3FFFFFF));
        group.addWidget(titleEditor);
        return group;
    }

    public void updateTitle(String title) {
        titleEditor.setContent(title);
    }

    private DraggableScrollableWidgetGroup createWidgetSelector() {
        DraggableScrollableWidgetGroup group = new DraggableScrollableWidgetGroup(0, 0, getSize().width, getSize().height - 10)
                .setBackground(new ColorRectTexture(new Color(246, 120, 120, 190)))
                .setYScrollBarWidth(4)
                .setYBarStyle(null, new ColorRectTexture(new Color(148, 226, 193)));
        int y = 10; //133
        for (Map.Entry<String, IGuideWidget> entry : GuidePageWidget.REGISTER_WIDGETS.entrySet()) {
            IGuideWidget widgetTemplate = entry.getValue();
            JsonObject template = widgetTemplate.getTemplate(false);
            Widget guideWidget = widgetTemplate.updateOrCreateStreamWidget(5, y + 10, getSize().width - 14, template);
            group.addWidget(new LabelWidget(getSize().width / 2 - 1, y - 3, entry.getKey(), -1).setXCentered(true).setShadow(true));
            y += guideWidget.getSize().height + 25;
            group.addWidget(guideWidget);
        }
        group.addWidget(new WidgetGroup(new Position(5, group.getWidgetBottomHeight() + 5), Size.ZERO));
        return group;
    }

    private DraggableScrollableWidgetGroup createConfigurator() {
        return new DraggableScrollableWidgetGroup(0, 0, getSize().width, getSize().height - 10)
                .setBackground(new ColorRectTexture(new Color(246, 120, 120, 190)))
                .setYScrollBarWidth(4)
                .setYBarStyle(null, new ColorRectTexture(new Color(148, 226, 193)));
    }

    public void loadConfigurator(IGuideWidget widget) {
        widgetConfigurator.clearAllWidgets();
        if (widget != null) {
            widget.loadConfigurator(widgetConfigurator, widget.getConfig(), widget.isFixed(), widget::updateValue);
            widgetConfigurator.addWidget(new WidgetGroup(new Position(5, widgetConfigurator.getWidgetBottomHeight() + 5), Size.ZERO));
        }
    }

    private void loadJson(ClickData data) {
        if(pageEditor != null) {
            File file = new File("terminal\\guide_editor");
            TerminalDialogWidget.showFileDialog(app.getOs(), "Load Json", file, true, result->{
               if (result != null && result.isFile()) {
                   try {
                       FileReader reader = new FileReader(result);
                       JsonObject config = new JsonParser().parse(new JsonReader(reader)).getAsJsonObject();
                       pageEditor.loadJsonConfig(config);
                       reader.close();
                       getPageEditor().setSection(config.get("section").getAsString());
                       updateTitle(config.get("title").getAsString());
                   } catch (Exception e) {
                       TerminalDialogWidget.showInfoDialog(app.getOs(), "ERROR", "An error occurred while loading the file.").setClientSide().open();
                   }
               }
            }).setClientSide().open();
        }
    }

    private void saveJson(ClickData data) {
        if(pageEditor != null) {
            File file = new File("terminal\\guide_editor");
            TerminalDialogWidget.showFileDialog(app.getOs(), "Save Json", file, false, result->{
                if (result != null) {
                    try {
                        FileWriter writer = new FileWriter(result);
                        writer.write(pageEditor.getJsonString());
                        writer.close();
                    } catch (Exception e) {
                        TerminalDialogWidget.showInfoDialog(app.getOs(), "ERROR", "An error occurred while saving the file.").setClientSide().open();
                    }
                }
            }).setClientSide().open();
        }
    }

    private void addFixed(ClickData data) {
        if (pageEditor != null && selected != null) {
            selected.setStroke(0);
            pageEditor.addGuideWidget(selected, true);
            selected.setStroke(0xFF7CA1FF);
        }
    }

    private void addStream(ClickData data) {
        if (pageEditor != null && selected != null) {
            selected.setStroke(0);
            pageEditor.addGuideWidget(selected, false);
            selected.setStroke(0xFF7CA1FF);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        boolean flag = super.mouseClicked(mouseX, mouseY, button);
        if (selectedTabIndex == 1 && widgetSelector != null) {
            for (Widget widget : widgetSelector.widgets) {
                if (widget.isMouseOverElement(mouseX, mouseY)) {
                    if (widget instanceof IGuideWidget) {
                        if (selected != null) {
                            selected.setStroke(0);
                        }
                        ((IGuideWidget) widget).setStroke(0xFF7CA1FF);
                        selected = (IGuideWidget) widget;
                    }
                    playButtonClickSound();
                    return true;
                }
            }
        }
        return flag;
    }
}
