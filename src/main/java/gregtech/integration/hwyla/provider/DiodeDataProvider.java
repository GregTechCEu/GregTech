package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.metatileentities.electric.MetaTileEntityDiode;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DiodeDataProvider extends ElectricContainerDataProvider {

    public static final DiodeDataProvider INSTANCE = new DiodeDataProvider();

    @Override
    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.addConfig(GTValues.MODID, "gregtech.diode");
    }

    @Override
    protected NBTTagCompound getNBTData(IEnergyContainer capability, NBTTagCompound tag) {
        NBTTagCompound subTag = new NBTTagCompound();
        for (var facing : EnumFacing.VALUES) {
            if (capability.outputsEnergy(facing)) {
                subTag.setInteger("FrontFacing", facing.getIndex());
                break;
            }
        }
        subTag.setLong("InputAmperage", capability.getInputAmperage());
        subTag.setLong("OutputAmperage", capability.getOutputAmperage());
        tag.setTag("gregtech.MetaTileEntityDiode", subTag);
        return tag;
    }

    @Override
    public @NotNull List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                              IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.diode") || !(accessor.getTileEntity() instanceof IGregTechTileEntity gtte) ||
                !(gtte.getMetaTileEntity() instanceof MetaTileEntityDiode)) {
            return tooltip;
        }

        if (accessor.getNBTData().hasKey("gregtech.MetaTileEntityDiode")) {
            NBTTagCompound tag = accessor.getNBTData().getCompoundTag("gregtech.MetaTileEntityDiode");
            final long inputAmperage = tag.getLong("InputAmperage");
            final long outputAmperage = tag.getLong("OutputAmperage");
            final EnumFacing frontFacing = EnumFacing.byIndex(tag.getInteger("FrontFacing"));

            if (accessor.getSide() == frontFacing) { // output side
                tooltip.add(I18n.format("gregtech.top.transform_output") + " " + outputAmperage + " A");
            } else {
                tooltip.add(I18n.format("gregtech.top.transform_input") + " " + inputAmperage + " A");
            }
        }
        return tooltip;
    }
}
