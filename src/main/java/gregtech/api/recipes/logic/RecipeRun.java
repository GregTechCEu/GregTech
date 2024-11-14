package gregtech.api.recipes.logic;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.List;

public interface RecipeRun {

    @NotNull
    List<ItemStack> getItemsOut();

    @NotNull
    List<FluidStack> getFluidsOut();

    @NotNull
    List<ItemStack> getItemsConsumed();

    /**
     * Does not need to be serialized; should only be used to consume during recipe run setup, before the run can even
     * be written to nbt.
     */
    @ApiStatus.Internal
    long @Nullable [] getItemArrayConsumption();

    @NotNull
    List<FluidStack> getFluidsConsumed();

    /**
     * Does not need to be serialized; should only be used to consume during recipe run setup, before the run can even
     * be written to nbt.
     */
    @ApiStatus.Internal
    long @Nullable [] getFluidArrayConsumption();

    @Range(from = 1, to = Integer.MAX_VALUE)
    int getParallel();

    @Range(from = 0, to = Integer.MAX_VALUE)
    int getOverclocks();

    double getDuration();

    @Range(from = 0, to = Long.MAX_VALUE)
    long getRequiredVoltage();

    @Range(from = 0, to = Long.MAX_VALUE)
    long getRequiredAmperage();

    boolean isGenerating();

    String getRegistryName();

    NBTTagCompound serializeNBT();
}
