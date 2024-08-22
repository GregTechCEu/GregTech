package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.metatileentities.electric.MetaTileEntityBatteryBuffer;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BatteryBufferDataProvider extends CapabilityDataProvider<IEnergyContainer> {

    public static final BatteryBufferDataProvider INSTANCE = new BatteryBufferDataProvider();

    @Override
    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.addConfig(GTValues.MOD_NAME, "gregtech.battery-buffer");
    }

    @Override
    protected @NotNull Capability<IEnergyContainer> getCapability() {
        return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER;
    }

    @Override
    protected NBTTagCompound getNBTData(IEnergyContainer capability, NBTTagCompound tag) {
        NBTTagCompound subTag = new NBTTagCompound();
        subTag.setLong("InputPerSec", capability.getInputPerSec());
        subTag.setLong("OutputPerSec", capability.getOutputPerSec());
        tag.setTag("gregtech.BatteryBuffer", subTag);
        return tag;
    }

    @Override
    @NotNull
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.battery-buffer") || accessor.getTileEntity() == null) {
            return tooltip;
        }

        if (accessor.getNBTData().hasKey("gregtech.BatteryBuffer")) {
            NBTTagCompound batteryTag = accessor.getNBTData().getCompoundTag("gregtech.BatteryBuffer");

            if (accessor.getTileEntity() instanceof IGregTechTileEntity iGregTechTileEntity) {
                MetaTileEntity metaTileEntity = iGregTechTileEntity.getMetaTileEntity();

                if (metaTileEntity instanceof MetaTileEntityBatteryBuffer) {
                    long averageIn = batteryTag.getLong("InputPerSec") / 20;
                    String averageInFormatted = I18n.format("gregtech.waila.energy_input",
                            TextFormattingUtil.formatNumbers(averageIn));
                    tooltip.add(averageInFormatted);

                    long averageOut = batteryTag.getLong("OutputPerSec") / 20;
                    String averageOutFormatted = I18n.format("gregtech.waila.energy_output",
                            TextFormattingUtil.formatNumbers(averageOut));
                    tooltip.add(averageOutFormatted);
                }
            }
        }

        return tooltip;
    }
}
