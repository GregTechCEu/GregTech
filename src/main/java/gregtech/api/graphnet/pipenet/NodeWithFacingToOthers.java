package gregtech.api.graphnet.pipenet;

import gregtech.api.graphnet.net.NetNode;

import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NodeWithFacingToOthers {

    @Nullable
    EnumFacing getFacingToOther(@NotNull NetNode other);
}
