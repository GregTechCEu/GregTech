package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.TextFormattingUtil;
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
                    int steamOutput = boiler.getTotalSteamOutput();
                    // If we are producing steam, or we have fuel
                    if (steamOutput > 0 || boiler.isBurning()) {
                        // Creating steam
                        if (steamOutput > 0 && boiler.hasWater()) {
                            probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.energy_production*} " +
                                    TextFormatting.AQUA + TextFormattingUtil.formatNumbers(steamOutput / 10) +
                                    TextStyleClass.INFO + " L/t" + " {*" +
                                    Materials.Steam.getUnlocalizedName() + "*}");
                        }

                        // Cooling Down
                        if (!boiler.isBurning()) {
                            probeInfo.text(TextStyleClass.INFO.toString() + TextFormatting.RED +
                                    "{*gregtech.top.steam_cooling_down*}");
                        }

                        // Initial heat-up
                        if (steamOutput <= 0 && boiler.getCurrentTemperature() > 0) {
                            // Current Temperature = the % until the boiler reaches 100
                            probeInfo.text(TextStyleClass.INFO.toString() + TextFormatting.RED +
                                    "{*gregtech.top.steam_heating_up*} " +
                                    TextFormattingUtil.formatNumbers(boiler.getCurrentTemperature()) + "%");
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
