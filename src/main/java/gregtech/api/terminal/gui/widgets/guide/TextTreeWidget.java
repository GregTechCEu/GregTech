package gregtech.api.terminal.gui.widgets.guide;

import com.google.gson.JsonObject;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.terminal.util.TreeNode;
import gregtech.api.util.Position;
import gregtech.api.util.RenderUtil;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class TextTreeWidget<T> extends Widget {
    private static final int ITEM_HEIGHT = 11;
    protected int scrollOffset;
    protected List<TreeNode<String, T>> list;
    protected TreeNode<String, T> selected;
    protected IGuiTexture background;
    protected IGuiTexture nodeTexture;
    protected IGuiTexture leafTexture;
    protected Consumer<TreeNode<String, T>> onSelected;
    protected Function<T, IGuiTexture> iconSupplier;
    protected Function<T, String> nameSupplier;

    public TextTreeWidget(int xPosition, int yPosition, int width, int height,
                          TreeNode<String, T> root,
                          Consumer<TreeNode<String, T>> onSelected,
                          Function<T, IGuiTexture> iconSupplier,
                          Function<T, String> nameSupplier) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        list = new ArrayList<>();
        if (root.children != null) {
            list.addAll(root.children);
        }
        this.onSelected = onSelected;
        this.iconSupplier = iconSupplier;
        this.nameSupplier = nameSupplier;
    }

    public TextTreeWidget<T> setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public TextTreeWidget<T> setNodeTexture(IGuiTexture nodeTexture) {
        this.nodeTexture = nodeTexture;
        return this;
    }

    public TextTreeWidget<T> setLeafTexture(IGuiTexture leafTexture) {
        this.leafTexture = leafTexture;
        return this;
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (this.isMouseOverElement(mouseX, mouseY, true)) {
            int moveDelta = -MathHelper.clamp(wheelDelta, -1, 1) * 5;
            this.scrollOffset = MathHelper.clamp(scrollOffset + moveDelta, 0, Math.max(list.size() * ITEM_HEIGHT - getSize().height, 0));
            return true;
        }
        return false;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position position = getPosition();
        Size size = getSize();
        if (background != null) {
            background.draw(position.x, position.y, size.width, size.height);
        } else {
            drawGradientRect(position.x, position.y, size.width, size.height, 0x8f000000, 0x8f000000);
        }
        RenderUtil.useScissor(position.x, position.y, size.width, size.height, ()->{
            FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
            int minToRender = scrollOffset / ITEM_HEIGHT;
            int maxToRender = Math.min(list.size(), size.height / ITEM_HEIGHT + 2 + minToRender);

            for (int i = minToRender; i < maxToRender; i++) {
                GlStateManager.color(1,1,1,1);
                TreeNode<String, T> node = list.get(i);
                int x = position.x + 10 * node.dimension;
                int y = position.y - scrollOffset + i * ITEM_HEIGHT;
                String name = node.key;
                if (node.isLeaf()) {
                    if (leafTexture != null) {
                        leafTexture.draw(position.x, y, size.width, ITEM_HEIGHT);
                    } else {
                        drawSolidRect(position.x, y, size.width, ITEM_HEIGHT, 0xffff0000);
                    }
                    if (node.content != null) {
                        String nameS = nameSupplier.apply(node.content);
                        name = nameS == null ? name : nameS;
                        IGuiTexture icon = iconSupplier.apply(node.content);
                        if (icon != null) {
                            icon.draw(x - 9, y + 1, 8, 8);
                        }
                    }
                } else {
                    if (nodeTexture != null) {
                        nodeTexture.draw(position.x, y, size.width, ITEM_HEIGHT);
                    } else {
                        drawSolidRect(position.x, y, size.width, ITEM_HEIGHT, 0xffffff00);
                    }
                }
                if (node == selected) {
                    drawSolidRect(position.x, y, size.width, ITEM_HEIGHT, 0x7f000000);
                }
                fr.drawString(I18n.format(name), x, y + 2, 0xff000000);
            }
        });
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            int index = ((mouseY - getPosition().y) + scrollOffset) / ITEM_HEIGHT;
            if (index < list.size()) {
                TreeNode<String, T> node = list.get(index);
                if (node.isLeaf()) {
                    if (node != this.selected) {
                        this.selected = node;
                        onSelected.accept(node);
                    }
                } else if (node.children.size() > 0 && list.contains(node.children.get(0))){
                    for (TreeNode<String, T> child : node.children) {
                        list.remove(child);
                    }
                } else {
                    for (int i = 0; i < node.children.size(); i++) {
                        list.add(index + 1 + i, node.children.get(i));
                    }
                }
                playButtonClickSound();
            }
            return true;
        }
        return false;
    }
}
