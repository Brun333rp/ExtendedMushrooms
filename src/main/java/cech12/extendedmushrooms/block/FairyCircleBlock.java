package cech12.extendedmushrooms.block;


import cech12.extendedmushrooms.api.block.ExtendedMushroomsBlocks;
import cech12.extendedmushrooms.tileentity.FairyCircleTileEntity;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MushroomBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class FairyCircleBlock extends AirBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static Direction[] FAIRY_CIRCLE_DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    public FairyCircleBlock() {
        super(Block.Properties.create(Material.AIR).doesNotBlockMovement().noDrops());
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Nonnull
    @Override
    public BlockRenderType getRenderType(BlockState p_149645_1_) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FairyCircleTileEntity();
    }

    /**
     * Called periodically client side on blocks near the player to show effects.
     */
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        super.animateTick(stateIn, worldIn, pos, rand);
        worldIn.addParticle(ParticleTypes.MYCELIUM, -0.5 + pos.getX() + rand.nextFloat() * 2, pos.getY() + 0.1F, -0.5 + pos.getZ() + rand.nextFloat() * 2, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void neighborChanged(BlockState blockState, World world, BlockPos blockPos, Block block, BlockPos neighbourPos, boolean isMoving) {
        //check for 2 mushrooms and 2 Fairy Circle blocks as neighbour. (diagonally the same)
        //check if block below is solid
        boolean mushroomSeen = false;
        int mushrooms = 0;
        boolean fairyCircleBlockSeen = false;
        int fairyCircleBlocks = 0;
        boolean neighboursFound = false;
        for (Direction direction : FAIRY_CIRCLE_DIRECTIONS) {
            Block neighbourBlock = world.getBlockState(blockPos.offset(direction)).getBlock();
            if (neighbourBlock instanceof MushroomBlock) {
                mushrooms++;
                if (mushroomSeen) {
                    neighboursFound = true;
                } else {
                    mushroomSeen = true;
                    fairyCircleBlockSeen = false;
                }
            } else if (neighbourBlock == ExtendedMushroomsBlocks.FAIRY_CIRCLE) {
                fairyCircleBlocks++;
                if (fairyCircleBlockSeen) {
                    neighboursFound = true;
                } else {
                    mushroomSeen = false;
                    fairyCircleBlockSeen = true;
                }
            } else {
                break;
            }
        }

        if (!neighboursFound || mushrooms != 2 || fairyCircleBlocks != 2 || !world.getBlockState(blockPos.down()).isSolid()) {
            //remove me
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
        }

        super.neighborChanged(blockState, world, blockPos, block, neighbourPos, isMoving);
    }


    /**
     * Check for built fairy circle and if true, place fairy circle blocks.
     */
    public static void fairyCirclePlaceCheck(IWorld world, BlockPos pos) {
        //check for formed fairy circles
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        boolean[] clockwises = new boolean[]{true, false};
        for (Direction direction : FAIRY_CIRCLE_DIRECTIONS) {
            for (boolean clockwise : clockwises) {
                try {
                    FairyCircle fairyCircle = new FairyCircle(world, direction, clockwise, mutablePos.setPos(pos));
                    fairyCircle.placeBlocks(world);
                } catch (FairyCircle.CannotFormFairyCircleException ignore) {}
            }
        }
    }


    private static class FairyCircle {

        static class CannotFormFairyCircleException extends Exception {}

        /** a list of 12 block positions (first 8 border blocks [B], following 4 center blocks [C]) */
        LinkedList<BlockPos> circlePositions;

        /**
         * Construct a Fairy Circle with a list of 12 block positions.
         *
         * # B B #
         * B C C B
         * B C C B
         * # B B #
         */
        FairyCircle(IWorld world, Direction direction, boolean clockwise, BlockPos.Mutable mutablePos) throws CannotFormFairyCircleException {
            this.circlePositions = getFairyCirclePositions(world, new LinkedList<>(), direction, clockwise, mutablePos);
            if (this.circlePositions == null || circlePositions.size() != 12) {
                throw new CannotFormFairyCircleException();
            }
        }

        /**
         * Recursive method to get all important positions of a fairy circle.
         * @param positions - empty linked list which is filled with all positions
         * @param direction - initial direction where it should look for the next block
         * @param clockwise - boolean to say if the circle should be checked clockwise or counter clockwise
         * @param mutablePos - a mutable block position, where the position of the initial block is set
         * @return list of all 12 important positions of the circle (parameter positions) - returns null, when circle cannot be placed
         */
        private static LinkedList<BlockPos> getFairyCirclePositions(IWorld world, LinkedList<BlockPos> positions, Direction direction, boolean clockwise, BlockPos.Mutable mutablePos) {
            Direction rotatedDirection = (clockwise) ? direction.rotateY() : direction.rotateYCCW();
            Direction newDirection = direction;

            //check if circle has mushrooms
            if (positions.size() < 8 && !(world.getBlockState(mutablePos).getBlock() instanceof MushroomBlock)) {
                return null;
            } else
            //check if center is filled with air blocks and below must be a solid block
            if (positions.size() >= 8 && (world.getBlockState(mutablePos).getBlock() != Blocks.AIR ||
                        !world.getBlockState(mutablePos.down()).isSolid())) {
                return null;
            }

            //put position in list
            positions.add(new BlockPos(mutablePos));

            if (positions.size() != 8) {
                //don't move forward when at last border block
                mutablePos.move(direction);
            }

            if (positions.size() >= 8 || positions.size() % 2 == 0) {
                //rotate direction for center blocks of for even border blocks
                newDirection = rotatedDirection;
            }

            if (positions.size() == 8 || (positions.size() < 8 && positions.size() % 2 == 0)) {
                // at last border block only move in rotated direction
                // for even border blocks additional move in rotated direction (to go diagonally)
                mutablePos.move(newDirection);
            }

            if (positions.size() < 12) {
                return getFairyCirclePositions(world, positions, newDirection, clockwise, mutablePos);
            } else {
                return positions;
            }
        }

        private List<BlockPos> getSortedCenterPositions() {
            List<BlockPos> list = this.circlePositions.subList(8, 12);
            list.sort((o1, o2) -> {
                int result = o1.getX() - o2.getX();
                if (result == 0) {
                    return o1.getZ() - o2.getZ();
                }
                return result;
            });
            return list;
        }

        void placeBlocks(IWorld world) {
            List<BlockPos> list = getSortedCenterPositions();
            for (int i = 0; i < 4; i++) {
                BlockState state = ExtendedMushroomsBlocks.FAIRY_CIRCLE.getDefaultState().with(FairyCircleBlock.FACING, FAIRY_CIRCLE_DIRECTIONS[i]);
                //2 - no block updates to avoid to calling neighborChanged while placing
                world.setBlockState(list.get(i), state, 2);
            }
        }

    }
}
