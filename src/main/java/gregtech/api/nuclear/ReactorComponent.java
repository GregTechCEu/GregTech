package gregtech.api.nuclear;

public class ReactorComponent {
    private final int maxTemperature;
    private final double moderatorFactor;
    private final double thermalConductivity;
    boolean isInside;
    int id = -1;
    int[] pos = new int[2];

    public ReactorComponent(boolean isInside, int maxTemperature, double moderatorFactor, double thermalConductivity) {
        this.isInside = isInside;
        this.maxTemperature = maxTemperature;
        this.moderatorFactor = moderatorFactor;
        this.thermalConductivity = thermalConductivity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPos(int x, int y) {
        this.pos[0] = x;
        this.pos[1] = y;
    }

    public double getModeratorFactor() {
        return moderatorFactor;
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    public boolean isInside() {
        return isInside;
    }

    public double getThermalConductivity() {
        return thermalConductivity;
    }

    public int[] getPos() {
        return pos;
    }
}
