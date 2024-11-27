package gregtech.api.unification.material.properties;

import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.unification.material.Material;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;

@Desugar
public record ChemicalStructureProperty(int textureHeight, int textureWidth) implements IMaterialProperty {

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.CHEMICAL_STRUCTURE, true);
    }

    @NotNull
    public static TextureArea getMoleculeTexture(Material material) {
        return TextureArea.fullImage(
                String.format("textures/chemicalstructure/%s.png", material.getResourceLocation().getPath()));
    }

    @NotNull
    public static ImageWidget getChemicalStructureWidget(Material material, int x, int y, int height, int width) {
        TextureArea tx = getMoleculeTexture(material);
        return new ImageWidget(x, y, width, height, tx);
    }
}
