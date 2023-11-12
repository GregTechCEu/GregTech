package gregtech.common;

import gregtech.api.GregTechAPI;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.handler.DynamiteRenderer;
import gregtech.client.renderer.handler.GTBoatRenderer;
import gregtech.client.renderer.handler.PortalRenderer;
import gregtech.common.entities.DynamiteEntity;
import gregtech.common.entities.GTBoatEntity;
import gregtech.common.entities.MiningPipeEntity;
import gregtech.common.entities.PortalEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MetaEntities {

    public static void init() {
        EntityRegistry.registerModEntity(GTUtility.gregtechId("dynamite"), DynamiteEntity.class, "Dynamite", 1, GregTechAPI.instance, 64, 3, true);
        EntityRegistry.registerModEntity(GTUtility.gregtechId("gtportal"), PortalEntity.class, "GTPortal", 2, GregTechAPI.instance, 64, 5, true);
        EntityRegistry.registerModEntity(GTUtility.gregtechId("gtboat"), GTBoatEntity.class, "GTBoat", 3, GregTechAPI.instance, 64, 2, true);
        EntityRegistry.registerModEntity(GTUtility.gregtechId("mining_pipe"), MiningPipeEntity.class, "MiningPipe", 4, GregTechAPI.instance, 64, 5, false);
    }

    @SideOnly(Side.CLIENT)
    public static void initRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(DynamiteEntity.class, manager -> new DynamiteRenderer(manager, Minecraft.getMinecraft().getRenderItem()));
        RenderingRegistry.registerEntityRenderingHandler(PortalEntity.class, PortalRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(GTBoatEntity.class, GTBoatRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(MiningPipeEntity.class, manager -> new Render<>(manager) {
            @Nullable
            @Override
            protected ResourceLocation getEntityTexture(@Nonnull MiningPipeEntity entity) {
                return null;
            }
        }); // TODO test shit please remove
    }
}
