package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Materials;
import gregtech.common.metatileentities.steam.boiler.SteamBoiler;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SteamBoilerDataProvider implements IWailaDataProvider {

    public static final SteamBoilerDataProvider INSTANCE = new SteamBoilerDataProvider();

    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, IGregTechTileEntity.class);
        registrar.registerNBTProvider(this, IGregTechTileEntity.class);
        registrar.addConfig(GTValues.MODID, "gregtech.steam_boiler");
    }

    @NotNull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world,
                                     BlockPos pos) {
        if (te instanceof IGregTechTileEntity gtte) {
            if (gtte.getMetaTileEntity() instanceof SteamBoiler boiler) {
                NBTTagCompound subTag = new NBTTagCompound();
                subTag.setBoolean("IsBurning", boiler.isBurning());
                subTag.setBoolean("HasWater", boiler.hasWater());
                subTag.setInteger("SteamRate", boiler.getTotalSteamOutput());
                tag.setTag("gregtech.SteamBoiler", subTag);
            }
        }
        return tag;
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.steam_boiler") ||
                !(accessor.getTileEntity() instanceof IGregTechTileEntity gtte) ||
                !(gtte.getMetaTileEntity() instanceof SteamBoiler)) {
            return tooltip;
        }

        if (accessor.getNBTData().hasKey("gregtech.SteamBoiler")) {
            NBTTagCompound tag = accessor.getNBTData().getCompoundTag("gregtech.SteamBoiler");
            if (tag.getBoolean("IsBurning")) {
                int steamRate = tag.getInteger("SteamRate");
                boolean hasWater = tag.getBoolean("HasWater");

                // Creating steam
                if (steamRate > 0 && hasWater) {
                    tooltip.add(I18n.format("gregtech.top.energy_production") + ": " + (steamRate / 10) + " L/t " +
                            I18n.format(Materials.Steam.getUnlocalizedName()));
                }

                // Initial heat-up
                if (steamRate <= 0) {
                    tooltip.add(I18n.format("gregtech.top.steam_heating_up"));
                }

                // No water
                if (!hasWater) {
                    tooltip.add(I18n.format("gregtech.top.steam_no_water"));
                }
            }
        }
        return tooltip;
    }
}
