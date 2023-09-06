package gregtech.client.renderer;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Object representation of GL setup code. Any recurring render setup / cleanup code should probably go here.<p/>
 * During render, render calls with identical render setup instance will be drawn in a batch.
 * Providing proper equality checks and hashes is recommended for non-singleton render setup implementations.
 */
@SideOnly(Side.CLIENT)
public interface RenderSetup {

    /**
     * Run any pre render gl code here.
     *
     * @param buffer Buffer builder
     */
    void preDraw(@Nonnull BufferBuilder buffer);

    /**
     * Run any post render gl code here.
     *
     * @param buffer Buffer builder
     */
    void postDraw(@Nonnull BufferBuilder buffer);
}
