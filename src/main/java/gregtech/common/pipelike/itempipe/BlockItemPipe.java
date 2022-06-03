package gregtech.common.pipelike.itempipe;

import com.google.common.base.Preconditions;
import gregtech.api.GregTechAPI;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.client.renderer.pipe.ItemPipeRenderer;
import gregtech.common.pipelike.itempipe.net.WorldItemPipeNet;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipeTickable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BlockItemPipe extends BlockMaterialPipe<ItemPipeType, ItemPipeProperties, WorldItemPipeNet> {

    private final Map<Material, ItemPipeProperties> enabledMaterials = new HashMap<>();

    public BlockItemPipe(ItemPipeType itemPipeType) {
        super(itemPipeType);
        setHarvestLevel(ToolClasses.WRENCH, 1);
    }

    public void addPipeMaterial(Material material, ItemPipeProperties properties) {
        Preconditions.checkNotNull(material, "material");
        Preconditions.checkNotNull(properties, "material %s itemPipeProperties was null", material);
        Preconditions.checkArgument(GregTechAPI.MATERIAL_REGISTRY.getNameForObject(material) != null, "material %s is not registered", material);
        this.enabledMaterials.put(material, properties);
    }

    @Override
    public TileEntityPipeBase<ItemPipeType, ItemPipeProperties> createNewTileEntity(boolean supportsTicking) {
        return supportsTicking ? new TileEntityItemPipeTickable() : new TileEntityItemPipe();
    }

    @Override
    public Class<ItemPipeType> getPipeTypeClass() {
        return ItemPipeType.class;
    }

    @Override
    protected ItemPipeProperties getFallbackType() {
        return enabledMaterials.values().iterator().next();
    }

    @Override
    public WorldItemPipeNet getWorldPipeNet(World world) {
        return WorldItemPipeNet.getWorldPipeNet(world);
    }

    @Override
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return ItemPipeRenderer.INSTANCE.getParticleTexture((TileEntityItemPipe) world.getTileEntity(blockPos));
    }

    @Override
    protected ItemPipeProperties createProperties(ItemPipeType itemPipeType, Material material) {
        return itemPipeType.modifyProperties(enabledMaterials.getOrDefault(material, getFallbackType()));
    }

    public Collection<Material> getEnabledMaterials() {
        return Collections.unmodifiableSet(enabledMaterials.keySet());
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs itemIn, @Nonnull NonNullList<ItemStack> items) {
        for (Material material : enabledMaterials.keySet()) {
            items.add(getItem(material));
        }
    }

    @Override
    public ItemPipeType getItemPipeType(ItemStack itemStack) {
        return super.getItemPipeType(itemStack);
    }

    @Override
    public boolean canPipesConnect(IPipeTile<ItemPipeType, ItemPipeProperties> selfTile, EnumFacing side, IPipeTile<ItemPipeType, ItemPipeProperties> sideTile) {
        return selfTile instanceof TileEntityItemPipe && sideTile instanceof TileEntityItemPipe;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeTile<ItemPipeType, ItemPipeProperties> selfTile, EnumFacing side, TileEntity tile) {
        return tile != null && tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()) != null;
    }

    @Override
    public boolean isHoldingPipe(EntityPlayer player) {
        if (player == null) {
            return false;
        }
        ItemStack stack = player.getHeldItemMainhand();
        return stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlockItemPipe;
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return ItemPipeRenderer.INSTANCE.getBlockRenderType();
    }


}
