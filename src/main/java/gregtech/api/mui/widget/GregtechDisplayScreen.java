package gregtech.api.mui.widget;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextFormattingUtil;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.HoveredWidgetList;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.scroll.ScrollArea;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widget.sizer.Area;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class GregtechDisplayScreen extends ParentWidget<GregtechDisplayScreen> implements IViewport, Interactable {

    private final DisplaySyncHandler syncHandler;
    private final TextRenderer textRenderer = new TextRenderer();
    private final ScrollArea scroll = new ScrollArea();
    private final MultiblockWithDisplayBase mte;

    public GregtechDisplayScreen(MultiblockWithDisplayBase mte) {
        this.mte = mte;
        this.syncHandler = new DisplaySyncHandler();
        setSyncHandler(this.syncHandler);
        this.syncHandler.setChangeListener(() -> scroll.getScrollY().setScrollSize(syncHandler.getActiveHeight()));
        scroll.setScrollDataY(new VerticalScrollData());
        sizeRel(1f);
        listenGuiAction((IGuiAction.MouseReleased) mouseButton -> {
            this.scroll.mouseReleased(getContext());
            return false;
        });
        addLine(buffer -> NetworkUtils.writeStringSafe(buffer, mte.getMetaFullName()),
                buffer -> KeyUtil.lang(TextFormatting.WHITE, NetworkUtils.readStringSafe(buffer)));
        addLine(buffer -> buffer.writeBoolean(mte.isStructureFormed()), buffer -> {
            if (buffer.readBoolean()) return null;
            return KeyUtil.lang(TextFormatting.RED, "gregtech.multiblock.invalid_structure");
        });
    }

    @Override
    public Area getArea() {
        return this.scroll;
    }

    @Override
    public void getSelfAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (isInside(stack, x, y)) {
            widgets.add(this, stack.peek());
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {}

    @Override
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        // draw the keys
        int x = getArea().getPadding().left;
        int y = getArea().getPadding().top - scroll.getScrollY().getScroll();

        textRenderer.setShadow(widgetTheme.getTextShadow());
        textRenderer.setAlignment(Alignment.CenterLeft, getArea().width, 12);
        textRenderer.setScale(1f);

        for (var key : syncHandler.builtKeys) {
            if (key == null) continue;
            textRenderer.setPos(x, y);
            textRenderer.draw(key.get());
            y += 12;
        }
    }

    @Override
    public void onResized() {
        if (this.scroll.getScrollY() != null) {
            this.scroll.getScrollY().clamp(this.scroll);
        }
    }

    @Override
    public boolean canHover() {
        return super.canHover() ||
                this.scroll.isInsideScrollbarArea(getContext().getMouseX(), getContext().getMouseY());
    }

    @Override
    public @NotNull Interactable.Result onMousePressed(int mouseButton) {
        GuiContext context = getContext();
        if (this.scroll.mouseClicked(context)) {
            return Interactable.Result.STOP;
        }
        return Interactable.Result.IGNORE;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        return this.scroll.mouseScroll(getContext());
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        this.scroll.mouseReleased(getContext());
        return false;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.scroll.drag(getContext().getAbsMouseX(), getContext().getAbsMouseY());
    }

    public GregtechDisplayScreen addLine(Consumer<PacketBuffer> serializer, Function<PacketBuffer, IKey> deserializer) {
        this.syncHandler.addLine(serializer, deserializer);
        return getThis();
    }

    public GregtechDisplayScreen energy(LongSupplier maxVoltage, LongSupplier recipeEUt) {
        addLine(buffer -> {
            long maxV = maxVoltage.getAsLong();
            boolean b = maxV != 0 && maxV >= -recipeEUt.getAsLong();
            buffer.writeBoolean(mte.isStructureFormed() && b);
            if (b) buffer.writeLong(maxV);
        }, buffer -> {
            if (!buffer.readBoolean()) return null;
            long maxV = buffer.readLong();
            // wrap in text component to keep it from being formatted
            var voltageName = KeyUtil.string(
                    GTValues.VOCNF[GTUtility.getFloorTierByVoltage(maxV)]);

            return KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.max_energy_per_tick",
                    TextFormattingUtil.formatNumbers(maxV), voltageName);
        });
        return getThis();
    }

    public GregtechDisplayScreen fuelNeeded(Supplier<String> amount, IntSupplier duration) {
        addLine(buffer -> {
            String s = amount.get();
            buffer.writeBoolean(s != null && mte.isStructureFormed());
            if (s != null) {
                NetworkUtils.writeStringSafe(buffer, s);
                buffer.writeInt(duration.getAsInt());
            }
        }, buffer -> {
            if (!buffer.readBoolean()) return null;
            return KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.turbine.fuel_needed",
                    KeyUtil.string(TextFormatting.RED, NetworkUtils.readStringSafe(buffer)),
                    KeyUtil.number(TextFormatting.AQUA, buffer.readInt()));
        });
        return getThis();
    }

    public GregtechDisplayScreen status() {
        addLine(buffer -> {
            var arl = mte.getRecipeLogic();
            int i;
            if (arl == null) {
                i = 0;
            } else if (!arl.isWorkingEnabled()) {
                i = 1;
            } else if (arl.isActive()) {
                i = 2;
            } else {
                i = 3;
            }
            buffer.writeVarInt(i);
        }, buffer -> switch (buffer.readVarInt()) {
            case 1 -> KeyUtil.lang(TextFormatting.GOLD, "gregtech.multiblock.work_paused");
            case 2 -> KeyUtil.lang(TextFormatting.GREEN, "gregtech.multiblock.running");
            case 3 -> KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.idling");
            default -> null;
        });
        return getThis();
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof DisplaySyncHandler;
    }

    @Override
    public void preDraw(GuiContext context, boolean transformed) {
        if (!transformed) {
            Stencil.applyAtZero(this.scroll, context);
        }
    }

    @Override
    public void postDraw(GuiContext context, boolean transformed) {
        if (!transformed) {
            Stencil.remove();
            this.scroll.drawScrollbar();
        }
    }

    private static class DisplaySyncHandler extends SyncHandler {

        private final List<Consumer<PacketBuffer>> serializers = new ArrayList<>();
        private final List<Function<PacketBuffer, IKey>> deserializers = new ArrayList<>();
        private IKey[] builtKeys = new IKey[0];
        private final PacketBuffer internalBuffer = new PacketBuffer(Unpooled.buffer());
        private boolean dirty = true;
        private Runnable changeListener = null;
        private int activeHeight = 0;

        public void addLine(Consumer<PacketBuffer> serializer, Function<PacketBuffer, IKey> deserializer) {
            serializers.add(serializer);
            deserializers.add(deserializer);
        }

        private void markDirty() {
            this.dirty = true;
        }

        @Override
        public void detectAndSendChanges(boolean init) {
            if (init || dirty) {
                if (init) buildKeys(null);
                if (dirty) dirty = false;
                syncToClient(0, this::serializeKeys);
                if (changeListener != null) changeListener.run();
                return;
            }

            IKey[] copy = builtKeys.clone();
            internalBuffer.clear();
            serializeKeys(internalBuffer);
            buildKeys(internalBuffer);

            for (int i = 0; i < builtKeys.length; i++) {
                if (builtKeys[i] == null && copy[i] == null) continue;
                if (builtKeys[i] == null || copy[i] == null || !builtKeys[i].get().equals(copy[i].get())) {
                    markDirty();
                    return;
                }
            }
        }

        public void setChangeListener(Runnable changeListener) {
            this.changeListener = changeListener;
        }

        private void buildKeys(PacketBuffer buffer) {
            builtKeys = new IKey[deserializers.size()];
            activeHeight = 0;
            if (buffer == null) return;
            Arrays.setAll(builtKeys, i -> {
                var key = deserializers.get(i).apply(buffer);
                if (key != null) activeHeight += 12;
                return key;
            });
        }

        public int getActiveHeight() {
            return activeHeight;
        }

        private void serializeKeys(PacketBuffer buffer) {
            serializers.forEach(s -> s.accept(buffer));
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) throws IOException {
            if (id == 0) {
                builtKeys = new IKey[deserializers.size()];
                for (int i = 0; i < builtKeys.length; i++) {
                    builtKeys[i] = deserializers.get(i).apply(buf);
                }
            }
        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) throws IOException {}
    }
}
