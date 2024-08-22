package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IMultiblockController;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityActiveTransformer;

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

public class ActiveTransformerDataProvider extends CapabilityDataProvider<IMultiblockController> {

    public static final ActiveTransformerDataProvider INSTANCE = new ActiveTransformerDataProvider();

    @Override
    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.addConfig(GTValues.MOD_NAME, "gregtech.multiblock.activetransformer");
    }

    @Override
    protected @NotNull Capability<IMultiblockController> getCapability() {
        return GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER;
    }

    @Override
    protected NBTTagCompound getNBTData(IMultiblockController capability, NBTTagCompound tag) {
        if (capability instanceof MetaTileEntityActiveTransformer activeTransformer) {
            if (activeTransformer.isStructureFormed() && activeTransformer.isActive()) {
                NBTTagCompound subTag = new NBTTagCompound();

                subTag.setLong("AverageIO", activeTransformer.getAverageIOLastSec());

                tag.setTag("gregtech.IMultiblockController.ActiveTransformer", subTag);
            }
        }
        return tag;
    }

    @Override
    @NotNull
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.multiblock") || accessor.getTileEntity() == null) {
            return tooltip;
        }

        if (accessor.getNBTData().hasKey("gregtech.IMultiblockController.ActiveTransformer")) {
            NBTTagCompound tag = accessor.getNBTData()
                    .getCompoundTag("gregtech.IMultiblockController.ActiveTransformer");

            tooltip.add(I18n.format("gregtech.waila.active_transformer.average_io",
                    TextFormattingUtil.formatNumbers(tag.getLong("AverageIO"))));
        }

        return tooltip;
    }
}
