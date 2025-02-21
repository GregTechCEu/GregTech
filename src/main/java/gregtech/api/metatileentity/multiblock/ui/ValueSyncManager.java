package gregtech.api.metatileentity.multiblock.ui;

import com.cleanroommc.modularui.api.value.sync.IValueSyncHandler;
import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;
import com.cleanroommc.modularui.utils.serialization.IEquals;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import com.cleanroommc.modularui.value.sync.ValueSyncHandler;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class ValueSyncManager extends SyncHandler {

    private final List<ValueSyncHandler<?>> values = new ArrayList<>();

    @Override
    public void detectAndSendChanges(boolean init) {
        for (int i = 0; i < values.size(); i++) {
            ValueSyncHandler<?> vsh = values.get(i);
            if (vsh.updateCacheFromSource(init)) {
                syncToClient(i, vsh::write);
            }
        }

    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        values.get(id).read(buf);
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {}

    public interface ValueUpdatable {
        boolean hasChanged();
        void write(PacketBuffer buf);
        void read(PacketBuffer buf);
    }

    public static class Value<T> implements ValueUpdatable {

        T value;
        final Supplier<T> getter;
        final IEquals<T> equals;

        public Value(Supplier<T> getter) {
            this(getter, Objects::equals);
        }

        public Value(Supplier<T> getter, IEquals<T> equals) {
            this.getter = getter;
            this.equals = equals;
        }

        @Override
        public boolean hasChanged() {
            T newVal = this.getter.get();
            if (!this.equals.areEqual(this.value, newVal)) {
                this.value = newVal;
                return true;
            }
            return false;
        }

        @Override
        public void write(PacketBuffer buf) {

        }

        @Override
        public void read(PacketBuffer buf) {

        }
    }
}
