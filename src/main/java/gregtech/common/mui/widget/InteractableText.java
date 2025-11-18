package gregtech.common.mui.widget;

import gregtech.api.util.virtualregistry.VirtualEntry;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widgets.TextWidget;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class InteractableText<T extends VirtualEntry> extends TextWidget<InteractableText<T>> implements Interactable {

    private final T entry;
    private final EntryColorSH syncHandler;

    public InteractableText(T entry, Predicate<String> setter) {
        super(IKey.str(entry.getColorStr())
                .alignment(Alignment.CenterLeft)
                .color(Color.WHITE.darker(1)));
        this.entry = entry;
        this.syncHandler = new EntryColorSH(setter);
        setSyncHandler(this.syncHandler);
    }

    @NotNull
    @Override
    public Result onMousePressed(int mouseButton) {
        if (this.syncHandler.setColor(this.entry.getColorStr())) {
            Interactable.playButtonClickSound();
            this.syncHandler.syncToServer(1, buf -> NetworkUtils.writeStringSafe(buf, this.entry.getColorStr()));
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof EntryColorSH;
    }

    private static class EntryColorSH extends SyncHandler {

        private final Predicate<String> setter;

        private EntryColorSH(Predicate<String> setter) {
            this.setter = setter;
        }

        public boolean setColor(String c) {
            return this.setter.test(c);
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) {}

        @Override
        public void readOnServer(int id, PacketBuffer buf) {
            if (id == 1) {
                setColor(NetworkUtils.readStringSafe(buf));
            }
        }
    }
}
