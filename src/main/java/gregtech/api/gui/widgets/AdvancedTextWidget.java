package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents a text-component based widget, which obtains
 * text from server and automatically synchronizes it with clients
 */
public class AdvancedTextWidget extends Widget {
    protected int maxWidthLimit;

    @SideOnly(Side.CLIENT)
    private WrapScreen wrapScreen;

    protected final Consumer<List<ITextComponent>> textSupplier;
    protected BiConsumer<String, ClickData> clickHandler;
    private List<ITextComponent> displayText = new ArrayList<>();
    private final int color;

    public AdvancedTextWidget(int xPosition, int yPosition, Consumer<List<ITextComponent>> text, int color) {
        super(new Position(xPosition, yPosition), Size.ZERO);
        this.textSupplier = text;
        this.color = color;
    }

    public static ITextComponent withButton(ITextComponent textComponent, String componentData) {
        Style style = textComponent.getStyle();
        style.setClickEvent(new ClickEvent(Action.OPEN_URL, "@!" + componentData));
        if(style.getColor() == null) {
            style.setColor(TextFormatting.YELLOW);
        }
        return textComponent;
    }

    public static ITextComponent withHoverTextTranslate(ITextComponent textComponent, String hoverTranslation) {
        Style style = textComponent.getStyle();
        ITextComponent translation = new TextComponentTranslation(hoverTranslation);
        translation.getStyle().setColor(TextFormatting.GRAY);
        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translation));
        return textComponent;
    }

    public AdvancedTextWidget setMaxWidthLimit(int maxWidthLimit) {
        this.maxWidthLimit = maxWidthLimit;
        if (isClientSide()) {
            updateComponentTextSize();
        }
        return this;
    }

    public AdvancedTextWidget setClickHandler(BiConsumer<String, ClickData> clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }

    @SideOnly(Side.CLIENT)
    private WrapScreen getWrapScreen() {
        if (wrapScreen == null)
            wrapScreen = new WrapScreen();

        return wrapScreen;
    }

    @SideOnly(Side.CLIENT)
    private void resizeWrapScreen() {
        if (sizes != null) {
            getWrapScreen().setWorldAndResolution(Minecraft.getMinecraft(), sizes.getScreenWidth(), sizes.getScreenHeight());
        }
    }

    @Override
    public void initWidget() {
        super.initWidget();
        if (isClientSide()) {
            resizeWrapScreen();
        }
    }

    @Override
    protected void onPositionUpdate() {
        super.onPositionUpdate();
        if (isClientSide()) {
            resizeWrapScreen();
        }
    }

    @Override
    public void detectAndSendChanges() {
        ArrayList<ITextComponent> textBuffer = new ArrayList<>();
        textSupplier.accept(textBuffer);
        if (!displayText.equals(textBuffer)) {
            this.displayText = textBuffer;
            writeUpdateInfo(1, buffer -> {
                buffer.writeVarInt(displayText.size());
                for (ITextComponent textComponent : displayText) {
                    buffer.writeString(ITextComponent.Serializer.componentToJson(textComponent));
                }
            });
        }
    }

    protected ITextComponent getTextUnderMouse(int mouseX, int mouseY) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        Position position = getPosition();
        int selectedLine = (mouseY - position.y) / (fontRenderer.FONT_HEIGHT + 2);
        if (mouseX >= position.x && selectedLine >= 0 && selectedLine < displayText.size()) {
            ITextComponent selectedComponent = displayText.get(selectedLine);
            int mouseOffset = mouseX - position.x;
            int currentOffset = 0;
            for (ITextComponent lineComponent : selectedComponent) {
                currentOffset += fontRenderer.getStringWidth(lineComponent.getUnformattedComponentText());
                if (currentOffset >= mouseOffset) {
                    return lineComponent;
                }
            }
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    private void updateComponentTextSize() {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int maxStringWidth = 0;
        int totalHeight = 0;
        for (ITextComponent textComponent : displayText) {
            maxStringWidth = Math.max(maxStringWidth, fontRenderer.getStringWidth(textComponent.getFormattedText()));
            totalHeight += fontRenderer.FONT_HEIGHT + 2;
        }
        totalHeight -= 2;
        setSize(new Size(maxStringWidth, totalHeight));
        if (uiAccess != null) {
            uiAccess.notifySizeChange();
        }
    }

    @SideOnly(Side.CLIENT)
    private void formatDisplayText() {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int maxTextWidthResult = maxWidthLimit == 0 ? Integer.MAX_VALUE : maxWidthLimit;
        this.displayText = displayText.stream()
                .flatMap(c -> GuiUtilRenderComponents.splitText(c, maxTextWidthResult, fontRenderer, true, true).stream())
                .collect(Collectors.toList());
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            this.displayText.clear();
            int count = buffer.readVarInt();
            for (int i = 0; i < count; i++) {
                String jsonText = buffer.readString(32767);
                this.displayText.add(ITextComponent.Serializer.jsonToComponent(jsonText));
            }
            formatDisplayText();
            updateComponentTextSize();
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            ClickData clickData = ClickData.readFromBuf(buffer);
            String componentData = buffer.readString(128);
            if (clickHandler != null) {
                clickHandler.accept(componentData, clickData);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private boolean handleCustomComponentClick(ITextComponent textComponent) {
        Style style = textComponent.getStyle();
        if (style.getClickEvent() != null) {
            ClickEvent clickEvent = style.getClickEvent();
            String componentText = clickEvent.getValue();
            if (clickEvent.getAction() == Action.OPEN_URL && componentText.startsWith("@!")) {
                String rawText = componentText.substring(2);
                ClickData clickData = new ClickData(Mouse.getEventButton(), isShiftDown(), isCtrlDown());
                writeClientAction(1, buf -> {
                    clickData.writeToBuf(buf);
                    buf.writeString(rawText);
                });
                return true;
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        ITextComponent textComponent = getTextUnderMouse(mouseX, mouseY);
        if (textComponent != null) {
            if (handleCustomComponentClick(textComponent) ||
                    getWrapScreen().handleComponentClick(textComponent)) {
                playButtonClickSound();
                return true;
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        Position position = getPosition();
        for (int i = 0; i < displayText.size(); i++) {
            fontRenderer.drawString(displayText.get(i).getFormattedText(), position.x, position.y + i * (fontRenderer.FONT_HEIGHT + 2), color);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        ITextComponent component = getTextUnderMouse(mouseX, mouseY);
        if (component != null) {
            getWrapScreen().handleComponentHover(component, mouseX, mouseY);
        }
    }

    /**
     * Used to call mc-related chat component handling code,
     * for example component hover rendering and default click handling
     */
    @SideOnly(Side.CLIENT)
    private static class WrapScreen extends GuiScreen {
        @Override
        public void handleComponentHover(@Nonnull ITextComponent component, int x, int y) {
            super.handleComponentHover(component, x, y);
        }

        @Override
        public boolean handleComponentClick(@Nonnull ITextComponent component) {
            return super.handleComponentClick(component);
        }

        @Override
        protected void drawHoveringText(@Nonnull List<String> textLines, int x, int y, @Nonnull FontRenderer font) {
            GuiUtils.drawHoveringText(textLines, x, y, width, height, 256, font);
        }
    }
}
