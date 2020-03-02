package cech12.extendedmushrooms.mixin;

import cech12.extendedmushrooms.utils.TagUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.AbstractBigMushroomFeature;
import net.minecraft.world.gen.feature.BigMushroomFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBigMushroomFeature.class)
public abstract class MixinAbstractBigMushroomFeature {

    /**
     * Remove dirt check and add tag check
     * The automatic multiplication still remaining.
     */
    @Inject(at = @At("HEAD"), method = "func_227209_a_", cancellable = true)
    public void placeProxy(IWorld world, BlockPos pos, int p_227209_3_, BlockPos.Mutable p_227209_4_, BigMushroomFeatureConfig p_227209_5_, CallbackInfoReturnable<Boolean> cir) {
        int i = pos.getY();
        if (i >= 1 && i + p_227209_3_ + 1 < world.getMaxHeight()) {
            Block block = world.getBlockState(pos.down()).getBlock();
            if (TagUtils.hasTag(block, TagUtils.MUSHROOM_GROWING_BLOCKS) || TagUtils.hasTag(block, TagUtils.MUSHROOM_GROWING_BLOCKS_LIGHTLEVEL)) {
                for(int j = 0; j <= p_227209_3_; ++j) {
                    //int k = this.func_225563_a_(-1, -1, p_227209_5_.field_227274_c_, j);
                    int k = j <= 3 ? 0 : p_227209_5_.field_227274_c_;

                    for(int l = -k; l <= k; ++l) {
                        for(int i1 = -k; i1 <= k; ++i1) {
                            BlockState blockstate = world.getBlockState(p_227209_4_.setPos(pos).move(l, j, i1));
                            if (!blockstate.isAir(world, p_227209_4_) && !blockstate.isIn(BlockTags.LEAVES)) {
                                cir.setReturnValue(false);
                                cir.cancel();
                                return;
                            }
                        }
                    }
                }
                cir.setReturnValue(true);
                cir.cancel();
                return;
            }
        }
        cir.setReturnValue(false);
        cir.cancel();
    }

}
