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
import gregtech.api.util.GTLog;
import gregtech.api.util.VirtualTankRegistry;
import gregtech.common.terminal.component.SearchComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class VirtualTankApp extends AbstractApplication implements SearchComponent.IWidgetSearch<Pair<UUID, String>> {

    private WidgetGroup widgetGroup;
    private Map<Pair<UUID, String>, FluidStack> cacheServer;
    @SideOnly(Side.CLIENT)
    private Map<Pair<UUID, String>, IFluidTank> cacheClient;

    public VirtualTankApp() {
        super("vtank_viewer");
    }

    @Override
    public AbstractApplication initApp() {
        this.addWidget(new ImageWidget(5, 5, 333 - 10, 232 - 10, TerminalTheme.COLOR_B_2));
        this.addWidget(new LabelWidget(10, 10, "terminal.vtank_viewer.title", -1));
        this.addWidget(new RectButtonWidget(216, 7, 110, 18)
                .setClickListener(cd->{
                    if (cd.isClient) {
                        reloadWidgets(cacheClient);
                    }
                })
                .setIcon(new TextTexture("terminal.vtank_viewer.refresh", -1))
                .setFill(TerminalTheme.COLOR_B_2.getColor()));
        widgetGroup = new DraggableScrollableWidgetGroup(10, 30, 313, 195)
                .setDraggable(true)
                .setYScrollBarWidth(3)
                .setYBarStyle(null, TerminalTheme.COLOR_F_1);
        if (isClient) {
            cacheClient = new HashMap<>();
        } else {
            cacheServer = new HashMap<>();
        }
        this.addWidget(widgetGroup);
        if (!isRemote()) {
            refresh();
        }
        return this;
    }

    private List<Pair<UUID, String>> findVirtualTanks() {
        List<Pair<UUID, String>> result = new LinkedList<>();
        Map<UUID, Map<String, IFluidTank>> tankMap = VirtualTankRegistry.getTankMap();
        for (UUID uuid : tankMap.keySet().stream().sorted(Comparator.nullsLast(UUID::compareTo)).collect(Collectors.toList())) {
            if (uuid == null || uuid.equals(gui.entityPlayer.getUniqueID())) {
                for (String key : tankMap.get(uuid).keySet().stream().sorted().collect(Collectors.toList())) {
                    result.add(new ImmutablePair<>(uuid, key));
                }
            }
        }
        return result;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (gui.entityPlayer.ticksExisted % 20 == 0) {
            refresh();
        }
    }

    private void refresh() {
        Map<UUID, Map<String, IFluidTank>> tankMap = VirtualTankRegistry.getTankMap();
        Map<Pair<UUID, String>, FluidStack> access = new HashMap<>();
        for (Pair<UUID, String> virtualTankEntry : findVirtualTanks()) {
            UUID uuid = virtualTankEntry.getKey();
            String key = virtualTankEntry.getValue();
            FluidStack fluidStack = tankMap.get(uuid).get(key).getFluid();
            access.put(new ImmutablePair<>(uuid, key), fluidStack == null ? null : fluidStack.copy());
        }
        if (access.keySet().containsAll(cacheServer.keySet()) && access.size() == cacheServer.size()) {
            List<Pair<UUID, String>> toUpdated = new LinkedList<>();
            for (Pair<UUID, String> pair : access.keySet()) {
                FluidStack fluidStackNew = access.get(pair);
                FluidStack fluidStackOld = cacheServer.get(pair);
                if (fluidStackNew == null && fluidStackOld == null) {
                    continue;
                }
                if (fluidStackNew != null && fluidStackOld == null) {
                    toUpdated.add(pair);
                } else if (fluidStackNew == null) {
                    toUpdated.add(pair);
                } else if (fluidStackOld.amount != fluidStackNew.amount || !fluidStackNew.isFluidEqual(fluidStackOld)) {
                    toUpdated.add(pair);
                }
            }
            if (!toUpdated.isEmpty()) {
                writeUpdateInfo(-2, buffer->{ // update specific info
                    buffer.writeVarInt(toUpdated.size());
                    for (Pair<UUID, String> update : toUpdated) {
                        buffer.writeBoolean(update.getKey() != null);
                        if (update.getKey() != null) {
                            buffer.writeString(update.getKey().toString());
                        }
                        buffer.writeString(update.getValue());
                        FluidStack value = access.get(update);
                        buffer.writeBoolean(value != null);
                        if (value != null) {
                            buffer.writeCompoundTag(value.writeToNBT(new NBTTagCompound()));
                        }
                    }
                });
            }
            return;
        }
        cacheServer = access;
        writeUpdateInfo(-1, buffer -> { // update all info
            buffer.writeVarInt(cacheServer.size());
            cacheServer.forEach((key, value) -> {
                buffer.writeBoolean(key.getKey() != null);
                if (key.getKey() != null) {
                    buffer.writeString(key.getKey().toString());
                }
                buffer.writeString(key.getValue());
                buffer.writeBoolean(value != null);
                if (value != null) {
                    buffer.writeCompoundTag(value.writeToNBT(new NBTTagCompound()));
                }
            });
        });
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == -1) { // update all info
            int size = buffer.readVarInt();
            cacheClient.clear();
            try {
                for (int i = 0; i < size; i++) {
                    UUID uuid = null;
                    if (buffer.readBoolean()) {
                        uuid = UUID.fromString(buffer.readString(32767));
                    }
                    String key = buffer.readString(32767);
                    IFluidTank fluidTank = new FluidTank(64000);
                    if (buffer.readBoolean()) {
                        fluidTank.fill(FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag()), true);
                    }
                    cacheClient.put(new ImmutablePair<>(uuid, key), fluidTank);
                }
            } catch (Exception e) {
                GTLog.logger.error("error sync fluid", e);
            }
            reloadWidgets(cacheClient);
        } else if (id == -2) {
            int size = buffer.readVarInt();
            try {
                for (int i = 0; i < size; i++) {
                    UUID uuid = null;
                    if (buffer.readBoolean()) {
                        uuid = UUID.fromString(buffer.readString(32767));
                    }
                    String key = buffer.readString(32767);
                    FluidStack fluidStack = null;
                    if (buffer.readBoolean()) {
                        fluidStack = FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
                    }

                    IFluidTank fluidTank = cacheClient.get(new ImmutablePair<>(uuid, key));
                    if (fluidTank != null) {
                        fluidTank.drain(64000, true);
                        if (fluidStack != null) {
                            fluidTank.fill(fluidStack, true);
                        }
                    }
                }
            } catch (Exception e) {
                GTLog.logger.error("error sync fluid", e);
            }
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    private void reloadWidgets(Map<Pair<UUID, String>, IFluidTank> map) {
        widgetGroup.clearAllWidgets();
        AtomicInteger cy = new AtomicInteger();
        map.forEach((key, fluidTank) -> {
            if (key.getKey() != null) {
                widgetGroup.addWidget(new ImageWidget(0, cy.get() + 4, 8, 8, GuiTextures.LOCK_WHITE));
            }
            widgetGroup.addWidget(new TankWidget(fluidTank, 8, cy.get(), 18, 18)
                    .setAlwaysShowFull(true)
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT).setClient());
            widgetGroup.addWidget(new LabelWidget(36, cy.get() + 5, key.getValue(), -1)
                    .setWidth(180));
            cy.addAndGet(23);
        });
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
        Map<Pair<UUID, String>, IFluidTank> map = new HashMap<>();
        map.put(result, cacheClient.get(result));
        reloadWidgets(map);
    }

    @Override
    public void search(String word, Consumer<Pair<UUID, String>> find) {
        if (!isClient)
            return;
        for (Map.Entry<Pair<UUID, String>, IFluidTank> access : cacheClient.entrySet()) {
            Pair<UUID, String> accessingCover = access.getKey();
            if (accessingCover.getValue() != null && accessingCover.getValue().toLowerCase().contains(word.toLowerCase())) {
                find.accept(accessingCover);
            } else {
                FluidStack fluidStack = access.getValue().getFluid();
                if (fluidStack != null && fluidStack.getLocalizedName().toLowerCase().contains(word.toLowerCase())) {
                    find.accept(accessingCover);
                }
            }
        }
    }
}
