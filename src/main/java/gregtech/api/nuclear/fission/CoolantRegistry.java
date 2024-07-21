package gregtech.api.nuclear.fission;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CoolantRegistry {
    private static final Map<Fluid, ICoolantStats> COOLANTS = new Object2ObjectOpenHashMap<>();
    private static final Map<ICoolantStats, Fluid> COOLANTS_INVERSE = new Object2ObjectOpenHashMap<>();


    public static void registerCoolant(@NotNull Fluid fluid, @NotNull ICoolantStats coolant) {
        COOLANTS.put(fluid, coolant);
        COOLANTS_INVERSE.put(coolant, fluid);
    }

    @Nullable
    public static ICoolantStats getCoolant(Fluid fluid) {
        return COOLANTS.get(fluid);
    }

    @Nullable
    public static Fluid originalFluid(ICoolantStats stats) {
        return COOLANTS_INVERSE.get(stats);
    }
}
