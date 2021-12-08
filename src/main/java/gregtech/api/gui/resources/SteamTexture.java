package gregtech.api.gui.resources;

/**
 * A simple wrapper around two {@link TextureArea} Objects used to
 * ease in choosing between Bronze Steam and Steel Steam textures.
 */
public class SteamTexture {

    private static final String BRONZE = "bronze";
    private static final String STEEL = "steel";

    private final TextureArea bronzeTexture;
    private final TextureArea steelTexture;

    private SteamTexture(TextureArea bronzeTexture, TextureArea steelTexture) {
        this.bronzeTexture = bronzeTexture;
        this.steelTexture = steelTexture;
    }

    public static SteamTexture fullImage(String path) {
        return new SteamTexture(
                TextureArea.fullImage(String.format(path, BRONZE)),
                TextureArea.fullImage(String.format(path, STEEL))
        );
    }

    public TextureArea get(boolean isHighPressure) {
        return isHighPressure ?  steelTexture : bronzeTexture;
    }
}
