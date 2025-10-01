package gregtech.api.block.coil;

import gregtech.api.GregTechAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class CoilBlockBuilder {

    public static final int ACTIVE_META_LIMIT = 8;

    private final List<CustomCoilStats> stats = new ArrayList<>(ACTIVE_META_LIMIT);
    private final Consumer<CustomCoilBlock> onBuild;

    private CoilBlockBuilder(Consumer<CustomCoilBlock> onBuild) {
        this.onBuild = onBuild;
    }

    static CoilBlockBuilder builder(Consumer<CustomCoilBlock> onBuild) {
        return new CoilBlockBuilder(onBuild);
    }

    public CoilBlockBuilder addCoilType(UnaryOperator<CoilStatBuilder> builder) {
        if (stats.size() >= ACTIVE_META_LIMIT) {
            throw new IllegalStateException("Cannot exceed active meta limit!");
        }
        stats.add(builder.apply(new CoilStatBuilder()).build());
        return this;
    }

    public CustomCoilBlock build() {
        if (this.stats.isEmpty())
            throw new IllegalArgumentException("Variants is empty!");
        CustomCoilBlock.setActiveList(this.stats);
        var block = new CustomCoilBlock();
        CustomCoilBlock.clearActiveList();
        for (var stat : this.stats) {
            GregTechAPI.HEATING_COILS.put(block.getState(stat), stat);
        }

        this.onBuild.accept(block);

        return block;
    }
}
