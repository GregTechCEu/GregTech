package gregtech.api.fission.component;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public abstract class FissionComponentData {

    private String id;

    protected FissionComponentData() {}

    public final @NotNull String id() {
        return this.id;
    }

    @ApiStatus.Internal
    public final void setId(@NotNull String id) {
        this.id = id;
    }
}
