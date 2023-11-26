package gregtech.common.terminal.app.guideeditor.widget;

import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.*;
import gregtech.api.gui.widgets.tab.IGuiTextureTabInfo;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.CustomTabListRenderer;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.gui.widgets.SelectorWidget;
import gregtech.api.terminal.gui.widgets.TextEditorWidget;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.FileUtility;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.common.inventory.handlers.SingleItemStackHandler;
import gregtech.common.terminal.app.guide.GuideApp;
import gregtech.common.terminal.app.guide.ItemGuideApp;
import gregtech.common.terminal.app.guide.MultiBlockGuideApp;
import gregtech.common.terminal.app.guide.SimpleMachineGuideApp;
import gregtech.common.terminal.app.guide.widget.GuidePageWidget;
import gregtech.common.terminal.app.guide.widget.IGuideWidget;
import gregtech.common.terminal.app.guideeditor.GuideEditorApp;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.google.gson.JsonObject;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class GuideConfigEditor extends TabGroup<AbstractWidgetGroup> {

    public String json;
    private IGuideWidget selected;
    private GuidePageEditorWidget pageEditor;
    private TextEditorWidget titleEditor;
    private final DraggableScrollableWidgetGroup widgetSelector;
    private final DraggableScrollableWidgetGroup widgetConfigurator;
    private final CircleButtonWidget[] addButton;
    private final GuideEditorApp app;
    private final IItemHandlerModifiable handler;
    private final List<String> candidates;
    private String type;

    public GuideConfigEditor(int x, int y, int width, int height, GuideEditorApp app) {
        super(x, y + 10, new CustomTabListRenderer(TerminalTheme.COLOR_F_2, TerminalTheme.COLOR_B_3, 30, 10));
        setSize(new Size(width, height));
        candidates = TerminalRegistry.getAllApps().stream().filter(GuideApp.class::isInstance)
                .map(AbstractApplication::getUnlocalizedName).collect(Collectors.toList());
        type = candidates.get(candidates.size() - 1);
        handler = new SingleItemStackHandler(1);
        addButton = new CircleButtonWidget[2];
        widgetSelector = createWidgetSelector();
        widgetConfigurator = createConfigurator();
        this.addTab(new IGuiTextureTabInfo(new TextTexture("P", -1), "terminal.guide_editor.page_config"),
                createPageConfig());
        this.addTab(new IGuiTextureTabInfo(new TextTexture("W", -1), "terminal.guide_editor.widgets_box"),
                widgetSelector);
        this.addTab(new IGuiTextureTabInfo(new TextTexture("C", -1), "terminal.guide_editor.widget_config"),
                widgetConfigurator);
        this.setOnTabChanged((oldIndex, newIndex) -> {
            if (newIndex == 1) {
                addButton[0].setVisible(true);
                addButton[1].setVisible(true);
            } else {
                addButton[0].setVisible(false);
                addButton[1].setVisible(false);
            }
        });
        addButton[0] = new CircleButtonWidget(115, 15, 8, 1, 8)
                .setColors(new Color(255, 255, 255, 0).getRGB(),
                        TerminalTheme.COLOR_7.getColor(),
                        TerminalTheme.COLOR_4.getColor())
                .setIcon(GuiTextures.ICON_ADD)
                .setHoverText("terminal.guide_editor.add_stream")
                .setClickListener(this::addStream);
        addButton[1] = new CircleButtonWidget(115, 35, 8, 1, 8)
                .setColors(new Color(255, 255, 255, 0).getRGB(),
                        TerminalTheme.COLOR_7.getColor(),
                        TerminalTheme.COLOR_5.getColor())
                .setIcon(GuiTextures.ICON_ADD)
                .setHoverText("terminal.guide_editor.add_fixed")
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
        DraggableScrollableWidgetGroup group = new DraggableScrollableWidgetGroup(0, 0, getSize().width,
                getSize().height - 10)
                        .setBackground(TerminalTheme.COLOR_B_3)
                        .setYScrollBarWidth(4)
                        .setYBarStyle(null, TerminalTheme.COLOR_F_1);
        group.addWidget(new LabelWidget(5, 5, "section", -1).setShadow(true));
        group.addWidget(new TextFieldWidget(5, 15, 116, 20, new ColorRectTexture(0x9f000000), null, null)
                .setTextResponder(s -> {
                    if (pageEditor != null) {
                        pageEditor.setSection(s);
                    }
                }, true)
                .setTextSupplier(() -> getPageEditor().getSection(), true)
                .setMaxStringLength(Integer.MAX_VALUE)
                .setValidator(s -> true));
        group.addWidget(new ImageWidget(5, 40, 116, 1, new ColorRectTexture(-1)));
        group.addWidget(new LabelWidget(5, 45, "type", -1).setShadow(true));

        group.addWidget(new SelectorWidget(30, 55, 91, 20, candidates, -1,
                () -> type, true).setIsUp(true)
                        .setOnChanged(type -> this.type = type)
                        .setColors(TerminalTheme.COLOR_B_2.getColor(), TerminalTheme.COLOR_F_1.getColor(),
                                TerminalTheme.COLOR_B_2.getColor())
                        .setBackground(TerminalTheme.COLOR_6));
        group.addWidget(new PhantomSlotWidget(handler, 0, 6, 56).setBackgroundTexture(TerminalTheme.COLOR_B_2));

        group.addWidget(new ImageWidget(5, 80, 116, 1, new ColorRectTexture(-1)));

        group.addWidget(new LabelWidget(5, 85, "title", -1).setShadow(true));
        titleEditor = new TextEditorWidget(5, 95, 116, 70, s -> {
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
        DraggableScrollableWidgetGroup group = new DraggableScrollableWidgetGroup(0, 0, getSize().width,
                getSize().height - 10)
                        .setBackground(TerminalTheme.COLOR_B_3)
                        .setYScrollBarWidth(4)
                        .setYBarStyle(null, TerminalTheme.COLOR_F_1);
        int y = 10; // 133
        for (Map.Entry<String, IGuideWidget> entry : GuidePageWidget.REGISTER_WIDGETS.entrySet()) {
            IGuideWidget widgetTemplate = entry.getValue();
            JsonObject template = widgetTemplate.getTemplate(false);
            Widget guideWidget = widgetTemplate.updateOrCreateStreamWidget(5, y + 10, getSize().width - 14, template);
            group.addWidget(new LabelWidget(getSize().width / 2 - 1, y - 3, entry.getKey(), -1).setXCentered(true)
                    .setShadow(true));
            y += guideWidget.getSize().height + 25;
            group.addWidget(guideWidget);
        }
        group.addWidget(new WidgetGroup(new Position(5, group.getWidgetBottomHeight() + 5), Size.ZERO));
        return group;
    }

    private DraggableScrollableWidgetGroup createConfigurator() {
        return new DraggableScrollableWidgetGroup(0, 0, getSize().width, getSize().height - 10)
                .setBackground(TerminalTheme.COLOR_B_3)
                .setYScrollBarWidth(4)
                .setYBarStyle(null, TerminalTheme.COLOR_F_1);
    }

    public void loadConfigurator(IGuideWidget widget) {
        widgetConfigurator.clearAllWidgets();
        if (widget != null) {
            widget.loadConfigurator(widgetConfigurator, widget.getConfig(), widget.isFixed(), type -> {
                widget.updateValue(type);
                if (pageEditor != null) {
                    pageEditor.computeMax();
                }
            });
            widgetConfigurator.addWidget(
                    new WidgetGroup(new Position(5, widgetConfigurator.getWidgetBottomHeight() + 5), Size.ZERO));
        }
    }

    public void newPage() {
        TerminalDialogWidget
                .showConfirmDialog(app.getOs(), "terminal.component.new_page", "terminal.component.confirm", res -> {
                    if (res) {
                        pageEditor.loadJsonConfig(
                                "{\"section\":\"default\",\"title\":\"Template\",\"stream\":[],\"fixed\":[]}");
                        getPageEditor().setSection("default");
                        updateTitle("Template");
                        type = candidates.get(candidates.size() - 1);
                        handler.setStackInSlot(0, ItemStack.EMPTY);
                    }
                }).setClientSide().open();
    }

    public void loadJson() {
        if (pageEditor != null) {
            File file = new File(TerminalRegistry.TERMINAL_PATH, "guide");
            TerminalDialogWidget.showFileDialog(app.getOs(), "terminal.component.load_file", file, true, result -> {
                if (result != null && result.isFile()) {
                    try {
                        JsonObject config = Objects.requireNonNull(FileUtility.loadJson(result)).getAsJsonObject();
                        pageEditor.loadJsonConfig(config);
                        getPageEditor().setSection(config.get("section").getAsString());
                        updateTitle(config.get("title").getAsString());
                        for (AbstractApplication app : TerminalRegistry.getAllApps()) {
                            if (app instanceof GuideApp) {
                                Object object = ((GuideApp<?>) app).ofJson(config);
                                if (object != null) {
                                    type = app.getUnlocalizedName();
                                    if (object instanceof ItemGuideApp.GuideItem) {
                                        handler.setStackInSlot(0, ((ItemGuideApp.GuideItem) object).stack.copy());
                                    } else if (object instanceof MetaTileEntity) {
                                        handler.setStackInSlot(0, ((MetaTileEntity) object).getStackForm());
                                    } else if (object instanceof ItemStack) {
                                        handler.setStackInSlot(0, ((ItemStack) object).copy());
                                    } else {
                                        handler.setStackInSlot(0, ItemStack.EMPTY);
                                    }
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        TerminalDialogWidget.showInfoDialog(app.getOs(), "terminal.component.error",
                                "terminal.component.load_file.error").setClientSide().open();
                    }
                }
            }).setClientSide().open();
        }
    }

    public void saveJson() {
        if (pageEditor != null) {
            File file = new File(TerminalRegistry.TERMINAL_PATH, "guide");
            TerminalDialogWidget.showFileDialog(app.getOs(), "terminal.component.save_file", file, false, result -> {
                if (result != null) {
                    if (!FileUtility.saveJson(result, saveType(pageEditor.getJsonConfig()))) {
                        TerminalDialogWidget.showInfoDialog(app.getOs(), "terminal.component.error",
                                "terminal.component.save_file.error").setClientSide().open();
                    }
                }
            }).setClientSide().open();
        }
    }

    private JsonObject saveType(JsonObject jsonObject) {
        ItemStack stack = handler.getStackInSlot(0);
        for (AbstractApplication app : TerminalRegistry.getAllApps()) {
            if (app instanceof GuideApp) {
                if (type.equals(app.getUnlocalizedName())) {
                    if (app instanceof ItemGuideApp) {
                        if (stack.isEmpty()) {
                            TerminalDialogWidget.showInfoDialog(app.getOs(), "terminal.component.warning",
                                    "terminal.guide_editor.error_type").setClientSide().open();
                        } else {
                            jsonObject.addProperty("item", Item.REGISTRY.getNameForObject(stack.getItem()).toString() +
                                    ":" + stack.getMetadata());
                        }
                    } else if ((app instanceof MultiBlockGuideApp || app instanceof SimpleMachineGuideApp) &&
                            stack.getItem() instanceof MachineItemBlock) {
                                MetaTileEntity mte = GregTechAPI.MTE_REGISTRY.getObjectById(stack.getItemDamage());
                                if (mte != null) {
                                    jsonObject.addProperty("metatileentity",
                                            GregTechAPI.MTE_REGISTRY.getNameForObject(mte).getPath());
                                } else {
                                    TerminalDialogWidget.showInfoDialog(app.getOs(), "terminal.component.warning",
                                            "terminal.guide_editor.error_type").setClientSide().open();
                                }
                            }
                    return jsonObject;
                }
            }
        }
        return jsonObject;
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
