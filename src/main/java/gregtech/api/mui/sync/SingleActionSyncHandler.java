package gregtech.api.mui.sync;

import gregtech.api.GTValues;

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
    public void clientAction(@Nullable Runnable clientAction) {
        if (!GTValues.isClientSide()) return;
        this.clientAction = clientAction;
    }

    /**
     * Set the action to run server side.
     */
    public void serverAction(@Nullable Runnable serverAction) {
        if (GTValues.isClientSide()) return;
        this.serverAction = serverAction;
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
