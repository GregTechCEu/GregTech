package gregtech.common.pipes;

import gregtech.api.pipes.BlockPipe;

public class MetaPipes {
    public static BlockPipe PIPE;

    public static void init() {
        PIPE = new BlockPipe();
        PIPE.setRegistryName("pipe");
    }
}
