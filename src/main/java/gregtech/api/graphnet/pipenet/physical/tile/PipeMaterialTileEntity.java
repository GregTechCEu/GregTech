package gregtech.api.graphnet.pipenet.physical.tile;

import gregtech.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.PipeModel;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.property.IExtendedBlockState;

import org.jetbrains.annotations.NotNull;

public class PipeMaterialTileEntity extends PipeTileEntity {

    private Material material;

    @Override
    public void placedBy(ItemStack stack, EntityPlayer player) {
        super.placedBy(stack, player);
        setMaterial(getBlockType().getMaterialForStack(stack));
    }

    @Override
    public @NotNull PipeMaterialBlock getBlockType() {
        return (PipeMaterialBlock) super.getBlockType();
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        if (material == null) return Materials.Aluminium;
        return material;
    }

    @Override
    public int getDefaultPaintingColor() {
        return GTUtility.convertRGBtoARGB(getMaterial().getMaterialRGB());
    }

    @Override
    public IExtendedBlockState getRenderInformation(IExtendedBlockState state) {
        return super.getRenderInformation(state).withProperty(PipeModel.MATERIAL_PROPERTY, getMaterial());
    }
}
