package gregtech.api.pipenet;

public interface INodeData {

    default double getWeightFactor() {
        return 0.5;
    }
}
