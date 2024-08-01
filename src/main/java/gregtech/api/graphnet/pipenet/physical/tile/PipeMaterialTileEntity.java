package gregtech.api.graphnet.pipenet.physical.tile;

import gregtech.api.GregTechAPI;
import gregtech.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.AbstractPipeModel;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
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
    public ItemStack getMainDrop(@NotNull IBlockState state) {
        return getBlockType().getItem(getMaterial());
    }

    @Override
    public int getDefaultPaintingColor() {
        return GTUtility.convertRGBtoARGB(getMaterial().getMaterialRGB());
    }

    @Override
    public IExtendedBlockState getRenderInformation(IExtendedBlockState state) {
        return super.getRenderInformation(state).withProperty(AbstractPipeModel.MATERIAL_PROPERTY, getMaterial());
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        if (material != null) compound.setString("Material", material.getRegistryName());
        return compound;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("Material"))
            this.material = GregTechAPI.materialManager.getMaterial(compound.getString("Material"));
        else this.material = null;
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        buf.writeBoolean(material != null);
        if (material != null) buf.writeString(material.getRegistryName());
        super.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        if (buf.readBoolean()) material = GregTechAPI.materialManager.getMaterial(buf.readString(255));
        super.receiveInitialSyncData(buf);
    }
}
