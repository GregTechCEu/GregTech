package gregtech.common.metatileentities.steam.boiler;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SteamSolarBoiler extends SteamBoiler {

    public SteamSolarBoiler(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, isHighPressure, Textures.SOLAR_BOILER_OVERLAY);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SteamSolarBoiler(metaTileEntityId, isHighPressure);
    }

    @Override
    protected int getBaseSteamOutput() {
        return isHighPressure ? 360 : 120;
    }

    @Override
    protected void tryConsumeNewFuel() {
        if (GTUtility.canSeeSunClearly(getWorld(), getPos())) {
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
                .progressBar(() -> GTUtility.canSeeSunClearly(getWorld(), getPos()) ? 1.0 : 0.0, 114, 44, 20, 20,
                        GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(isHighPressure), MoveType.HORIZONTAL)
                .build(getHolder(), entityPlayer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick() {
        // Solar boilers do not display particles
    }
}
