package gregtech.api.unification.material.properties;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTLog;
import gregtech.api.util.IBlockOre;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class StoneTypeProperty implements IMaterialProperty<StoneTypeProperty> {

    private static final Set<Material> stoneTypes = new ObjectArraySet<>();

    public static Material compute(IBlockState blockState, IBlockAccess world, BlockPos blockPos) {
        for (Material stoneType : stoneTypes) {
            StoneTypeProperty stoneTypeProperty = stoneType.getProperty(PropertyKey.STONE_TYPE);
            if (blockState.getBlock().isReplaceableOreGen(stoneTypeProperty.state.get(), world, blockPos, stoneTypeProperty.generationCondition)) {
                return stoneType;
            }
        }
        return null;
    }

    public final int id;
    public final Supplier<IBlockState> state;
    public final Supplier<OrePrefix> processingPrefix;
    public final Predicate<IBlockState> generationCondition;
    public final String backgroundTopTexture, backgroundSideTexture;
    public final Map<Material, IBlockOre> oreTable = new Object2ObjectOpenHashMap<>();

    public SoundType soundType = SoundType.STONE;
    public boolean affectedByGravity = false;

    public StoneTypeProperty(int id, Supplier<IBlockState> state, Supplier<OrePrefix> processingPrefix, Predicate<IBlockState> generationCondition, String backgroundTopTexture, String backgroundSideTexture) {
        this.id = id;
        this.state = state;
        this.processingPrefix = processingPrefix;
        this.generationCondition = generationCondition;
        this.backgroundTopTexture = backgroundTopTexture;
        this.backgroundSideTexture = backgroundSideTexture;
    }

    public StoneTypeProperty() {
        this(-1, Blocks.STONE::getDefaultState, () -> OrePrefix.ore, BlockMatcher.forBlock(Blocks.STONE), "minecraft:blocks/stone", "minecraft:blocks/stone");
    }

    public void setSoundType(SoundType soundType) {
        this.soundType = soundType;
    }

    public void setAffectedByGravity(boolean gravity) {
        this.affectedByGravity = gravity;
    }

    public void addOre(Material material, IBlockOre ore) {
        this.oreTable.put(material, ore);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
        for (Material stoneType : stoneTypes) {
            StoneTypeProperty stoneTypeProperty = stoneType.getProperty(PropertyKey.STONE_TYPE);
            Preconditions.checkArgument(stoneTypeProperty.id != this.id, "Material " + properties.getMaterial() + " is trying to register the same StoneType id as Material " + stoneType);
        }
        stoneTypes.add(properties.getMaterial());
    }

}
