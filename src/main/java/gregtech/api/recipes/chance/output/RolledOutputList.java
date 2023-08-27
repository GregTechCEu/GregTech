package gregtech.api.recipes.chance.output;

import gregtech.api.capability.IMultipleTankHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A list of chanced outputs whose chances have already been rolled
 */
public final class RolledOutputList {

    private final List<ChancedOutput<?>> producedOutputs;

    public RolledOutputList(@NotNull List<@NotNull ChancedOutput<?>> producedOutputs) {
        this.producedOutputs = producedOutputs;
    }

    /**
     * Output the rolled ingredients
     *
     * @param itemInventory  the item inventory to insert into
     * @param fluidInventory the fluid inventory to insert into
     * @param simulate       if output should only be tested
     * @return if all ingredients were or can be output successfully
     */
    public boolean output(@NotNull IItemHandler itemInventory, @NotNull IMultipleTankHandler fluidInventory, boolean simulate) {
        for (ChancedOutput<?> output : producedOutputs) {
            if (!output.addToInventory(itemInventory, fluidInventory, simulate)) {
                return false;
            }
        }
        return true;
    }
}
