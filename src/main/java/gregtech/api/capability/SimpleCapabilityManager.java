package gregtech.api.capability;

import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.cover.ICoverable;
import gregtech.api.metatileentity.multiblock.IMaintenance;
import gregtech.api.terminal.hardware.HardwareProvider;
import gregtech.api.worldgen.generator.GTWorldGenCapability;
import gregtech.common.metatileentities.converter.ConverterTrait;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class SimpleCapabilityManager {

    /**
     * Registers a capability without any default implementation
     * Forge is stupid enough to disallow null storage and factory
     */
    public static <T> void registerCapabilityWithNoDefault(Class<T> capabilityClass) {
        CapabilityManager.INSTANCE.register(capabilityClass, new Capability.IStorage<T>() {
            @Override
            public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
                throw new UnsupportedOperationException("Not supported");
            }

            @Override
            public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
                throw new UnsupportedOperationException("Not supported");
            }
        }, () -> {
            throw new UnsupportedOperationException("This capability has no default implementation");
        });
    }

    public static void init() {
        registerCapabilityWithNoDefault(IEnergyContainer.class);
        registerCapabilityWithNoDefault(IElectricItem.class);
        registerCapabilityWithNoDefault(IWorkable.class);
        registerCapabilityWithNoDefault(ICoverable.class);
        registerCapabilityWithNoDefault(IControllable.class);
        registerCapabilityWithNoDefault(IActiveOutputSide.class);
        registerCapabilityWithNoDefault(IFuelable.class);
        registerCapabilityWithNoDefault(IMultiblockController.class);
        registerCapabilityWithNoDefault(IMaintenance.class);
        registerCapabilityWithNoDefault(IMultipleRecipeMaps.class);
        registerCapabilityWithNoDefault(AbstractRecipeLogic.class);
        registerCapabilityWithNoDefault(HardwareProvider.class);
        registerCapabilityWithNoDefault(ConverterTrait.class);

        //internal capabilities
        CapabilityManager.INSTANCE.register(GTWorldGenCapability.class, GTWorldGenCapability.STORAGE, GTWorldGenCapability.FACTORY);
    }
}
