package gregtech.api.pipenet.block.simple;

import org.jetbrains.annotations.ApiStatus;

@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "2.9")
@SuppressWarnings("ALL")
public class EmptyNodeData {

    public static final EmptyNodeData INSTANCE = new EmptyNodeData();

    private EmptyNodeData() {}
}
