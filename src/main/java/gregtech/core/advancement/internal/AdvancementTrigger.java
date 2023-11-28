package gregtech.core.advancement.internal;

import gregtech.api.advancement.IAdvancementCriterion;
import gregtech.api.advancement.IAdvancementTrigger;
import gregtech.api.util.GTUtility;

import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AdvancementTrigger<T extends IAdvancementCriterion> implements IAdvancementTrigger<T> {

    private final ResourceLocation id;
    private final T criterion;
    private final Map<PlayerAdvancements, AdvancementListeners<T>> listeners = Maps.newHashMap();

    public AdvancementTrigger(String name, @NotNull T criterion) {
        this.id = GTUtility.gregtechId(name);
        this.criterion = criterion;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void addListener(@NotNull PlayerAdvancements playerAdvancementsIn, @NotNull Listener<T> listener) {
        AdvancementListeners<T> gtListener = listeners.get(playerAdvancementsIn);

        if (gtListener == null) {
            gtListener = new AdvancementListeners<>(playerAdvancementsIn);
            listeners.put(playerAdvancementsIn, gtListener);
        }

        gtListener.add(listener);
    }

    @Override
    public void removeListener(@NotNull PlayerAdvancements playerAdvancementsIn, @NotNull Listener<T> listener) {
        AdvancementListeners<T> gtListener = listeners.get(playerAdvancementsIn);

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
        return criterion;
    }

    @Override
    public void trigger(EntityPlayerMP player) {
        AdvancementListeners<T> listener = listeners.get(player.getAdvancements());

        if (listener != null) {
            listener.trigger(player);
        }
    }
}
