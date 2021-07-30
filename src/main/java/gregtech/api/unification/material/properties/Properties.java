package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.Material;

import java.util.ArrayList;
import java.util.List;

public class Properties {

    private BlastProperty blastProperty;
    private DustProperty dustProperty;
    private FluidPipeProperty fluidPipeProperty;
    private FluidProperty fluidProperty;
    private GemProperty gemProperty;
    private IngotProperty ingotProperty;
    private ItemPipeProperty itemPipeProperty;
    private OreProperty oreProperty;
    private PlasmaProperty plasmaProperty;
    private ToolProperty toolProperty;
    private WireProperty wireProperty;

    private final List<IMaterialProperty> properties = new ArrayList<>();

    private Material material;

    public List<IMaterialProperty> getAll() {
        return properties;
    }

    public void verify() {
        properties.forEach(p -> p.verifyProperty(this, material));
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    ///////////////////////////////////////////////
    //                 GETTERS                   //
    ///////////////////////////////////////////////

    public BlastProperty getBlastProperty() {
        return blastProperty;
    }

    public DustProperty getDustProperty() {
        return dustProperty;
    }

    public FluidPipeProperty getFluidPipeProperty() {
        return fluidPipeProperty;
    }

    public FluidProperty getFluidProperty() {
        return fluidProperty;
    }

    public GemProperty getGemProperty() {
        return gemProperty;
    }

    public IngotProperty getIngotProperty() {
        return ingotProperty;
    }

    public ItemPipeProperty getItemPipeProperty() {
        return itemPipeProperty;
    }

    public OreProperty getOreProperty() {
        return oreProperty;
    }

    public PlasmaProperty getPlasmaProperty() {
        return plasmaProperty;
    }

    public ToolProperty getToolProperty() {
        return toolProperty;
    }

    public WireProperty getWireProperty() {
        return wireProperty;
    }

    ///////////////////////////////////////////////
    //                 SETTERS                   //
    ///////////////////////////////////////////////

    // Setters are effectively final, and will throw an error if reassigned.

    public void setBlastProperty(BlastProperty blastProperty) {
        if (this.blastProperty == null) {
            this.blastProperty = blastProperty;
            properties.add(this.blastProperty);
        } else throw new IllegalArgumentException("Blast Property already set for this Material!");
    }

    public void setDustProperty(DustProperty dustProperty) {
        if (this.dustProperty == null) {
            this.dustProperty = dustProperty;
            properties.add(this.dustProperty);
        } else throw new IllegalArgumentException("Dust Property already set for this Material!");
    }

    public void setFluidPipeProperty(FluidPipeProperty fluidPipeProperty) {
        if (itemPipeProperty != null)
            throw new IllegalArgumentException("Item and Fluid Pipe Property cannot be specified for one Material!");

        if (this.fluidPipeProperty == null) {
            this.fluidPipeProperty = fluidPipeProperty;
            properties.add(this.fluidPipeProperty);
        } else throw new IllegalArgumentException("Fluid Pipe Property already set for this Material!");
    }

    public void setFluidProperty(FluidProperty fluidProperty) {
        if (this.fluidProperty == null) {
            this.fluidProperty = fluidProperty;
            properties.add(this.fluidProperty);
        } else throw new IllegalArgumentException("Fluid Property already set for this Material!");
    }

    public void setGemProperty(GemProperty gemProperty) {
        if (this.gemProperty == null) {
            this.gemProperty = gemProperty;
            properties.add(this.gemProperty);
        } else throw new IllegalArgumentException("Gem Property already set for this Material!");
    }

    public void setIngotProperty(IngotProperty ingotProperty) {
        if (gemProperty != null)
            throw new IllegalArgumentException("Ingot and Gem cannot be specified for one Material!");

        if (this.ingotProperty == null) {
            this.ingotProperty = ingotProperty;
            properties.add(this.ingotProperty);
        } else throw new IllegalArgumentException("Ingot Property already set for this Material!");
    }

    public void setItemPipeProperty(ItemPipeProperty itemPipeProperty) {
        if (fluidPipeProperty != null)
            throw new IllegalArgumentException("Item and Fluid Pipe Property cannot be specified for one Material!");

        if (this.itemPipeProperty == null) {
            this.itemPipeProperty = itemPipeProperty;
            properties.add(this.itemPipeProperty);
        } else throw new IllegalArgumentException("Item Pipe Property already set for this Material!");
    }

    public void setOreProperty(OreProperty oreProperty) {
        if (this.oreProperty == null) {
            this.oreProperty = oreProperty;
            properties.add(this.oreProperty);
        } else throw new IllegalArgumentException("Ore Property already set for this Material!");
    }

    public void setPlasmaProperty(PlasmaProperty plasmaProperty) {
        if (this.plasmaProperty == null) {
            this.plasmaProperty = plasmaProperty;
            properties.add(this.plasmaProperty);
        } else throw new IllegalArgumentException("Plasma Property already set for this Material!");
    }

    public void setToolProperty(ToolProperty toolProperty) {
        if (this.toolProperty == null) {
            this.toolProperty = toolProperty;
            properties.add(this.toolProperty);
        } else throw new IllegalArgumentException("Tool Property already set for this Material!");
    }

    public void setWireProperty(WireProperty wireProperty) {
        if (this.wireProperty == null) {
            this.wireProperty = wireProperty;
            properties.add(this.wireProperty);
        } else throw new IllegalArgumentException("Wire Property already set for this Material!");
    }
}
