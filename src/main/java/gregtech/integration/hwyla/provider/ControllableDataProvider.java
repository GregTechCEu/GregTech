package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;

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

public class ControllableDataProvider extends CapabilityDataProvider<IControllable> {

    public static final ControllableDataProvider INSTANCE = new ControllableDataProvider();

    @Override
    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.addConfig(GTValues.MODID, "gregtech.controllable");
    }

    @Override
    protected @NotNull Capability<IControllable> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE;
    }

    @Override
    protected NBTTagCompound getNBTData(IControllable capability, NBTTagCompound tag) {
        NBTTagCompound subTag = new NBTTagCompound();
        subTag.setBoolean("Enabled", capability.isWorkingEnabled());
        tag.setTag("gregtech.IControllable", subTag);
        return tag;
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.controllable") || accessor.getTileEntity() == null) {
            return tooltip;
        }

        if (accessor.getNBTData().hasKey("gregtech.IControllable")) {
            NBTTagCompound tag = accessor.getNBTData().getCompoundTag("gregtech.IControllable");
            boolean isWorkingEnabled = tag.getBoolean("Enabled");
            if (!isWorkingEnabled) {
                tooltip.add(TextFormatting.RED + I18n.format("gregtech.top.working_disabled"));
            }
        }
        return tooltip;
    }
}
