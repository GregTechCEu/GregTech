package gregtech.common.covers;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;

public class CoverShutter extends CoverBase implements IControllable {

    private boolean isWorkingAllowed = true;

    public CoverShutter(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                        @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.SHUTTER.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return true;
    }

    @Override
    public @NotNull EnumActionResult onRightClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                  @NotNull CuboidRayTraceResult hitResult) {
        return EnumActionResult.FAIL;
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        return EnumActionResult.FAIL;
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, T defaultValue) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return isWorkingEnabled() ? null : defaultValue;
    }

    @Override
    public boolean shouldAutoConnectToPipes() {
        return false;
    }

    @Override
    public boolean canPipePassThrough() {
        return !isWorkingAllowed;
    }

    @Override
    public @NotNull EnumActionResult onSoftMalletClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                       @NotNull CuboidRayTraceResult hitResult) {
        this.isWorkingAllowed = !this.isWorkingAllowed;
        if (!playerIn.world.isRemote) {
            playerIn.sendMessage(new TextComponentTranslation(isWorkingEnabled() ?
                    "cover.shutter.message.enabled" : "cover.shutter.message.disabled"));
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean isWorkingEnabled() {
        return isWorkingAllowed;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        isWorkingAllowed = isActivationAllowed;
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("WorkingAllowed", isWorkingAllowed);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        isWorkingAllowed = tagCompound.getBoolean("WorkingAllowed");
    }
}
