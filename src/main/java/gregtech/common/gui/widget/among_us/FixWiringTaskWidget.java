package gregtech.common.gui.widget.among_us;

import gregtech.api.GTValues;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec2f;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FixWiringTaskWidget extends Widget {

    private final static TextureArea background = TextureArea
            .fullImage("textures/gui/widget/electricity_wires_baseback.png");
    private final static TextureArea wire = TextureArea.fullImage("textures/gui/widget/electricity_wires.png");
    private final static TextureArea wire_base = TextureArea
            .fullImage("textures/gui/widget/electricity_wires_base.png");
    private final int[] colors = new int[4];
    private final boolean[] connect = new boolean[4];
    private final int[] init = new int[4];
    private Consumer<Integer> onConnected;
    private Runnable onFinished;
    private BiPredicate<Integer, ItemStack> hoverItemCheck;
    private int dragged = -1;
    private Supplier<Boolean> canInteractPredicate = () -> true;

    public FixWiringTaskWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
        for (int i = 0; i < 4; i++) {
            colors[i] = (GTValues.RNG.nextInt() & 0x00ffffff) | 0xff000000;
        }
        List<Integer> list = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(list);
        for (int i = 0; i < list.size(); i++) {
            init[i] = list.get(i);
        }
    }

    public FixWiringTaskWidget setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
        return this;
    }

    public FixWiringTaskWidget setOnConnected(Consumer<Integer> onConnected) {
        this.onConnected = onConnected;
        return this;
    }

    public FixWiringTaskWidget setHoverItemCheck(BiPredicate<Integer, ItemStack> hoverItemCheck) {
        this.hoverItemCheck = hoverItemCheck;
        return this;
    }

    public FixWiringTaskWidget setCanInteractPredicate(Supplier<Boolean> canInteractPredicate) {
        this.canInteractPredicate = canInteractPredicate;
        return this;
    }

    public void setColor(int index, int color) {
        this.colors[index] = color;
    }

    public boolean getConnected(int index) {
        return connect[index];
    }

    public int isMouseOverWire(int mouseX, int mouseY, boolean isLeft) {
        if (!canInteractPredicate.get()) return -1;
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        float xScale = width / 504f;
        float yScale = height / 504f;
        for (int i = 0; i < 4; i++) {
            double y1 = y + yScale * ((i + 1) * 104 - 10);
            if (isLeft) {
                if (isMouseOver(x, (int) (y1 - yScale * 16), (int) (xScale * 38 * 1.5), (int) (yScale * 64), mouseX,
                        mouseY)) {
                    return i;
                }
            } else {
                if (isMouseOver(x + width - (int) (xScale * 38 * 1.5), (int) (y1 - yScale * 16),
                        (int) (xScale * 38 * 1.5), (int) (yScale * 64), mouseX, mouseY)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        background.draw(x, y, width, height);
        float xScale = width / 504f;
        float yScale = height / 504f;
        // connected
        for (int i = 0; i < 4; i++) {
            double y1 = y + yScale * ((i + 1) * 104 - 10);
            int color = colors[i];
            if (connect[i]) {
                for (int j = 0; j < init.length; j++) {
                    if (init[j] == i) {
                        double y2 = y + yScale * ((j + 1) * 104 - 10);
                        drawLines(Arrays.asList(new Vec2f(x + xScale * 25, (float) y1 + yScale * 12),
                                new Vec2f(x + width - (int) (xScale * 25), (float) y2 + yScale * 12)), color, color, 5);
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            double y1 = y + yScale * ((i + 1) * 104 - 10);
            int color = colors[i];

            if (dragged == i) {
                drawLines(
                        Arrays.asList(new Vec2f(x + xScale * 25, (float) y1 + yScale * 12), new Vec2f(mouseX, mouseY)),
                        color, color, 5);
            }

            // left
            wire.drawSubArea(x + (int) (xScale * 25), y1 - yScale * 5, (int) (xScale * 38), (int) (yScale * 32), 0, 0,
                    0.5, 1);
            setColor(color);
            wire_base.drawSubArea(x, (int) y1, (int) (xScale * 50), (int) (yScale * 32), 0, 0, 0.5, 1);
            GlStateManager.color(1, 1, 1, 1);

            // right
            wire.drawSubArea(x + width - (int) (xScale * (25 + 38)), y1 - yScale * 5, (int) (xScale * 38),
                    (int) (yScale * 32), 0.5, 0, 0.5, 1);
            setColor(colors[init[i]]);
            wire_base.drawSubArea(x + width - (int) (xScale * 50), (int) y1, (int) (xScale * 50), (int) (yScale * 32),
                    0.5, 0, 0.5, 1);
            GlStateManager.color(1, 1, 1, 1);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        int over = isMouseOverWire(mouseX, mouseY, true);
        ItemStack holdStack = this.gui.entityPlayer.inventory.getItemStack();
        if (over != -1 && !connect[over] && (hoverItemCheck == null || hoverItemCheck.test(over, holdStack))) {
            dragged = over;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        return super.mouseDragged(mouseX, mouseY, button, timeDragged);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        if (dragged != -1) {
            int over = isMouseOverWire(mouseX, mouseY, false);
            if (over != -1 && init[over] == dragged) {
                connect[dragged] = true;
                writeClientAction(5, buffer -> buffer.writeVarInt(dragged));
            }
        }
        dragged = -1;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 5) {
            int connected = buffer.readVarInt();
            connect[connected] = true;
            if (onConnected != null) {
                onConnected.accept(connected);
            }
            if (connect[0] && connect[1] && connect[2] && connect[3] && onFinished != null) {
                onFinished.run();
            }
        }
    }
}
