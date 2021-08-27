package gregtech.api.capability;

public interface IActiveOutputSide {

    boolean isAutoOutputItems();

    boolean isAutoOutputFluids();

    boolean isAllowInputFromOutputSideItems();

    boolean isAllowInputFromOutputSideFluids();
}
