package cech12.extendedmushrooms.mixin;

import cech12.extendedmushrooms.api.block.ExtendedMushroomsBlocks;
import cech12.extendedmushrooms.block.mushrooms.BrownMushroom;
import cech12.extendedmushrooms.block.mushrooms.RedMushroom;
import cech12.extendedmushrooms.utils.TagUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MushroomBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(MushroomBlock.class)
public class MixinMushroomBlock {

    /**
     * Add a tree like automatic growing.
     * The automatic multiplication still remaining.
     */
    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void tickProxy(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        //automatic growing of mushrooms
        //Forge: prevent loading unloaded chunks
        if (world.isAreaLoaded(pos, 7) && random.nextInt(25) == 0) {
            if (state.getBlock() instanceof MushroomBlock) {
                Block blockBeneath = world.getBlockState(pos.down()).getBlock();
                if (TagUtils.hasTag(blockBeneath, TagUtils.MUSHROOM_GROWING_BLOCKS) ||
                        (TagUtils.hasTag(blockBeneath, TagUtils.MUSHROOM_GROWING_BLOCKS_LIGHTLEVEL) && world.getLightSubtracted(pos, 0) < 13)) {
                    //only grow when not part of a fairy ring
                    Direction[] directions = { Direction.NORTH, Direction.EAST, Direction.WEST, Direction.SOUTH };
                    boolean partOfFairyRing = false;
                    BlockPos.Mutable mutablePos = new BlockPos.Mutable();
                    for (Direction direction : directions) {
                        mutablePos.setPos(pos).move(direction);
                        if (world.getBlockState(mutablePos).getBlock() == ExtendedMushroomsBlocks.FAIRY_RING) {
                            partOfFairyRing = true;
                            break;
                        }
                    }
                    if (!partOfFairyRing) {
                        ((MushroomBlock) state.getBlock()).grow(world, random, pos, state);
                    }
                }
            }
        }
        //automatic multiplication follows in tick method when ci.canceled NOT called
        //ci.cancel();
    }


    /**
     * Add a tree like automatic growing.
     * The automatic multiplication still remaining.
     */
    @Inject(at = @At("HEAD"), method = "isValidPosition", cancellable = true)
    public void isValidPositionProxy(BlockState state, IWorldReader world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockPos blockpos = pos.down();
        BlockState blockstate = world.getBlockState(blockpos);
        Block block = blockstate.getBlock();
        if (TagUtils.hasTag(block, TagUtils.MUSHROOM_VALID_BLOCKS)) {
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(world.getLightSubtracted(pos, 0) < 13 && blockstate.canSustainPlant(world, blockpos, net.minecraft.util.Direction.UP, (IPlantable) state.getBlock()));
        }
        cir.cancel();
    }

    /**
     * Change grow behaviour to enable mega mushrooms can be grown out of vanilla mushrooms.
     */
    @Inject(at = @At("HEAD"), method = "func_226940_a_", cancellable = true)
    public void growProxy(ServerWorld world, BlockPos pos, BlockState state, Random random, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() == Blocks.BROWN_MUSHROOM) {
            (new BrownMushroom()).growMushroom(world, world.getChunkProvider().getChunkGenerator(), pos, state, random);
            cir.setReturnValue(true);
        } else if (state.getBlock() == Blocks.RED_MUSHROOM) {
            (new RedMushroom()).growMushroom(world, world.getChunkProvider().getChunkGenerator(), pos, state, random);
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }
        cir.cancel();
    }

}
