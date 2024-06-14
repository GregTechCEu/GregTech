package gregtech.common.covers.ender;

import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;

import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;

public abstract class CoverAbstractEnderLink<T> extends CoverBase {

    public CoverAbstractEnderLink(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                  @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }
}
