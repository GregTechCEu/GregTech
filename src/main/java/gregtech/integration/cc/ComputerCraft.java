package gregtech.integration.cc;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import gregtech.api.GTValues;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ComputerCraft {
    public static void init() {
        if (!Loader.isModLoaded(GTValues.MODID_COMPUTERCRAFT)) return;

        ComputerCraftAPI.registerPeripheralProvider(new IPeripheralProvider() {
            @Nullable
            @Override
            public IPeripheral getPeripheral(@Nonnull World world, @Nonnull BlockPos blockPos, @Nonnull EnumFacing enumFacing) {
                MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(world, blockPos);

                if (metaTileEntity != null) {
                    CoverBehavior cover = ((ICoverable) metaTileEntity).getCoverAtSide(enumFacing);
                    if (cover instanceof IPeripheralWrapper) {
                        IPeripheral peripheral = ((IPeripheralWrapper) cover).getPeripheral();
                        if (peripheral != null) {
                            return peripheral;
                        }
                    }
                    if (metaTileEntity instanceof IPeripheralWrapper) {
                        IPeripheral peripheral = ((IPeripheralWrapper) metaTileEntity).getPeripheral();
                        if (peripheral != null) {
                            return peripheral;
                        }
                    }
                }
                return null;
            }
        });
    }

}
