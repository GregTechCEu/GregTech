package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IMultiblockController;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MultiblockDataProvider extends CapabilityDataProvider<IMultiblockController> {

    public static final MultiblockDataProvider INSTANCE = new MultiblockDataProvider();

    @Override
    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.addConfig(GTValues.MODID, "gregtech.multiblock");
    }

    @Override
    protected @NotNull Capability<IMultiblockController> getCapability() {
        return GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER;
    }

    @Override
    protected NBTTagCompound getNBTData(IMultiblockController capability, NBTTagCompound tag) {
        NBTTagCompound subTag = new NBTTagCompound();
        subTag.setBoolean("Formed", capability.isStructureFormed());
        subTag.setBoolean("Obstructed", capability.isStructureObstructed());
        tag.setTag("gregtech.IMultiblockController", subTag);
        return tag;
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.multiblock") || accessor.getTileEntity() == null) {
            return tooltip;
        }

        if (accessor.getNBTData().hasKey("gregtech.IMultiblockController")) {
            NBTTagCompound tag = accessor.getNBTData().getCompoundTag("gregtech.IMultiblockController");
            boolean formed = tag.getBoolean("Formed");
            boolean obstructed = tag.getBoolean("Obstructed");
            if (formed) {
                tooltip.add(I18n.format("gregtech.top.valid_structure"));
                if (obstructed) {
                    tooltip.add(TextFormatting.RED + I18n.format("gregtech.top.obstructed_structure"));
                }
            } else {
                tooltip.add(I18n.format("gregtech.top.invalid_structure"));
            }
        }
        return tooltip;
    }
}
