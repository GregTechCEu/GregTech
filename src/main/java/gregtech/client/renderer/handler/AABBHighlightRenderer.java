package gregtech.client.renderer.handler;

import gregtech.api.pattern.GreggyBlockPos;
import gregtech.client.utils.RenderBufferHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.github.bsideup.jabel.Desugar;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BooleanSupplier;

@SideOnly(Side.CLIENT)
// maybe refactor as subclass of GTParticle? idk
public class AABBHighlightRenderer {

    private static final Map<AABBRender, BooleanSupplier> rendering = new HashMap<>();

    public static void renderWorldLastEvent(RenderWorldLastEvent event) {
        EntityPlayerSP p = Minecraft.getMinecraft().player;
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * event.getPartialTicks();
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * event.getPartialTicks();
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-doubleX, -doubleY, -doubleZ);
        // maybe not necessary? idk what it even does, but one time the outline was gray despite it being white and i
        // can't reproduce it
        GlStateManager.color(1, 1, 1);

        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();

        GlStateManager.glLineWidth(5);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        long time = System.currentTimeMillis();
        for (Iterator<Map.Entry<AABBRender, BooleanSupplier>> iter = rendering.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<AABBRender, BooleanSupplier> entry = iter.next();

            AABBRender aabb = entry.getKey();
            if (time > aabb.end() || !entry.getValue().getAsBoolean()) iter.remove();

            // todo maybe use GL_QUADS and draw 12 prisms instead of drawing 12 lines? this prevents incorrect scaling,
            // and fix or javadoc the +1 issue
            RenderBufferHelper.renderCubeFrame(buffer, aabb.from.x(), aabb.from.y(), aabb.from.z(),
                    aabb.to.x(), aabb.to.y(), aabb.to.z(),
                    aabb.r, aabb.g, aabb.b, 1);
        }

        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    public static void addAABB(AABBRender aabb, BooleanSupplier predicate) {
        rendering.put(aabb, predicate);
    }

    public static void removeAABB(AABBRender aabb) {
        rendering.remove(aabb);
    }

    @Desugar
    public record AABBRender(GreggyBlockPos from, GreggyBlockPos to, float r, float g, float b, long end) {
        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }
}
