package gregtech.api.unification.material.properties;

import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;

import gregtech.api.graphnet.pipenet.block.IPipeStructure;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public class PipeNetProperties implements IMaterialProperty, IPipeNetNodeHandler {

    protected final Map<String, IPipeNetMaterialProperty> properties = new Object2ObjectOpenHashMap<>();

    public void setProperty(IPipeNetMaterialProperty property) {
        this.properties.put(property.getName(), property);
    }

    public void removeProperty(String propertyName) {
        this.properties.remove(propertyName);
    }

    @Override
    public void addToNets(World world, BlockPos pos, IPipeStructure structure) {
        for (IPipeNetMaterialProperty p : properties.values()) {
            if (p.supportedStructure(structure)) p.addToNet(world, pos, structure);
        }
    }

    @Override
    public void removeFromNets(World world, BlockPos pos, IPipeStructure structure) {
        for (IPipeNetMaterialProperty p : properties.values()) {
            if (p.supportedStructure(structure)) p.removeFromNet(world, pos);
        }
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        for (IPipeNetMaterialProperty p : this.properties.values()) {
            p.verifyProperty(properties);
        }
    }

    public interface IPipeNetMaterialProperty extends IMaterialProperty, IStringSerializable {

        void addToNet(World world, BlockPos pos, IPipeStructure structure);

        void removeFromNet(World world, BlockPos pos);

        boolean supportedStructure(IPipeStructure structure);
    }
}
