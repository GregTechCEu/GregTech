package gregtech.api.graphnet.pipenet.block;

import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.properties.PropertyMaterial;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public abstract class PipeMaterialBlock extends PipeBlock {

    public final MaterialRegistry registry;
    public final PropertyMaterial propertyMaterial;

    public PipeMaterialBlock(IPipeStructure structure, MaterialRegistry registry, Collection<? extends Material> materials) {
        super(structure);
        this.registry = registry;
        this.propertyMaterial = PropertyMaterial.create("Material", materials);
    }

    @Override
    protected IBlockState constructDefaultState() {
        return super.constructDefaultState().withProperty(propertyMaterial, propertyMaterial.getAllowedValues().get(0));
    }

    @Override
    protected @NotNull IPipeNetNodeHandler getHandler(IBlockState state) {
        return getHandler(state.getValue(propertyMaterial));
    }

    @NotNull
    protected abstract IPipeNetNodeHandler getHandler(Material material);

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
        return super.getDefaultState().withProperty(propertyMaterial, material);
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
            tooltip.add("MetaItem Id: pipe" + getStructureName() + getGtMaterial(stack).toCamelCaseString());
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
