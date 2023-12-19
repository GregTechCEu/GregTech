package gregtech.client.renderer.handler;

import gregtech.api.util.GTUtility;
import gregtech.common.entities.GTBoatEntity;

import net.minecraft.client.renderer.entity.RenderBoat;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.ResourceLocation;

public class GTBoatRenderer extends RenderBoat {

    public static final ResourceLocation RUBBER_WOOD_BOAT_TEXTURE = GTUtility
            .gregtechId("textures/entity/rubber_wood_boat.png");
    public static final ResourceLocation TREATED_WOOD_BOAT_TEXTURE = GTUtility
            .gregtechId("textures/entity/treated_wood_boat.png");

    public GTBoatRenderer(RenderManager m) {
        super(m);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBoat entity) {
        if (entity instanceof GTBoatEntity) {
            switch (((GTBoatEntity) entity).getGTBoatType()) {
                case RUBBER_WOOD_BOAT:
                    return RUBBER_WOOD_BOAT_TEXTURE;
                case TREATED_WOOD_BOAT:
                    return TREATED_WOOD_BOAT_TEXTURE;
            }
        }
        return super.getEntityTexture(entity);
    }
}
