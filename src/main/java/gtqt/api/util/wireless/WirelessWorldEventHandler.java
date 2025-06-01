package gtqt.api.util.wireless;

import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class WirelessWorldEventHandler {
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote &&  event.getWorld().provider.getDimension() == 0) {
            NetworkDatabase.get(event.getWorld());
            System.out.println("[Network] 已为维度 " +
                    event.getWorld().provider.getDimension() +
                    " 加载网络数据");
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END &&
                !event.world.isRemote &&
                event.world.provider.getDimension() == 0) {
            if (event.world.getTotalWorldTime() % 600 == 0) {
                NetworkDatabase db = NetworkDatabase.get(event.world);
                if (db.isDirty()) {
                    db.markDirty();
                }
            }
        }
    }
}
