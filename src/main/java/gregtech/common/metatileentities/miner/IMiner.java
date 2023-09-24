package gregtech.common.metatileentities.miner;

import gregtech.client.renderer.ICubeRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IMiner {

    /**
     * Try to drain all mining resources required for one operation. (e.g. energy, mining fluids)
     *
     * @param simulate Whether this action shouldn't affect the state
     * @return Whether the action succeeded
     */
    boolean drainMiningResources(boolean simulate);

    @SideOnly(Side.CLIENT)
    ICubeRenderer getPipeTexture();
}
