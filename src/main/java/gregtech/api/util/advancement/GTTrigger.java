package gregtech.api.util.advancement;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import gregtech.api.GTValues;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import java.util.Map;

public abstract class GTTrigger<T extends GTInstance> implements ICriterionTrigger<T> {

    private final ResourceLocation ID;
    private final Map<PlayerAdvancements, GTListeners<T>> listeners = Maps.newHashMap();

    public GTTrigger(String name) {
        ID = new ResourceLocation(GTValues.MODID, name);
    }

    public abstract T create();

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addListener(@NotNull PlayerAdvancements playerAdvancementsIn, @NotNull Listener<T> listener) {
        GTListeners<T> gtListener = listeners.get(playerAdvancementsIn);

        if (gtListener == null) {
            gtListener = new GTListeners<>(playerAdvancementsIn);
            listeners.put(playerAdvancementsIn, gtListener);
        }

        gtListener.add(listener);
    }

    @Override
    public void removeListener(@NotNull PlayerAdvancements playerAdvancementsIn, @NotNull Listener<T> listener) {
        GTListeners<T> gtListener = listeners.get(playerAdvancementsIn);

        if (gtListener != null) {
            gtListener.remove(listener);

            if (gtListener.isEmpty()) {
                listeners.remove(playerAdvancementsIn);
            }
        }
    }

    @Override
    public void removeAllListeners(@NotNull PlayerAdvancements playerAdvancementsIn) {
        listeners.remove(playerAdvancementsIn);
    }

    @NotNull
    @Override
    public T deserializeInstance(@NotNull JsonObject json, @NotNull JsonDeserializationContext context) {
        return create();
    }

    public void trigger(EntityPlayerMP player) {
        GTListeners<T> listener = listeners.get(player.getAdvancements());

        if (listener != null) {
            listener.trigger(player);
        }
    }
}
