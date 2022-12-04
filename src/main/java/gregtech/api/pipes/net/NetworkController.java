package gregtech.api.pipes.net;

import java.util.ArrayList;

public class NetworkController {
    public static NetworkController INSTANCE = new NetworkController();

    public ArrayList<PipeNetwork> networks = new ArrayList<>();

    public NetworkController() {}

    public void register(PipeNetwork net) {
        this.networks.add(net);
    }

    public void deregister(PipeNetwork net) {
        this.networks.remove(net);
    }
}
