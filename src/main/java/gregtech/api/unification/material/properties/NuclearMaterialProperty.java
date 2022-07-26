package gregtech.api.unification.material.properties;

public class NuclearMaterialProperty implements IMaterialProperty<NuclearMaterialProperty> {
    private final double fission_cs_HE;
    private final double fission_cs_LE;
    private final double capture_cs_HE;
    private final double capture_cs_LE;

    public NuclearMaterialProperty(double fission_cs_HE, double fission_cs_LE, double capture_cs_HE, double capture_cs_LE) {
        this.fission_cs_HE = fission_cs_HE;
        this.fission_cs_LE = fission_cs_LE;
        this.capture_cs_HE = capture_cs_HE;
        this.capture_cs_LE = capture_cs_LE;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {

    }

    public double[] getNuclearCrossSections() {
        return new double[]{fission_cs_HE, fission_cs_LE, capture_cs_HE, capture_cs_LE};
    }
}
