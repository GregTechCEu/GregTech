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
public class BlockableSQC extends StructureQuadCache {

    protected final EnumMap<EnumFacing, SubListAddress> blockedCoords = new EnumMap<>(EnumFacing.class);

    protected final SpriteInformation blockedTex;

    protected BlockableSQC(PipeQuadHelper helper, SpriteInformation endTex, SpriteInformation sideTex,
                           SpriteInformation blockedTex) {
        super(helper, endTex, sideTex);
        this.blockedTex = blockedTex;
    }

    public static @NotNull BlockableSQC create(PipeQuadHelper helper, SpriteInformation endTex,
                                               SpriteInformation sideTex, SpriteInformation blockedTex) {
        BlockableSQC cache = new BlockableSQC(helper, endTex, sideTex, blockedTex);
        cache.buildPrototype();
        return cache;
    }

    @Override
    protected List<RecolorableBakedQuad> buildPrototypeInternal() {
        List<RecolorableBakedQuad> quads = super.buildPrototypeInternal();
        buildBlocked(quads);
        return quads;
    }

    protected void buildBlocked(List<RecolorableBakedQuad> list) {
        helper.setTargetSprite(blockedTex);
        for (EnumFacing facing : EnumFacing.VALUES) {
            int start = list.size();
            list.addAll(helper.visitTube(facing));
            blockedCoords.put(facing, new SubListAddress(start, list.size()));
        }
    }

    @Override
    public void addToList(List<BakedQuad> list, byte connectionMask, byte closedMask, byte blockedMask, int argb) {
        List<BakedQuad> quads = cache.getQuads(argb);
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (GTUtility.evalMask(facing, connectionMask)) {
                list.addAll(tubeCoords.get(facing).getSublist(quads));
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
