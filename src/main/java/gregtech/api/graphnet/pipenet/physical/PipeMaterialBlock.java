package gregtech.api.graphnet.pipenet.physical;

import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.properties.PropertyMaterial;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public abstract class PipeMaterialBlock extends PipeBlock {

    public final MaterialRegistry registry;
    public final PropertyMaterial propertyMaterial;

    public PipeMaterialBlock(IPipeMaterialStructure structure, MaterialRegistry registry, Collection<? extends Material> materials) {
        super(structure);
        this.registry = registry;
        this.propertyMaterial = PropertyMaterial.create("Material", materials);
        this.setDefaultState(this.blockState.getBaseState().withProperty(propertyMaterial, propertyMaterial.getAllowedValues().get(0)));
    }

    @Override
    public IPipeMaterialStructure getStructure() {
        return (IPipeMaterialStructure) super.getStructure();
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, propertyMaterial);
    }

    @NotNull
    public ItemStack getItem(@NotNull Material material) {
        return GTUtility.toItem(getDefaultState().withProperty(propertyMaterial, material));
    }

    @NotNull
    public Material getGtMaterial(int meta) {
        if (meta >= propertyMaterial.getAllowedValues().size()) {
            meta = 0;
        }
        return propertyMaterial.getAllowedValues().get(meta);
    }

    @NotNull
    public Material getGtMaterial(@NotNull ItemStack stack) {
        return getGtMaterial(stack.getMetadata());
    }

    @NotNull
    public Material getGtMaterial(@NotNull IBlockState state) {
        return state.getValue(propertyMaterial);
    }

    @NotNull
    public IBlockState getBlock(@NotNull Material material) {
        return getDefaultState().withProperty(propertyMaterial, material);
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(propertyMaterial, getGtMaterial(meta));
    }

    @Override
    public int getMetaFromState(@NotNull IBlockState state) {
        return propertyMaterial.getAllowedValues().indexOf(state.getValue(propertyMaterial));
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public void getSubBlocks(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> list) {
        for (IBlockState state : blockState.getValidStates()) {
            if (getGtMaterial(state) != Materials.NULL) {
                list.add(GTUtility.toItem(state));
            }
        }
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public MapColor getMapColor(@NotNull IBlockState state, @NotNull IBlockAccess worldIn, @NotNull BlockPos pos) {
        return getMaterial(state).getMaterialMapColor();
    }

    @Override
    public int getFlammability(@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing face) {
        Material material = getGtMaterial(world.getBlockState(pos));
        if (material.hasFlag(MaterialFlags.FLAMMABLE)) {
            return 20; // flammability of things like Wood Planks
        }
        return super.getFlammability(world, pos, face);
    }

    @Override
    public int getFireSpreadSpeed(@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing face) {
        Material material = getGtMaterial(world.getBlockState(pos));
        if (material.hasFlag(MaterialFlags.FLAMMABLE)) {
            return 5; // encouragement of things like Wood Planks
        }
        return super.getFireSpreadSpeed(world, pos, face);
    }

    public OrePrefix getPrefix() {
        return getStructure().getOrePrefix();
    }
    @Override
    protected @NotNull IPipeNetNodeHandler getHandler(IBlockState state) {
        return state.getValue(propertyMaterial).getProperty(PropertyKey.PIPENET_PROPERTIES);
    }

    @Override
    public String getHarvestTool(@NotNull IBlockState state) {
        Material material = getGtMaterial(state);
        if (ModHandler.isMaterialWood(material)) {
            return ToolClasses.AXE;
        }
        return ToolClasses.WRENCH;
    }

    @NotNull
    @Override
    public SoundType getSoundType(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos,
                                  @Nullable Entity entity) {
        Material material = getGtMaterial(state);
        if (ModHandler.isMaterialWood(material)) {
            return SoundType.WOOD;
        }
        return SoundType.METAL;
    }

    public SoundType getSoundType(ItemStack stack) {
        Material material = getGtMaterial(stack);
        if (ModHandler.isMaterialWood(material)) {
            return SoundType.WOOD;
        }
        return SoundType.METAL;
    }

    @Override
    public int getHarvestLevel(@NotNull IBlockState state) {
        Material material = getGtMaterial(state);
        if (material.hasProperty(PropertyKey.DUST)) {
            return material.getBlockHarvestLevel();
        }
        return 1;
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    public net.minecraft.block.material.Material getMaterial(@NotNull IBlockState state) {
        Material material = getGtMaterial(state);
        if (ModHandler.isMaterialWood(material)) {
            return net.minecraft.block.material.Material.WOOD;
        }
        return super.getMaterial(state);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        if (ConfigHolder.misc.debug) {
            tooltip.add("MetaItem Id: " + getStructureName() + getGtMaterial(stack).toCamelCaseString());
        }
    }

    @Nullable
    public static PipeMaterialBlock getPipeMaterialBlockFromItem(@NotNull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlock) {
            Block block = ((ItemBlock) item).getBlock();
            if (block instanceof PipeMaterialBlock) return (PipeMaterialBlock) block;
        }
        return null;
    }
}
