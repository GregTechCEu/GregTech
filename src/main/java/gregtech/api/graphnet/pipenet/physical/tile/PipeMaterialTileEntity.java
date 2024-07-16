package gregtech.api.graphnet.pipenet.physical.tile;

import gregtech.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import gregtech.api.graphnet.pipenet.physical.block.WorldPipeBlock;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class PipeMaterialTileEntity extends PipeTileEntity {

    private Material material;

    public PipeMaterialTileEntity(PipeMaterialBlock block) {
        super(block);
    }

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
}
