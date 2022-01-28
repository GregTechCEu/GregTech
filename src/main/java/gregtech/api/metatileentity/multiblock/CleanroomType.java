package gregtech.api.metatileentity.multiblock;

public class CleanroomType {

    public static final CleanroomType CLEANROOM = new CleanroomType("cleanroom");

    private final String name;

    public CleanroomType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
