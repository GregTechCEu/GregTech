package gregtech.api.graphnet.pipenet;

import gregtech.api.cover.CoverableView;

import org.jetbrains.annotations.Nullable;

public interface NodeWithCovers {

    @Nullable CoverableView getCoverableView();
}
