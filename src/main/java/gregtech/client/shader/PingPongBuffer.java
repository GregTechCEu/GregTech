package gregtech.client.shader;

import gregtech.client.utils.RenderUtil;

import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PingPongBuffer {

    private static final Framebuffer BUFFER_A;
    private static final Framebuffer BUFFER_B;
    private static boolean flag;

    static {
        BUFFER_A = new Framebuffer(10, 10, false);
        BUFFER_B = new Framebuffer(10, 10, false);
        BUFFER_A.setFramebufferColor(0, 0, 0, 0);
        BUFFER_B.setFramebufferColor(0, 0, 0, 0);
    }

    public static void updateSize(int width, int height) {
        RenderUtil.updateFBOSize(BUFFER_A, width, height);
        RenderUtil.updateFBOSize(BUFFER_B, width, height);
    }

    public static void cleanAllUp() {
        BUFFER_A.framebufferClear();
        BUFFER_B.framebufferClear();
    }

    public static Framebuffer getCurrentBuffer(boolean clean) {
        Framebuffer buffer = flag ? BUFFER_A : BUFFER_B;
        if (clean) {
            buffer.framebufferClear();
        }
        return buffer;
    }

    public static Framebuffer getNextBuffer(boolean clean) {
        Framebuffer buffer = flag ? BUFFER_B : BUFFER_A;
        if (clean) {
            buffer.framebufferClear();
        }
        return buffer;
    }

    public static Framebuffer swap() {
        return swap(false);
    }

    public static Framebuffer swap(boolean clean) {
        flag = !flag;
        return getCurrentBuffer(clean);
    }

    public static void bindFramebufferTexture() {
        getCurrentBuffer(false).bindFramebufferTexture();
    }
}
