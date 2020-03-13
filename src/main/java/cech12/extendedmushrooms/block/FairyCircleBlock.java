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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.LinkedList;

public class FairyCircleBlock extends AirBlock {

    private static Direction[] FAIRY_CIRCLE_DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    public FairyCircleBlock() {
        super(Block.Properties.create(Material.AIR).doesNotBlockMovement().noDrops());
    }

    @Override
    public BlockRenderType getRenderType(BlockState p_149645_1_) {
        return BlockRenderType.MODEL; //TODO remove this method
    }

    @Override
    public boolean isAir(BlockState p_196261_1_) {
        return false; //TODO remove this method
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
                FairyCircle fairyCircle = new FairyCircle(FairyCircle.getFairyCirclePositions(new LinkedList<>(), direction, clockwise, mutablePos.setPos(pos)));
                fairyCircle.placeBlocksIfValid(world);
            }
        }
    }


    private static class FairyCircle {

        LinkedList<BlockPos> circlePositions;

        /**
         * Construct a Fairy Circle with a list of 12 block positions.
         *
         * # B B #
         * B C C B
         * B C C B
         * # B B #
         *
         * @param circlePositions Must be a list of 12 block positions (first 8 border blocks [B], following 4 center blocks [C])
         */
        FairyCircle(LinkedList<BlockPos> circlePositions) {
            this.circlePositions = circlePositions;
        }

        /**
         * Recursive method to get all important positions of a fairy circle.
         * @param positions - empty linked list which is filled with all positions
         * @param direction - initial direction where it should look for the next block
         * @param clockwise - boolean to say if the circle should be checked clockwise or counter clockwise
         * @param mutablePos - a mutable block position, where the position of the initial block is set
         * @return list of all 12 important positions of the circle (parameter positions)
         */
        private static LinkedList<BlockPos> getFairyCirclePositions(LinkedList<BlockPos> positions, Direction direction, boolean clockwise, BlockPos.Mutable mutablePos) {
            Direction rotatedDirection = (clockwise) ? direction.rotateY() : direction.rotateYCCW();

            Direction newDirection = direction;
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
                return getFairyCirclePositions(positions, newDirection, clockwise, mutablePos);
            } else {
                return positions;
            }
        }

        private boolean isValid(IWorld world) {
            if (circlePositions.size() != 12) {
                return false;
            }
            //check if circle has mushrooms
            for (int position = 0; position < 8; position++) {
                if (!(world.getBlockState(circlePositions.get(position)).getBlock() instanceof MushroomBlock)) {
                    return false;
                }
            }
            //check if center is filled with air blocks and below must be a solid block
            for (int position = 8; position < 12; position++) {
                if (world.getBlockState(circlePositions.get(position)).getBlock() != Blocks.AIR ||
                        !world.getBlockState(circlePositions.get(position).down()).isSolid()) {
                    return false;
                }
            }
            return true;
        }

        void placeBlocksIfValid(IWorld world) {
            if (this.isValid(world)) {
                for (int position = 8; position < 12; position++) {
                    //2 - no block updates to avoid to calling neighborChanged while placing
                    world.setBlockState(this.circlePositions.get(position), ExtendedMushroomsBlocks.FAIRY_CIRCLE.getDefaultState(), 2);
                }
            }
        }

    }
}
