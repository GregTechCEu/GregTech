package gregtech.common.metatileentities.steam;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SteamCompressor extends SteamMetaTileEntity {

    public SteamCompressor(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, RecipeMaps.COMPRESSOR_RECIPES, Textures.COMPRESSOR_OVERLAY, isHighPressure);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SteamCompressor(metaTileEntityId, isHighPressure);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        return createUITemplate(player)
                .slot(this.importItems, 0, 53, 25, GuiTextures.SLOT_STEAM.get(isHighPressure),
                        GuiTextures.COMPRESSOR_OVERLAY_STEAM.get(isHighPressure))
                .progressBar(this::recipeProgressPercent, 78, 25, 20, 18,
                        GuiTextures.PROGRESS_BAR_COMPRESS_STEAM.get(isHighPressure), MoveType.HORIZONTAL,
                        getRecipeMap())
                .slot(this.exportItems, 0, 107, 25, true, false, GuiTextures.SLOT_STEAM.get(isHighPressure))
                .build(getHolder(), player);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick() {
        // steam compressors do not make particles
    }
}
