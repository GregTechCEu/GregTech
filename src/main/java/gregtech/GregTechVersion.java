package gregtech;

public final class GregTechVersion {

    public static final int MAJOR = 2;
    //This number is incremented every major feature update
    public static final int MINOR = 0;
    //This number is incremented every time the feature is added, or bug is fixed. resets every major version change
    public static final int REVISION = 0;
    //This String is for additional info on the release version if needed (like alpha, beta, rc). Comment out when unused
    public static final String EXTRA = "alpha";
    //This number is incremented every build, and never reset. Should always be 0 in the repo code.
    public static final int BUILD = 0;

    private GregTechVersion() {
    }
}
