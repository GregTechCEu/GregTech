package gregtech.api.mui.sync;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class SingleActionSyncHandler extends SyncHandler {

    @Nullable
    private Runnable clientAction;
    @Nullable
    private Runnable serverAction;

    /**
     * Set the action to run client side.
     */
    public SingleActionSyncHandler clientAction(@Nullable Runnable clientAction) {
        this.clientAction = clientAction;
        return this;
    }

    /**
     * Set the action to run server side.
     */
    public SingleActionSyncHandler serverAction(@Nullable Runnable serverAction) {
        this.serverAction = serverAction;
        return this;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (clientAction != null) {
            clientAction.run();
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (serverAction != null) {
            serverAction.run();
        }
    }

    /**
     * Run the client action immediately if client-side, or tell the client to run the action if server-side.
     */
    public void notifyClient() {
        if (getSyncManager().isClient()) {
            if (clientAction != null) {
                clientAction.run();
            }
        } else {
            syncToClient(0);
        }
    }

    /**
     * Run the server action immediately if server-side, or tell the server to run the action if client-side.
     */
    public void notifyServer() {
        if (getSyncManager().isClient()) {
            syncToServer(0);
        } else {
            if (serverAction != null) {
                serverAction.run();
            }
        }
    }
}
