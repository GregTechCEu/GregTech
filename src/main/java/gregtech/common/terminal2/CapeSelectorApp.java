package gregtech.common.terminal2;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.terminal2.ITerminalApp;
import gregtech.api.terminal2.Terminal2Theme;
import gregtech.api.util.CapesRegistry;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class CapeSelectorApp implements ITerminalApp {

    private static final int SYNC_UNLOCKED_CAPES = 1;
    private static final int SELECT_CAPE = 2;

    private CapeSelectorSyncHandler syncHandler;

    @Override
    public IWidget buildWidgets(HandGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings,
                                ModularPanel panel) {
        UUID uuid = guiData.getPlayer().getPersistentID();
        syncHandler = new CapeSelectorSyncHandler(uuid);
        guiSyncManager.syncValue("cape_handler", syncHandler);

        var capeGrid = new Grid()
                .sizeRel(1f)
                .posRel(0.5f, 0.5f)
                .minElementMargin(18)
                .scrollable(new VerticalScrollData().texture(Terminal2Theme.COLOR_FOREGROUND_BRIGHT))
                .nextRow();

        for (ResourceLocation cape : CapesRegistry.allCapes()) {
            if (capeGrid.getChildren().size() % 4 == 0 && !capeGrid.getChildren().isEmpty()) {
                capeGrid.nextRow();
            }

            IWidget capeButton = new ButtonWidget<>()
                    .size(40, 72)
                    .posRel(0.5F, 0.5F)
                    .background(new UITexture(cape, 1f / 64, 1f / 32, 11f / 64, 17f / 32, null, false))
                    .overlay(new DynamicDrawable(capeForeground(cape)))
                    .disableHoverOverlay()
                    .disableHoverBackground()
                    .onMousePressed(b -> {
                        syncHandler.syncToServer(SELECT_CAPE, buf -> buf.writeString(cape.toString()));
                        return true;
                    });

            capeGrid.child(new ParentWidget<>()
                    .size(46, 78)
                    .background(new DynamicDrawable(capeBackground(cape)))
                    .child(capeButton)
                    .child(new DynamicDrawable(capeOverlay(cape)).asWidget()
                            .size(24)
                            .posRel(0.5F, 0.5F)));
        }

        return new ParentWidget<>()
                .sizeRel(0.98f)
                .posRel(0.5f, 0.5f)
                .background(Terminal2Theme.COLOR_BACKGROUND_1)
                .child(capeGrid);
    }

    @Override
    public IDrawable getIcon() {
        return GTGuiTextures.CAPES_APP_ICON;
    }

    @Override
    public void onOpen() {
        syncHandler.syncUnlockedCapes();
    }

    @Override
    public void dispose() {
        syncHandler = null;
    }

    private Supplier<IDrawable> capeBackground(ResourceLocation cape) {
        return () -> {
            if (cape.equals(CapesRegistry.getPlayerCape(syncHandler.uuid))) {
                return Terminal2Theme.COLOR_BRIGHT_1;
            } else if (syncHandler.unlockedCapes.contains(cape)) {
                return Terminal2Theme.COLOR_BACKGROUND_2;
            }
            return Terminal2Theme.COLOR_BRIGHT_3;
        };
    }

    private Supplier<IDrawable> capeOverlay(ResourceLocation cape) {
        return () -> syncHandler.unlockedCapes.contains(cape) ? IDrawable.EMPTY : GTGuiTextures.RECIPE_LOCK_WHITE;
    }

    private Supplier<IDrawable> capeForeground(ResourceLocation cape) {
        return () -> syncHandler.unlockedCapes.contains(cape) ? IDrawable.EMPTY : Terminal2Theme.COLOR_FOREGROUND_DARK;
    }

    private static final class CapeSelectorSyncHandler extends SyncHandler {

        private final UUID uuid;
        private List<ResourceLocation> unlockedCapes = Collections.emptyList();

        public CapeSelectorSyncHandler(UUID uuid) {
            this.uuid = uuid;
        }

        private void syncUnlockedCapes() {
            if (!getSyncManager().isClient()) {
                unlockedCapes = CapesRegistry.getUnlockedCapes(uuid);
                syncToClient(SYNC_UNLOCKED_CAPES, buf -> {
                    buf.writeVarInt(unlockedCapes.size());
                    for (ResourceLocation cape : unlockedCapes) {
                        buf.writeString(cape.toString());
                    }
                });
            } else {
                syncToServer(SYNC_UNLOCKED_CAPES);
            }
        }

        @Override
        public void detectAndSendChanges(boolean init) {
            super.detectAndSendChanges(init);
            if (init) {
                // prevents flicker when opening the app on dedicated server
                syncUnlockedCapes();
            }
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) {
            if (id == SYNC_UNLOCKED_CAPES) {
                int len = buf.readVarInt();
                unlockedCapes = new ArrayList<>(len);
                for (int i = 0; i < len; i++) {
                    unlockedCapes.add(new ResourceLocation(buf.readString(Short.MAX_VALUE)));
                }
            }
        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) {
            if (id == SELECT_CAPE) {
                ResourceLocation cape = new ResourceLocation(buf.readString(Short.MAX_VALUE));
                if (unlockedCapes.contains(cape)) {
                    if (cape.equals(CapesRegistry.getPlayerCape(uuid))) {
                        CapesRegistry.giveCape(uuid, null);
                    } else {
                        CapesRegistry.giveCape(uuid, cape);
                    }
                }
            } else if (id == SYNC_UNLOCKED_CAPES) {
                syncUnlockedCapes();
            }
        }
    }
}
