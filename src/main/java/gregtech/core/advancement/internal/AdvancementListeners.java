package gregtech.core.advancement.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gregtech.api.advancement.IAdvancementInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;
import java.util.Set;

public class AdvancementListeners<T extends IAdvancementInstance> {

    private final PlayerAdvancements playerAdvancements;
    private final Set<ICriterionTrigger.Listener<T>> listeners = Sets.newHashSet();

    public AdvancementListeners(PlayerAdvancements playerAdvancementsIn) {
        playerAdvancements = playerAdvancementsIn;
    }

    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    public void add(ICriterionTrigger.Listener<T> listener) {
        listeners.add(listener);
    }

    public void remove(ICriterionTrigger.Listener<T> listener) {
        listeners.remove(listener);
    }

    public void trigger(EntityPlayerMP player) {
        List<ICriterionTrigger.Listener<T>> list = Lists.newArrayList();

        for (ICriterionTrigger.Listener<T> listener : listeners) {
            if (listener.getCriterionInstance().test(player)) {
                list.add(listener);
            }
        }

        if (!list.isEmpty()) {
            for (ICriterionTrigger.Listener<T> listener : list) {
                listener.grantCriterion(playerAdvancements);
            }
        }
    }
}
