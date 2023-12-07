package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.ComputationRecipeLogic;

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

public class WorkableDataProvider extends CapabilityDataProvider<IWorkable> {

    public static final WorkableDataProvider INSTANCE = new WorkableDataProvider();

    @Override
    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.addConfig(GTValues.MODID, "gregtech.workable");
    }

    @Override
    protected @NotNull Capability<IWorkable> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_WORKABLE;
    }

    @Override
    protected NBTTagCompound getNBTData(IWorkable capability, NBTTagCompound tag) {
        NBTTagCompound subTag = new NBTTagCompound();
        subTag.setBoolean("Active", capability.isActive());
        if (capability.isActive()) {
            subTag.setBoolean("ShowAsComputation",
                    capability instanceof ComputationRecipeLogic logic && !logic.shouldShowDuration());
            subTag.setInteger("Progress", capability.getProgress());
            subTag.setInteger("MaxProgress", capability.getMaxProgress());
        }
        tag.setTag("gregtech.IWorkable", subTag);
        return tag;
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.workable") || accessor.getTileEntity() == null) {
            return tooltip;
        }

        if (accessor.getNBTData().hasKey("gregtech.IWorkable")) {
            NBTTagCompound tag = accessor.getNBTData().getCompoundTag("gregtech.IWorkable");
            boolean active = tag.getBoolean("Active");
            if (active) {
                int progress = tag.getInteger("Progress");
                int maxProgress = tag.getInteger("MaxProgress");

                if (tag.getBoolean("ShowAsComputation")) {
                    tooltip.add(I18n.format("gregtech.waila.progress_computation", progress, maxProgress));
                }

                if (maxProgress == 0) {
                    tooltip.add(I18n.format("gregtech.waila.progress_idle"));
                } else if (maxProgress < 20) {
                    tooltip.add(I18n.format("gregtech.waila.progress_tick", progress, maxProgress));
                } else {
                    progress = Math.round(progress / 20.0F);
                    maxProgress = Math.round(maxProgress / 20.0F);
                    tooltip.add(I18n.format("gregtech.waila.progress_sec", progress, maxProgress));
                }
            }
        }

        return super.getWailaBody(itemStack, tooltip, accessor, config);
    }
}
