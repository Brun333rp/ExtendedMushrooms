package cech12.extendedmushrooms.world.gen.feature;

import cech12.extendedmushrooms.init.ModTags;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.HugeMushroomBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.BigMushroomFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

import java.util.Random;

public abstract class BigMushroomFeature extends Feature<BigMushroomFeatureConfig> {

    public BigMushroomFeature(Codec<BigMushroomFeatureConfig> config) {
        super(config);
    }

    protected abstract int getCapRadius(Random random);

    protected int getSize(Random random) {
        int i = random.nextInt(3) + 4;
        if (random.nextInt(12) == 0) {
            i *= 2;
        }
        return i;
    }

    protected boolean isInWorldBounds(IWorld world, BlockPos mushroomPos, int size) {
        int y = mushroomPos.getY();
        return y >= 1 && y + size + 1 < world.func_234938_ad_(); //getMaxHeight
    }

    protected boolean hasValidGround(IWorld world, BlockPos mushroomPos) {
        Block block = world.getBlockState(mushroomPos.down()).getBlock();
        return block.isIn(ModTags.Blocks.MUSHROOM_GROWING_BLOCKS) || block.isIn(ModTags.Blocks.MUSHROOM_GROWING_BLOCKS_LIGHTLEVEL);
    }

    protected boolean canGrow(IWorld world, BlockPos blockPos, int size, int capRadius, BlockPos.Mutable mutableBlockPos, BigMushroomFeatureConfig config) {
        if (!isInWorldBounds(world, blockPos, size)) {
            return false;
        }
        if (!this.hasValidGround(world, blockPos)) {
            return false;
        }
        if (!canPlaceTrunk(world, blockPos, size, mutableBlockPos, config)) {
            return false;
        }
        if (!canPlaceCap(world, blockPos, size, capRadius, mutableBlockPos, config)) {
            return false;
        }
        return true;
    }

    protected abstract boolean canPlaceCap(IWorld world, BlockPos blockPos, int size, int capRadius, BlockPos.Mutable mutableBlockPos, BigMushroomFeatureConfig config);

    protected abstract void placeCap(IWorld world, Random random, BlockPos blockPos, int size, int capRadius, BlockPos.Mutable mutableBlockPos, BigMushroomFeatureConfig config);

    protected boolean canPlaceTrunk(IWorld world, BlockPos blockPos, int size, BlockPos.Mutable mutableBlockPos, BigMushroomFeatureConfig config) {
        for(int i = 0; i < size; ++i) {
            mutableBlockPos.setPos(blockPos).move(Direction.UP, i);
            if (!world.getBlockState(mutableBlockPos).canBeReplacedByLogs(world, mutableBlockPos)) {
                return false;
            }
        }
        return true;
    }

    protected void placeTrunk(IWorld world, Random random, BlockPos blockPos, BigMushroomFeatureConfig config, int size, BlockPos.Mutable mutableBlockPos) {
        for(int i = 0; i < size; ++i) {
            mutableBlockPos.setPos(blockPos).move(Direction.UP, i);
            if (world.getBlockState(mutableBlockPos).canBeReplacedByLogs(world, mutableBlockPos)) {
                //setBlockState
                this.func_230367_a_(world, mutableBlockPos, config.field_227273_b_.getBlockState(random, blockPos));
            }
        }
    }

    @Override
    //place
    public boolean func_241855_a(ISeedReader worldIn, ChunkGenerator generator, Random rand, BlockPos pos, BigMushroomFeatureConfig config) {
        int size = this.getSize(rand);
        int capRadius = this.getCapRadius(rand);
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
        if (!this.canGrow(worldIn, pos, size, capRadius, blockpos$mutable, config)) {
            return false;
        } else {
            this.placeCap(worldIn, rand, pos, size, capRadius, blockpos$mutable, config);
            this.placeTrunk(worldIn, rand, pos, config, size, blockpos$mutable);
            return true;
        }
    }

    protected void placeCapBlockIfPossible(IWorld world, Random random, BigMushroomFeatureConfig config, BlockPos blockPos, boolean west, boolean east, boolean north, boolean south) {
        this.placeCapBlockIfPossible(world, random, config, blockPos, west, east, north, south, true);
    }

    protected void placeCapBlockIfPossible(IWorld world, Random random, BigMushroomFeatureConfig config, BlockPos blockPos, boolean west, boolean east, boolean north, boolean south, boolean up) {
        if (world.getBlockState(blockPos).canBeReplacedByLeaves(world, blockPos)) {
            //setBlockState
            this.func_230367_a_(world, blockPos, config.field_227272_a_.getBlockState(random, blockPos).with(HugeMushroomBlock.WEST, west).with(HugeMushroomBlock.EAST, east).with(HugeMushroomBlock.NORTH, north).with(HugeMushroomBlock.SOUTH, south).with(HugeMushroomBlock.UP, up));
        }
    }

}
