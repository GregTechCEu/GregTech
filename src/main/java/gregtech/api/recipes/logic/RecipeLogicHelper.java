package gregtech.api.recipes.logic;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GuardedData;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public class RecipeLogicHelper {

    protected final GuardedData<Map<MapKey<?>, Object>> data;

    public RecipeLogicHelper(GuardedData<Map<MapKey<?>, Object>> data) {
        this.data = data;
    }

    protected Map<MapKey<?>, Object> tData() {
        return data.getTransientData();
    }
}
