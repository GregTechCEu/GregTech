package gregtech.common.metatileentities.steam;

import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

public class SteamMacerator extends SteamMetaTileEntity {

    public SteamMacerator(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, RecipeMaps.MACERATOR_RECIPES, Textures.MACERATOR_OVERLAY, isHighPressure);
    }

    @Override
    public MetaTileEntity copy() {
        return new SteamMacerator(metaTileEntityId, isHighPressure);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, true);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        return createUITemplate(player)
                .slot(this.importItems, 0, 53, 25, GuiTextures.SLOT_STEAM.get(isHighPressure),
                        GuiTextures.CRUSHED_ORE_OVERLAY_STEAM.get(isHighPressure))
                .progressBar(workableHandler::getProgressPercent, 79, 26, 21, 18,
                        GuiTextures.PROGRESS_BAR_MACERATE_STEAM.get(isHighPressure), MoveType.HORIZONTAL,
                        workableHandler.getRecipeMap())
                .slot(this.exportItems, 0, 107, 25, true, false, GuiTextures.SLOT_STEAM.get(isHighPressure),
                        GuiTextures.DUST_OVERLAY_STEAM.get(isHighPressure))
                .build(getHolder(), player);
    }

    @Override
    public int getItemOutputLimit() {
        return 1;
    }

    @Override
    public void updateMTE() {
        super.updateMTE();
        if (isActive() && getWorld().isRemote) {
            VanillaParticleEffects.TOP_SMOKE_SMALL.runEffect(this);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick() {
        // steam macerators do not make particles in this way
    }
}
