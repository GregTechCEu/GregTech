package gregtech.api;

/**
 * If you're an addon looking in here, you're probably in the wrong place.
 * 
 * @see GregTechAPI
 */
public final class GregTechAPIInternal {

    private GregTechAPIInternal() {/**/}

    public static void preInit() {
        GregTechAPI.initializeHighTier();
    }
}
