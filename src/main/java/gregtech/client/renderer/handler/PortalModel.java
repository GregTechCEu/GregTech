package gregtech.client.renderer.handler;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PortalModel extends ModelBase {
	private final ModelRenderer renderer;

	public PortalModel() {
		textureWidth = 64;
		textureHeight = 64;

		renderer = new ModelRenderer(this);
		renderer.setRotationPoint(0.0F, 24.0F, 0.0F);
		renderer.cubeList.add(new ModelBox(renderer, 0, 0, -8.0F, -32.0F, -1.0F, 16, 32, 2, 0.0F, false));
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		renderer.render(scale);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
