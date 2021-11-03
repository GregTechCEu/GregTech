package gregtech.common.terminal.app;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.terminal.os.menu.IMenuComponent;
import gregtech.api.util.VirtualTankRegistry;
import gregtech.common.terminal.component.SearchComponent;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class VirtualTankApp extends AbstractApplication implements SearchComponent.IWidgetSearch<Pair<UUID, String>> {

    private WidgetGroup widgetGroup;

    public VirtualTankApp() {
        super("vtank_viewer");
    }

    @Override
    public AbstractApplication initApp() {
        this.addWidget(new ImageWidget(5, 5, 333 - 10, 232 - 10, TerminalTheme.COLOR_B_2));
        this.addWidget(new LabelWidget(10, 10, "terminal.vtank_viewer.title", -1));
        this.addWidget(new RectButtonWidget(216, 7, 110, 18)
                .setClickListener(this::onRefreshClick)
                .setIcon(new TextTexture("terminal.vtank_viewer.refresh", -1))
                .setFill(TerminalTheme.COLOR_B_2.getColor()));
        widgetGroup = new DraggableScrollableWidgetGroup(10, 30, 313, 195)
                .setDraggable(true)
                .setYScrollBarWidth(3)
                .setYBarStyle(null, TerminalTheme.COLOR_F_1);
        this.addWidget(widgetGroup);
        refresh(null);
        return this;
    }

    private List<Pair<UUID, String>> findAccessingCovers() {
        List<Pair<UUID, String>> result = new LinkedList<>();
        Map<UUID, Map<String, IFluidTank>> tankMap = VirtualTankRegistry.getTankMap();
        for (UUID uuid : tankMap.keySet().stream().sorted(Comparator.nullsLast(UUID::compareTo)).collect(Collectors.toList())) {
            if (uuid == null || uuid == gui.entityPlayer.getUniqueID()) {
                for (String key : tankMap.get(uuid).keySet().stream().sorted().collect(Collectors.toList())) {
                    result.add(new ImmutablePair<>(uuid, key));
                }
            }
        }
        return result;
    }

    private void refresh(Pair<UUID, String> filter) {
        Map<UUID, Map<String, IFluidTank>> tankMap = VirtualTankRegistry.getTankMap();
        widgetGroup.clearAllWidgets();
        int cy = 0;
        for (Pair<UUID, String> accessingCover : findAccessingCovers()) {
            if (filter != null && !filter.equals(accessingCover)) {
                continue;
            }
            UUID uuid = accessingCover.getKey();
            String key = accessingCover.getValue();
            if (uuid != null) {
                widgetGroup.addWidget(new ImageWidget(0, cy + 4, 8, 8, GuiTextures.LOCK_WHITE));
            }
            widgetGroup.addWidget(new TankWidget(tankMap.get(uuid).get(key), 8, cy, 18, 18)
                    .setAlwaysShowFull(true)
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT));
            widgetGroup.addWidget(new LabelWidget(36, cy + 5, key, -1)
                    .setWidth(180));
            cy += 23;
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == -1) { // ask for refresh
            UUID uuid = null;
            if (buffer.readBoolean()) {
                uuid  = UUID.fromString(buffer.readString(32767));
            }
            String key = buffer.readString(32767);
            refresh(new ImmutablePair<>(uuid, key));
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    private void onRefreshClick(ClickData clickData) {
        refresh(null);
    }

    @Override
    public List<IMenuComponent> getMenuComponents() {
        return Collections.singletonList(new SearchComponent<>(this));
    }

    @Override
    public String resultDisplay(Pair<UUID, String> result) {
        FluidStack fluidStack = VirtualTankRegistry.getTankMap().get(result.getKey()).get(result.getValue()).getFluid();
        return String.format("Lock: %b, ID: %s, Fluid: %s", result.getKey() != null, result.getValue(), fluidStack == null ? "-" : fluidStack.getLocalizedName());
    }

    @Override
    public void selectResult(Pair<UUID, String> result) {
        writeClientAction(-1, buffer->{
            buffer.writeBoolean(result.getKey() != null);
            if (result.getKey() != null) {
                buffer.writeString(result.getKey().toString());
            }
            buffer.writeString(result.getValue());
        });
        refresh(result);
    }

    @Override
    public void search(String word, Consumer<Pair<UUID, String>> find) {
        Map<UUID, Map<String, IFluidTank>> tankMap = VirtualTankRegistry.getTankMap();
        for (Pair<UUID, String> accessingCover : findAccessingCovers()) {
            if (accessingCover.getValue() != null && accessingCover.getValue().toLowerCase().contains(word.toLowerCase())) {
                find.accept(accessingCover);
            } else {
                FluidStack fluidStack = tankMap.get(accessingCover.getKey()).get(accessingCover.getValue()).getFluid();
                if (fluidStack != null && fluidStack.getLocalizedName().toLowerCase().contains(word.toLowerCase())) {
                    find.accept(accessingCover);
                }
            }
        }
    }
}
