package gregtech.api.metatileentity.multiblock;

public interface IControlRodPort {

    /**
     * Whether it has a moderator tip, which would increase k_eff at certain control rod insertion values.
     */
    boolean hasModeratorTip();
}
