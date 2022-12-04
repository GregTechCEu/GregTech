package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;

public class CoverShutter extends CoverBehavior implements IControllable {

    private boolean isWorkingAllowed = true;

    public CoverShutter(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return true;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.SHUTTER.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public EnumActionResult onRightClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        return EnumActionResult.FAIL;
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        return EnumActionResult.FAIL;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return isWorkingEnabled() ? null : defaultValue;
    }

    @Override
    public boolean shouldAutoConnect() {
        return false;
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
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("WorkingAllowed", isWorkingAllowed);

        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        isWorkingAllowed = tagCompound.getBoolean("WorkingAllowed");
    }
}
