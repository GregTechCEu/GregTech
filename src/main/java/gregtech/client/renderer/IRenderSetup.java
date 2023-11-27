package gregtech.client.renderer;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Object representation of GL setup code. Any recurring render setup / cleanup code should probably go here.
 * <p/>
 * <p>
 * During render, render calls with identical render setup instance will be drawn in a batch.
 * Providing proper {@link Object#equals(Object) equals()} and {@link Object#hashCode() hashCode()} implementation is
 * recommended for non-singleton render setup implementations.
 * <p/>
 */
@SideOnly(Side.CLIENT)
public interface IRenderSetup {

    /**
     * Run any pre render gl code here.
     *
     * @param buffer Buffer builder
     */
    void preDraw(@NotNull BufferBuilder buffer);

    /**
     * Run any post render gl code here.
     *
     * @param buffer Buffer builder
     */
    void postDraw(@NotNull BufferBuilder buffer);
}
