package gregtech.api.recipes.logic.statemachine;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.logic.RecipeRun;
import gregtech.api.recipes.logic.statemachine.overclock.RecipeStandardOverclockingOperator;
import gregtech.api.statemachine.GTStateMachineTransientOperator;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RecipeRunCheckOperator implements GTStateMachineTransientOperator {

    public static final String STANDARD_SUCCESS_KEY = "RunCheckSuccess";
    public static final Predicate<NBTTagCompound> SUCCESS_PREDICATE = t -> t.getBoolean(STANDARD_SUCCESS_KEY);

    protected final @NotNull Predicate<RecipeRun> check;
    protected final String keyRecipe;
    protected final String keySuccess;

    public RecipeRunCheckOperator(@NotNull Predicate<RecipeRun> check) {
        this.check = check;
        this.keyRecipe = RecipeStandardOverclockingOperator.STANDARD_RESULT_KEY;
        this.keySuccess = STANDARD_SUCCESS_KEY;
    }

    public RecipeRunCheckOperator(@NotNull Predicate<RecipeRun> check, String keyRecipe, String keySuccess) {
        this.check = check;
        this.keyRecipe = keyRecipe;
        this.keySuccess = keySuccess;
    }

    @Override
    public void operate(NBTTagCompound data, Map<String, Object> transientData) {
        RecipeRun run = (RecipeRun) transientData.get(keyRecipe);
        data.setBoolean(keySuccess, check.test(run));
    }

    @Contract(pure = true)
    public static @NotNull Predicate<RecipeRun> standardConsumptionCheck(Supplier<IItemHandlerModifiable> itemHandler,
                                                                         Supplier<IMultipleTankHandler> fluidHandler) {
        return run -> standardConsumptionCheck(run, itemHandler.get(), fluidHandler.get());
    }

    public static boolean standardConsumptionCheck(@NotNull RecipeRun run, @Nullable IItemHandlerModifiable itemHandler,
                                                   @Nullable IMultipleTankHandler fluidHandler) {
        List<ItemStack> items = run.getItemsConsumed();
        List<FluidStack> fluids = run.getFluidsConsumed();
        Int2IntOpenHashMap itemConsumptions = new Int2IntOpenHashMap();
        Int2IntOpenHashMap fluidConsumptions = new Int2IntOpenHashMap();
        if (itemHandler != null) {
            for (ItemStack stack : items) {
                if (stack == null || stack.isEmpty()) continue;
                int req = stack.getCount();
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    int taken = itemConsumptions.get(i);
                    ItemStack available = itemHandler.extractItem(i, req + taken, true);
                    if (available.getCount() > taken && ItemStack.areItemStacksEqual(stack, available)) {
                        int get = Math.min(req, available.getCount() - taken);
                        itemConsumptions.put(i, get + taken);
                        req -= get;
                    }
                }
                if (req > 0) return false;
            }
        }
        if (fluidHandler != null) {
            for (FluidStack stack : fluids) {
                if (stack == null || stack.amount <= 0) continue;
                int req = stack.amount;
                for (int i = 0; i < fluidHandler.getTanks(); i++) {
                    int taken = fluidConsumptions.get(i);
                    FluidStack available = fluidHandler.getTankAt(i).drain(req + taken, false);
                    if (available != null && available.amount > taken && stack.isFluidEqual(available)) {
                        int get = Math.min(req, available.amount - taken);
                        fluidConsumptions.put(i, get + taken);
                        req -= get;
                    }
                }
                if (req > 0) return false;
            }
        }
        if (itemHandler != null) {
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                int taken = itemConsumptions.get(i);
                if (taken > 0) itemHandler.extractItem(i, taken, false);
            }
        }
        if (fluidHandler != null) {
            for (int i = 0; i < fluidHandler.getTanks(); i++) {
                int taken = fluidConsumptions.get(i);
                if (taken > 0) fluidHandler.getTankAt(i).drain(taken, true);
            }
        }
        return true;
    }
}
