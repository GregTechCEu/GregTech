package gregtech.core.advancement.internal;

import gregtech.api.advancement.IAdvancementCriterion;

import net.minecraft.advancements.ICriterionTrigger.Listener;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

public class AdvancementListeners<T extends IAdvancementCriterion> {

    private final PlayerAdvancements playerAdvancements;
    private final Set<Listener<T>> listeners = Sets.newHashSet();

    public AdvancementListeners(PlayerAdvancements playerAdvancementsIn) {
        playerAdvancements = playerAdvancementsIn;
    }

    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    public void add(Listener<T> listener) {
        listeners.add(listener);
    }

    public void remove(Listener<T> listener) {
        listeners.remove(listener);
    }

    public void trigger(EntityPlayerMP player) {
        List<Listener<T>> list = Lists.newArrayList();

        for (Listener<T> listener : listeners) {
            if (listener.getCriterionInstance().test(player)) {
                list.add(listener);
            }
        }

        if (!list.isEmpty()) {
            for (Listener<T> listener : list) {
                listener.grantCriterion(playerAdvancements);
            }
        }
    }
}
