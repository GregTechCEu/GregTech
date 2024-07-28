package gregtech.client.renderer.pipe.cache;

import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.quad.PipeQuadHelper;
import gregtech.client.renderer.pipe.quad.RecolorableBakedQuad;
import gregtech.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RestrictiveSQC extends BlockableSQC {

    protected final EnumMap<EnumFacing, SubListAddress> restrictiveCoords = new EnumMap<>(EnumFacing.class);

    private final SpriteInformation restrictiveTex;

    protected RestrictiveSQC(PipeQuadHelper helper, SpriteInformation endTex, SpriteInformation sideTex,
                             SpriteInformation blockedTex, SpriteInformation restrictiveTex) {
        super(helper, endTex, sideTex, blockedTex);
        this.restrictiveTex = restrictiveTex;
    }

    public static @NotNull RestrictiveSQC create(PipeQuadHelper helper, SpriteInformation endTex,
                                                 SpriteInformation sideTex,
                                                 SpriteInformation blockedTex, SpriteInformation restrictiveTex) {
        RestrictiveSQC sqc = new RestrictiveSQC(helper, endTex, sideTex, blockedTex, restrictiveTex);
        sqc.buildPrototype();
        return sqc;
    }

    @Override
    protected List<RecolorableBakedQuad> buildPrototypeInternal() {
        List<RecolorableBakedQuad> quads = super.buildPrototypeInternal();
        buildRestrictive(quads);
        return quads;
    }

    protected void buildRestrictive(List<RecolorableBakedQuad> list) {
        helper.setTargetSprite(restrictiveTex);
        for (EnumFacing facing : EnumFacing.VALUES) {
            int start = list.size();
            list.addAll(helper.visitTube(facing));
            restrictiveCoords.put(facing, new SubListAddress(start, list.size()));
        }
    }

    @Override
    public void addToList(List<BakedQuad> list, byte connectionMask, byte closedMask, byte blockedMask, int argb) {
        List<BakedQuad> quads = cache.getQuads(argb);
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (GTUtility.evalMask(facing, connectionMask)) {
                list.addAll(tubeCoords.get(facing).getSublist(quads));
                list.addAll(restrictiveCoords.get(facing).getSublist(quads));
                if (GTUtility.evalMask(facing, closedMask)) {
                    list.addAll(capperClosedCoords.get(facing).getSublist(quads));
                } else {
                    list.addAll(capperCoords.get(facing).getSublist(quads));
                }
                if (GTUtility.evalMask(facing, blockedMask)) {
                    list.addAll(blockedCoords.get(facing).getSublist(quads));
                }
            } else {
                list.addAll(coreCoords.get(facing).getSublist(quads));
            }
        }
    }
}
