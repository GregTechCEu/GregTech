package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Materials;
import gregtech.common.metatileentities.steam.boiler.SteamBoiler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.*;

public class SteamBoilerInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return GTValues.MODID + ":steam_boiler_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState state,
                             IProbeHitData data) {
        if (state.getBlock().hasTileEntity(state)) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (te instanceof IGregTechTileEntity igtte) {
                MetaTileEntity mte = igtte.getMetaTileEntity();
                if (mte instanceof SteamBoiler boiler) {
                    if (boiler.isBurning()) {
                        // Boiler is active
                        int steamOutput = boiler.getTotalSteamOutput();

                        // Creating steam
                        if (steamOutput > 0 && boiler.hasWater()) {
                            probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.energy_production*} " +
                                    TextFormatting.AQUA + (steamOutput / 10) + TextStyleClass.INFO + " L/t" + " {*" +
                                    Materials.Steam.getUnlocalizedName() + "*}");
                        }

                        // Initial heat-up
                        if (steamOutput <= 0) {
                            probeInfo.text(TextStyleClass.INFO.toString() + TextFormatting.RED +
                                    "{*gregtech.top.steam_heating_up*}");
                        }

                        // No water
                        if (!boiler.hasWater()) {
                            probeInfo.text(TextStyleClass.WARNING + "{*gregtech.top.steam_no_water*}");
                        }
                    }
                }
            }
        }
    }
}
