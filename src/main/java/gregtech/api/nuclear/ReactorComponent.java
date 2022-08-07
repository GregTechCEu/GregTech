package gregtech.api.nuclear;

public class ReactorComponent {
    private final double maxTemperature;
    private final double moderatorFactor;
    private final double thermalConductivity;
    boolean isInside;
    int id = -1;
    int[] pos = new int[2];

    public ReactorComponent(boolean isInside, double maxTemperature, double moderatorFactor, double thermalConductivity) {
        this.isInside = isInside;
        this.maxTemperature = maxTemperature;
        this.moderatorFactor = moderatorFactor;
        this.thermalConductivity = thermalConductivity;
    }

    public ReactorComponent copy(){
        return new ReactorComponent(isInside, maxTemperature, moderatorFactor, thermalConductivity);
    }

    public int getId() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public void setPos(int x, int y) {
        this.pos[0] = x;
        this.pos[1] = y;
    }

    public double getModeratorFactor() {
        return moderatorFactor;
    }

    public double getMaxTemperature() {
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

    public boolean samePositionAs(ReactorComponent component){
        return this.getPos()[0] == component.getPos()[0] || this.getPos()[1] == component.getPos()[1];
    }

    public double getDistanceSquared(ReactorComponent component){
        return getDistanceSquared(this, component);
    }

    public double getDistance(ReactorComponent component){
        return getDistance(this, component);
    }

    public static double getDistanceSquared(ReactorComponent component1, ReactorComponent component2){
        return Math.pow(component1.getPos()[0] - component2.getPos()[0], 2) + Math.pow(component1.getPos()[0] - component2.getPos()[0], 2);
    }

    public static double getDistance(ReactorComponent component1, ReactorComponent component2){
        return Math.sqrt(getDistanceSquared(component1, component2));
    }
}
