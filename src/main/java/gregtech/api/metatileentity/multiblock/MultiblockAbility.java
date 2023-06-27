package gregtech.api.metatileentity.multiblock;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMaintenanceHatch;
import gregtech.api.capability.IMufflerHatch;
import gregtech.api.capability.IRotorHolder;
import gregtech.api.metatileentity.MetaTileEntity;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("InstantiationOfUtilityClass")
public class MultiblockAbility<T> {
    public static final Map<String, MultiblockAbility<?>> NAME_REGISTRY = new HashMap<>();
    public static final Map<MultiblockAbility<?>, List<MetaTileEntity>> REGISTRY = new Object2ObjectOpenHashMap<>();

    public static final MultiblockAbility<IItemHandlerModifiable> EXPORT_ITEMS = new MultiblockAbility<>("export_items");
    public static final MultiblockAbility<IItemHandlerModifiable> IMPORT_ITEMS = new MultiblockAbility<>("import_items");

    public static final MultiblockAbility<IFluidTank> EXPORT_FLUIDS = new MultiblockAbility<>("export_fluids");
    public static final MultiblockAbility<IFluidTank> IMPORT_FLUIDS = new MultiblockAbility<>("import_fluids");

    public static final MultiblockAbility<IEnergyContainer> INPUT_ENERGY = new MultiblockAbility<>("input_energy");
    public static final MultiblockAbility<IEnergyContainer> OUTPUT_ENERGY = new MultiblockAbility<>("output_energy");

    public static final MultiblockAbility<IRotorHolder> ROTOR_HOLDER = new MultiblockAbility<>("rotor_holder");

    public static final MultiblockAbility<IFluidTank> PUMP_FLUID_HATCH = new MultiblockAbility<>("pump_fluid_hatch");

    public static final MultiblockAbility<IFluidTank> STEAM = new MultiblockAbility<>("steam");
    public static final MultiblockAbility<IItemHandlerModifiable> STEAM_IMPORT_ITEMS = new MultiblockAbility<>("steam_import_items");
    public static final MultiblockAbility<IItemHandlerModifiable> STEAM_EXPORT_ITEMS = new MultiblockAbility<>("steam_export_items");

    public static final MultiblockAbility<IMaintenanceHatch> MAINTENANCE_HATCH = new MultiblockAbility<>("maintenance_hatch");
    public static final MultiblockAbility<IMufflerHatch> MUFFLER_HATCH = new MultiblockAbility<>("muffler_hatch");

    public static final MultiblockAbility<IItemHandlerModifiable> MACHINE_HATCH = new MultiblockAbility<>("machine_hatch");

    public static final MultiblockAbility<IFluidHandler> TANK_VALVE = new MultiblockAbility<>("tank_valve");

    public static final MultiblockAbility<IPassthroughHatch> PASSTHROUGH_HATCH = new MultiblockAbility<>("passthrough_hatch");

    public static void registerMultiblockAbility(MultiblockAbility<?> ability, MetaTileEntity part) {
        if (!REGISTRY.containsKey(ability)) {
            REGISTRY.put(ability, new ArrayList<>());
        }
        REGISTRY.get(ability).add(part);
    }

    public MultiblockAbility(String name){
        NAME_REGISTRY.put(name.toLowerCase(), this);
    }
}
