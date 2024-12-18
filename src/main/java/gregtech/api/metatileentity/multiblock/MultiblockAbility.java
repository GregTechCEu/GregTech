package gregtech.api.metatileentity.multiblock;

import gregtech.api.capability.*;
import gregtech.api.fission.component.FissionComponent;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("InstantiationOfUtilityClass")
public class MultiblockAbility<T> {

    public static final Map<String, MultiblockAbility<?>> NAME_REGISTRY = new HashMap<>();
    public static final Map<MultiblockAbility<?>, List<MetaTileEntity>> REGISTRY = new Object2ObjectOpenHashMap<>();

    public static final MultiblockAbility<IItemHandlerModifiable> EXPORT_ITEMS = new MultiblockAbility<>(
            "export_items");
    public static final MultiblockAbility<IItemHandlerModifiable> IMPORT_ITEMS = new MultiblockAbility<>(
            "import_items");

    public static final MultiblockAbility<IFluidTank> EXPORT_FLUIDS = new MultiblockAbility<>("export_fluids");
    public static final MultiblockAbility<IFluidTank> IMPORT_FLUIDS = new MultiblockAbility<>("import_fluids");

    public static final MultiblockAbility<IEnergyContainer> INPUT_ENERGY = new MultiblockAbility<>("input_energy");
    public static final MultiblockAbility<IEnergyContainer> OUTPUT_ENERGY = new MultiblockAbility<>("output_energy");

    public static final MultiblockAbility<IEnergyContainer> SUBSTATION_INPUT_ENERGY = new MultiblockAbility<>(
            "substation_input_energy");
    public static final MultiblockAbility<IEnergyContainer> SUBSTATION_OUTPUT_ENERGY = new MultiblockAbility<>(
            "substation_output_energy");

    public static final MultiblockAbility<IRotorHolder> ROTOR_HOLDER = new MultiblockAbility<>("rotor_holder");

    public static final MultiblockAbility<IFluidTank> PUMP_FLUID_HATCH = new MultiblockAbility<>("pump_fluid_hatch");

    public static final MultiblockAbility<IFluidTank> STEAM = new MultiblockAbility<>("steam");
    public static final MultiblockAbility<IItemHandlerModifiable> STEAM_IMPORT_ITEMS = new MultiblockAbility<>(
            "steam_import_items");
    public static final MultiblockAbility<IItemHandlerModifiable> STEAM_EXPORT_ITEMS = new MultiblockAbility<>(
            "steam_export_items");

    public static final MultiblockAbility<IMaintenanceHatch> MAINTENANCE_HATCH = new MultiblockAbility<>(
            "maintenance_hatch");
    public static final MultiblockAbility<IMufflerHatch> MUFFLER_HATCH = new MultiblockAbility<>("muffler_hatch");

    public static final MultiblockAbility<IItemHandlerModifiable> MACHINE_HATCH = new MultiblockAbility<>(
            "machine_hatch");

    public static final MultiblockAbility<IFluidHandler> TANK_VALVE = new MultiblockAbility<>("tank_valve");

    public static final MultiblockAbility<IPassthroughHatch> PASSTHROUGH_HATCH = new MultiblockAbility<>(
            "passthrough_hatch");

    public static final MultiblockAbility<IDataAccessHatch> DATA_ACCESS_HATCH = new MultiblockAbility<>(
            "data_access_hatch");
    public static final MultiblockAbility<IOpticalDataAccessHatch> OPTICAL_DATA_RECEPTION = new MultiblockAbility<>(
            "optical_data_reception");
    public static final MultiblockAbility<IOpticalDataAccessHatch> OPTICAL_DATA_TRANSMISSION = new MultiblockAbility<>(
            "optical_data_transmission");
    public static final MultiblockAbility<ILaserContainer> INPUT_LASER = new MultiblockAbility<>("input_laser");
    public static final MultiblockAbility<ILaserContainer> OUTPUT_LASER = new MultiblockAbility<>("output_laser");

    public static final MultiblockAbility<IOpticalComputationHatch> COMPUTATION_DATA_RECEPTION = new MultiblockAbility<>(
            "computation_data_reception");
    public static final MultiblockAbility<IOpticalComputationHatch> COMPUTATION_DATA_TRANSMISSION = new MultiblockAbility<>(
            "computation_data_transmission");

    public static final MultiblockAbility<IHPCAComponentHatch> HPCA_COMPONENT = new MultiblockAbility<>(
            "hpca_component");
    public static final MultiblockAbility<IObjectHolder> OBJECT_HOLDER = new MultiblockAbility<>("object_holder");

    // public static final MultiblockAbility<EmptyComponent> EMPTY_FISSION_COMPONENT = new
    // MultiblockAbility<>("empty_fission_component");
    // public static final MultiblockAbility<ControlRodComponent> CONTROL_ROD = new MultiblockAbility<>("control_rod");
    // public static final MultiblockAbility<CoolingComponent> COOLING_COMPONENT = new
    // MultiblockAbility<>("cooling_component");
    // public static final MultiblockAbility<FuelComponent> FISSION_FUEL = new MultiblockAbility<>("fission_fuel");
    // public static final MultiblockAbility<ModeratorComponent> NEUTRON_MODERATOR = new
    // MultiblockAbility<>("neutron_moderator");
    // public static final MultiblockAbility<NeutronEmitter> NEUTRON_EMITTER = new
    // MultiblockAbility<>("neutron_emitter");
    // public static final MultiblockAbility<ReflectorComponent> NEUTRON_REFLECTOR = new
    // MultiblockAbility<>("neutron_reflector");
    public static final MultiblockAbility<FissionComponent> FISSION_COMPONENT = new MultiblockAbility<>(
            "fission_component");

    public static void registerMultiblockAbility(MultiblockAbility<?> ability, MetaTileEntity part) {
        if (!REGISTRY.containsKey(ability)) {
            REGISTRY.put(ability, new ArrayList<>());
        }
        REGISTRY.get(ability).add(part);
    }

    public MultiblockAbility(String name) {
        NAME_REGISTRY.put(name.toLowerCase(), this);
    }
}
