package cech12.extendedmushrooms.tileentity;

import cech12.extendedmushrooms.api.tileentity.ExtendedMushroomsTileEntities;
import cech12.extendedmushrooms.block.FairyCircleBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class FairyCircleTileEntity extends TileEntity implements IInventory, ITickableTileEntity {

    private boolean hasMaster;
    private boolean isMaster;
    private BlockPos masterPos;

    private NonNullList<ItemStack> items = NonNullList.create();
    private int recipeTime;
    private int recipeTimeTotal;

    public FairyCircleTileEntity() {
        super(ExtendedMushroomsTileEntities.FAIRY_CIRCLE);
        this.hasMaster = false;
        this.isMaster = false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        //if master value is already set, do nothing
        if (this.isMaster() || this.hasMaster()) {
            return;
        }
        //initial loading of this tile entity.
        //search for a master, when not found, I am the master
        World world = this.getWorld();
        BlockPos pos = this.getPos();
        if (world != null) {
            for (Direction direction : FairyCircleBlock.FAIRY_CIRCLE_DIRECTIONS) {
                TileEntity tileEntity = world.getTileEntity(pos.offset(direction));
                if (tileEntity instanceof FairyCircleTileEntity) {
                    this.setMaster((FairyCircleTileEntity) tileEntity);
                    break;
                }
            }
        }
        if (!this.hasMaster()) {
            this.setAsMaster();
        }
    }

    public boolean hasMaster() {
        return hasMaster;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(FairyCircleTileEntity tileEntity) {
        this.isMaster = false;
        this.hasMaster = true;
        this.masterPos = tileEntity.getPos();
    }

    public void setAsMaster() {
        this.isMaster = true;
        this.hasMaster = false;
        this.masterPos = this.getPos();
    }

    public FairyCircleTileEntity getMaster() {
        if (this.isMaster()) {
            return this;
        }
        if (this.hasMaster() && this.getWorld() != null) {
            TileEntity tileEntity = this.getWorld().getTileEntity(this.masterPos);
            if (tileEntity instanceof FairyCircleTileEntity) {
                return ((FairyCircleTileEntity) tileEntity);
            }
        }
        return null;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.masterPos = new BlockPos(compound.getInt("MasterX"), compound.getInt("MasterY"), compound.getInt("MasterZ"));
        this.hasMaster = compound.getBoolean("HasMaster");
        this.isMaster = compound.getBoolean("IsMaster");
        if (this.isMaster()) {
            this.items = NonNullList.create();
            //ItemStackHelper.loadAllItems needs a fixed size. so own implementation
            ListNBT nbtList = compound.getList("Items", 10);
            for(int i = 0; i < nbtList.size(); ++i) {
                this.items.add(ItemStack.read(nbtList.getCompound(i)));
            }
            this.recipeTime = compound.getInt("RecipeTime");
            this.recipeTimeTotal = compound.getInt("RecipeTimeTotal");
        }
    }

    @Override
    @Nonnull
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("MasterX", this.masterPos.getX());
        compound.putInt("MasterY", this.masterPos.getY());
        compound.putInt("MasterZ", this.masterPos.getZ());
        compound.putBoolean("HasMaster", hasMaster);
        compound.putBoolean("IsMaster", isMaster);
        if (this.isMaster()) {
            ItemStackHelper.saveAllItems(compound, this.items);
            compound.putInt("RecipeTime", this.recipeTime);
            compound.putInt("RecipeTimeTotal", this.recipeTimeTotal);
        }
        return compound;
    }

    /**
     * Collect Item Entities.
     */
    public void onEntityCollision(World world, Entity entity) {
        if (!this.isMaster()) {
            FairyCircleTileEntity master = this.getMaster();
            if (master != null) {
                master.onEntityCollision(world, entity);
            }
        } else {
            if (entity instanceof ItemEntity) {
                //Collect Item Entities.
                ItemEntity itemEntity = (ItemEntity) entity;
                this.items.add(itemEntity.getItem());
                itemEntity.remove();
            } else if (entity instanceof PlayerEntity) {
                //Give entering player all stored items.
                if (this.items.size() > 0) {
                    PlayerEntity playerEntity = (PlayerEntity) entity;
                    Iterator<ItemStack> iterator = this.items.iterator();
                    while (iterator.hasNext()) {
                        ItemStack stack = iterator.next();
                        if (!playerEntity.inventory.addItemStackToInventory(stack)) {
                            break;
                        }
                        iterator.remove();
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        if (this.isMaster()) {
            //TODO do affection!
        }
    }


    public int getSizeInventory() {
        FairyCircleTileEntity master = this.getMaster();
        if (master != null) {
            return master.items.size();
        }
        return 0;
    }

    public boolean isEmpty() {
        FairyCircleTileEntity master = this.getMaster();
        if (master != null) {
            return master.items.size() == 0;
        }
        return true;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        FairyCircleTileEntity master = this.getMaster();
        if (master != null && slot < master.items.size()) {
            return master.items.get(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int slot, int count) {
        FairyCircleTileEntity master = this.getMaster();
        if (master != null && count > 0 && slot >= 0 && slot < master.items.size()) {
            ItemStack stack = master.items.get(slot);
            ItemStack splitStack = stack.split(count);
            stack.setCount(Math.max(0, stack.getCount() - count));
            if (stack.isEmpty()) {
                master.items.remove(stack);
            }
            return splitStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack removeStackFromSlot(int slot) {
        FairyCircleTileEntity master = this.getMaster();
        if (master != null && slot < master.items.size()) {
            return master.items.remove(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int slot, @Nonnull ItemStack itemStack) {
        FairyCircleTileEntity master = this.getMaster();
        if (master != null) {
            if (slot < master.items.size()) {
                master.items.set(slot, itemStack);
            } else {
                master.items.add(itemStack);
            }
        }
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull PlayerEntity playerEntity) {
        return false;
    }

    @Override
    public void clear() {
        FairyCircleTileEntity master = this.getMaster();
        if (master != null) {
            master.items.clear();
        }
    }
}
