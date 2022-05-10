package gregtech;

@SuppressWarnings("unused")
public final class GregTechVersion {

    public static final int MAJOR = 2;
    //This number is incremented every major feature update
    public static final int MINOR = 2;
    //This number is incremented every time the feature is added, or bug is fixed. resets every major version change
    public static final int REVISION = 3;
    //This String is for additional info on the release version if needed (like alpha, beta, rc). Comment out when unused
    public static final String EXTRA = "beta";
    //This is the String formatted version, used in builds
    public static final String VERSION = "@VERSION@";

    private GregTechVersion() {
    }
}
