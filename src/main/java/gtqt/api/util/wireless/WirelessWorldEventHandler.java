package gtqt.api.util.wireless;

import gregtech.api.util.GTUtility;

import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

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
                db.getNetworks().keySet().forEach(x->{
                    var net = db.getNetwork(x);
                    if(net!=null)
                    {
                        List<WorldBlockPos> pos = new ArrayList<>();
                        for (var machine:net.machines)
                        {
                            if(machine.getDimension()==event.world.provider.getDimension() && event.world.isBlockLoaded(machine.getPos()) )
                            {
                                var mte = GTUtility.getMetaTileEntity(event.world,machine.getPos());
                                if(mte==null)
                                {
                                    pos.add(machine);
                                }
                            }
                        }
                        for (var remove:pos)
                        {
                            net.machines.remove(remove);
                        }
                    }
                });
                if (db.isDirty()) {
                    db.markDirty();
                }
            }
        }
    }
}
