package gregtech.common.metatileentities.steam.boiler;

import gregtech.api.guiOld.GuiTextures;
import gregtech.api.guiOld.ModularUI;
import gregtech.api.guiOld.widgets.ProgressWidget.MoveType;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class SteamSolarBoiler extends SteamBoiler {

    public SteamSolarBoiler(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, isHighPressure, Textures.SOLAR_BOILER_OVERLAY);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new SteamSolarBoiler(metaTileEntityId, isHighPressure);
    }

    @Override
    protected int getBaseSteamOutput() {
        return isHighPressure ? 360 : 120;
    }

    protected boolean checkCanSeeSun() {
        BlockPos blockPos = getPos().up();
        if (!getWorld().canBlockSeeSky(blockPos))
            return false;
        return !getWorld().isRaining() && getWorld().isDaytime();
    }

    @Override
    protected void tryConsumeNewFuel() {
        if (checkCanSeeSun()) {
            setFuelMaxBurnTime(20);
        }
    }

    @Override
    protected int getCooldownInterval() {
        return isHighPressure ? 50 : 45;
    }

    @Override
    protected int getCoolDownRate() {
        return 3;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createUITemplate(entityPlayer)
                .progressBar(() -> checkCanSeeSun() ? 1.0 : 0.0, 114, 44, 20, 20,
                        GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(isHighPressure), MoveType.HORIZONTAL)
                .build(getHolder(), entityPlayer);
    }
}
