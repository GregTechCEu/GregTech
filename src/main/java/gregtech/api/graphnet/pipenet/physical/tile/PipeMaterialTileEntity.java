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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

public class PipeMaterialTileEntity extends PipeTileEntity {

    private Material material;

    @Override
    protected void initialize() {
        // prevent initialization when we don't know our material;
        // this specifically happens right after we have been
        // placed and placedBy() has yet to be called.
        if (material != null) super.initialize();
    }

    @Override
    public void placedBy(ItemStack stack, EntityPlayer player) {
        super.placedBy(stack, player);
        setMaterial(getBlockType().getMaterialForStack(stack));
        initialize();
    }

    @Override
    protected ItemStack getPickItem(EntityPlayer player) {
        return getBlockType().getItem(material);
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
    @SideOnly(Side.CLIENT)
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
        if (material != null) encodeMaterialToBuffer(material, buf);
        super.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        if (buf.readBoolean()) material = decodeMaterialFromBuffer(buf);
        else material = null;
        super.receiveInitialSyncData(buf);
    }
}
