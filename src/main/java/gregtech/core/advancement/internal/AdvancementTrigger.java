package gregtech.core.advancement.internal;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import gregtech.api.GTValues;
import gregtech.api.advancement.IAdvancementCriterion;
import gregtech.api.advancement.IAdvancementTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Map;

public class AdvancementTrigger<T extends IAdvancementCriterion> implements IAdvancementTrigger<T> {

    private final ResourceLocation id;
    private final T criterion;
    private final Map<PlayerAdvancements, AdvancementListeners<T>> listeners = Maps.newHashMap();

    public AdvancementTrigger(String name, @Nonnull T criterion) {
        this.id = new ResourceLocation(GTValues.MODID, name);
        this.criterion = criterion;
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void addListener(@Nonnull PlayerAdvancements playerAdvancementsIn, @Nonnull Listener<T> listener) {
        AdvancementListeners<T> gtListener = listeners.get(playerAdvancementsIn);

        if (gtListener == null) {
            gtListener = new AdvancementListeners<>(playerAdvancementsIn);
            listeners.put(playerAdvancementsIn, gtListener);
        }

        gtListener.add(listener);
    }

    @Override
    public void removeListener(@Nonnull PlayerAdvancements playerAdvancementsIn, @Nonnull Listener<T> listener) {
        AdvancementListeners<T> gtListener = listeners.get(playerAdvancementsIn);

        if (gtListener != null) {
            gtListener.remove(listener);

            if (gtListener.isEmpty()) {
                listeners.remove(playerAdvancementsIn);
            }
        }
    }

    @Override
    public void removeAllListeners(@Nonnull PlayerAdvancements playerAdvancementsIn) {
        listeners.remove(playerAdvancementsIn);
    }

    @Nonnull
    @Override
    public T deserializeInstance(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context) {
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
