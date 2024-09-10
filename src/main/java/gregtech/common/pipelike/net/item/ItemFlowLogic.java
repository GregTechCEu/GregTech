package gregtech.common.pipelike.net.item;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.AbstractTransientLogicData;
import gregtech.api.graphnet.logic.NetLogicType;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ItemFlowLogic extends AbstractTransientLogicData<ItemFlowLogic> {

    public static final NetLogicType<ItemFlowLogic> TYPE = new NetLogicType<>(GTValues.MODID, "ItemFlow",
            ItemFlowLogic::new, new ItemFlowLogic());

    public static final int MEMORY_TICKS = 10;

    private final Long2ObjectOpenHashMap<List<ItemStack>> memory = new Long2ObjectOpenHashMap<>();
    private ItemStack last;

    @Override
    public @NotNull NetLogicType<ItemFlowLogic> getType() {
        return TYPE;
    }

    public @NotNull Long2ObjectOpenHashMap<List<ItemStack>> getMemory() {
        updateMemory(FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter());
        return memory;
    }

    public @NotNull List<ItemStack> getFlow(long tick) {
        updateMemory(tick);
        return memory.getOrDefault(tick, Collections.emptyList());
    }

    public void recordFlow(long tick, ItemStack flow) {
        updateMemory(tick);
        memory.computeIfAbsent(tick, k -> new ObjectArrayList<>()).add(flow);
        last = flow;
    }

    public ItemStack getLast() {
        return last;
    }

    private void updateMemory(long tick) {
        var iter = memory.long2ObjectEntrySet().fastIterator();
        while (iter.hasNext()) {
            Long2ObjectMap.Entry<List<ItemStack>> entry = iter.next();
            if (entry.getLongKey() + MEMORY_TICKS < tick) {
                iter.remove();
            }
        }
    }
}
