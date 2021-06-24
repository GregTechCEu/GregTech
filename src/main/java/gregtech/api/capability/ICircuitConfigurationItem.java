package gregtech.api.capability;

/**
 * for configuration circuit texture switching
 */
public interface ICircuitConfigurationItem {

    int getConfiguration();

    int changeConfiguration(int amount, boolean simulate);

}
