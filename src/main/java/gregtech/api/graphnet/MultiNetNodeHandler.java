package gregtech.api.graphnet;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

public class MultiNetNodeHandler {

    protected final int allowedActiveNets;
    protected final int timeoutDelay;

    protected final Object2LongOpenHashMap<IGraphNet> activeNets = new Object2LongOpenHashMap<>();

    public MultiNetNodeHandler(int allowedActiveNets, int timeoutDelay) {
        this.allowedActiveNets = allowedActiveNets;
        this.timeoutDelay = timeoutDelay;
    }

    public boolean traverse(IGraphNet net, long queryTick, boolean simulate) {
        timeout(queryTick);
        if (activeNets.size() < allowedActiveNets || activeNets.containsKey(net)) {
            if (!simulate) activeNets.put(net, queryTick + timeoutDelay);
            return true;
        } else return false;
    }

    private void timeout(long queryTick) {
        var iter = activeNets.object2LongEntrySet().fastIterator();
        while (iter.hasNext()) {
            var next = iter.next();
            if (next.getLongValue() <= queryTick) iter.remove();
        }
    }
}
