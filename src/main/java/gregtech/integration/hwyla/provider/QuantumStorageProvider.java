package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.IQuantumController;
import gregtech.api.capability.IQuantumStorage;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QuantumStorageProvider implements IWailaDataProvider {

    public static final QuantumStorageProvider INSTANCE = new QuantumStorageProvider();

    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, IGregTechTileEntity.class);
        registrar.registerNBTProvider(this, IGregTechTileEntity.class);
        registrar.addConfig(GTValues.MOD_NAME, "gregtech.quantum_storage");
    }

    @NotNull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world,
                                     BlockPos pos) {
        if (te instanceof IGregTechTileEntity gtte) {
            if (gtte.getMetaTileEntity() instanceof IQuantumStorage<?>storage &&
                    (storage.getType() == IQuantumStorage.Type.ITEM ||
                            storage.getType() == IQuantumStorage.Type.FLUID)) {
                var controller = storage.getQuantumController();
                int status = 0;

                if (controller != null) {
                    if (controller.isPowered()) {
                        status = 1;
                    } else {
                        status = 2;
                    }
                }

                NBTTagCompound subTag = new NBTTagCompound();
                subTag.setInteger("Connection", status);
                tag.setTag("gregtech.IQuantumStorageController", subTag);
            }
        }

        return tag;
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.quantum_storage") ||
                !(accessor.getTileEntity() instanceof IGregTechTileEntity gtte)) {
            return tooltip;
        }

        if (gtte.getMetaTileEntity() instanceof IQuantumController controller) {
            String eutText = configureEnergyUsage(controller.getEnergyUsage() / 10);
            if (controller.getCount(IQuantumStorage.Type.ENERGY) == 0) {
                tooltip.add(I18n.format("gregtech.top.quantum_controller.no_hatches"));
                tooltip.add(I18n.format("gregtech.top.energy_required") + eutText);
            } else if (!controller.isPowered()) {
                tooltip.add(I18n.format("gregtech.top.quantum_controller.no_power"));
                tooltip.add(I18n.format("gregtech.top.energy_required") + eutText);
            } else {
                tooltip.add(I18n.format("gregtech.top.energy_consumption") + eutText);
            }
        } else if (gtte.getMetaTileEntity() instanceof IQuantumStorage<?>storage &&
                (storage.getType() == IQuantumStorage.Type.ITEM ||
                        storage.getType() == IQuantumStorage.Type.FLUID)) {
                            if (accessor.getNBTData().hasKey("gregtech.IQuantumStorageController")) {
                                NBTTagCompound tag = accessor.getNBTData()
                                        .getCompoundTag("gregtech.IQuantumStorageController");
                                int connection = tag.getInteger("Connection");
                                String status;

                                switch (connection) {
                                    case 1 -> status = I18n.format("gregtech.top.quantum_status.powered");
                                    case 2 -> status = I18n.format("gregtech.top.quantum_status.connected");
                                    default -> status = I18n.format("gregtech.top.quantum_status.disconnected");
                                }

                                status = I18n.format("gregtech.top.quantum_status.label") +
                                        status;

                                tooltip.add(status);
                            }
                        }

        return tooltip;
    }

    public String configureEnergyUsage(long EUs) {
        return TextFormatting.RED.toString() + EUs + " EU/t" + TextFormatting.GREEN +
                " (" + GTValues.VNF[GTUtility.getTierByVoltage(EUs)] + TextFormatting.GREEN + ")";
    }
}
