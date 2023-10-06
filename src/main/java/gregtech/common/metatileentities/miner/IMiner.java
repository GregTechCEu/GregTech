package gregtech.common.metatileentities.miner;

import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.List;

public interface IMiner {

    /**
     * Try to drain all mining resources required for one operation. (e.g. energy, mining fluids)
     *
     * @param simulate Whether this action shouldn't affect the state
     * @return Whether the action succeeded
     */
    boolean drainMiningResources(boolean simulate);

    default void describeMiningResourceStatus(@Nonnull List<ITextComponent> textList) {}
}
