package gregtech.api.recipes.logic;

public final class OCParams {

    private long eut;
    private int duration;
    private int ocAmount;

    public void initialize(long eut, int duration, int ocAmount) {
        this.eut = eut;
        this.duration = duration;
        this.ocAmount = ocAmount;
    }

    public void reset() {
        this.eut = 0L;
        this.duration = 0;
        this.ocAmount = 0;
    }

    public long eut() {
        return eut;
    }

    public void setEut(long eut) {
        this.eut = eut;
    }

    public int duration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int ocAmount() {
        return ocAmount;
    }

    public void setOcAmount(int ocAmount) {
        this.ocAmount = ocAmount;
    }
}
