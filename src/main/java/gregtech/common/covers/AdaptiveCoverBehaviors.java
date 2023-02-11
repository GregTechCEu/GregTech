package gregtech.common.covers;

import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.util.IdleTracker;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public abstract class AdaptiveCoverBehaviors extends CoverBehavior implements ITickable {


    public AdaptiveCoverBehaviors(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }
}
