package gregtech.api.mui.sync;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widgets.PagedWidget;

public class PagedWidgetSyncHandler extends SyncHandler {

    private final PagedWidget.Controller controller;
    public static final int SET_PAGE = 0;

    public PagedWidgetSyncHandler(PagedWidget.Controller controller) {
        this.controller = controller;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == SET_PAGE) {
            setPage(buf.readVarInt(), false);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == SET_PAGE) {
            setPage(buf.readVarInt(), false);
        }
    }

    public void setPage(int page) {
        setPage(page, true);
    }

    public void setPage(int page, boolean sync) {
        if (controller.isInitialised() && page != getPage()) {
            controller.setPage(page);
            if (sync) sync(SET_PAGE, buffer -> buffer.writeVarInt(page));
        }
    }

    public int getPage() {
        return controller.getActivePageIndex();
    }
}
