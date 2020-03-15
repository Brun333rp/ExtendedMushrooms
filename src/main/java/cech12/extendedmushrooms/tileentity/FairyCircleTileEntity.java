package cech12.extendedmushrooms.tileentity;

import cech12.extendedmushrooms.api.block.ExtendedMushroomsBlocks;
import cech12.extendedmushrooms.api.tileentity.ExtendedMushroomsTileEntities;
import cech12.extendedmushrooms.block.FairyCircleBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FairyCircleTileEntity extends TileEntity implements IInventory, ITickableTileEntity {

    public static final Vec3d CENTER_TRANSLATION_VECTOR = new Vec3d(1, 0, 1);
    private static final int INVENTORY_SIZE = 16;

    private static final int EFFECT_EVENT = 0;

    private boolean hasMaster;
    private boolean isMaster;
    private BlockPos masterPos;

    private NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
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
        //use onLoad only on server (setup of clients happens via nbt sync)
        if (this.getWorld() != null && !this.getWorld().isRemote) {
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
                        this.setMaster(((FairyCircleTileEntity) tileEntity).getMaster());
                        break;
                    }
                }
            }
            if (!this.hasMaster()) {
                this.setAsMaster();
            }
        }
    }

    /**
     * Should only be called by master!
     */
    public Vec3d getCenter() {
        return new Vec3d(this.getPos()).add(CENTER_TRANSLATION_VECTOR);
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
        this.sendUpdates();
    }

    public void setAsMaster() {
        this.isMaster = true;
        this.hasMaster = false;
        this.masterPos = this.getPos();
        this.sendUpdates();
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
            this.items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
            ItemStackHelper.loadAllItems(compound, this.items);
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

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.getPos(), 3, this.getUpdateTag());
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(pkt.getNbtCompound());
    }

    /**
     * Informs clients about state changes of this tile entity.
     * Should be called every time when a state value is updated. (in setters)
     */
    private void sendUpdates() {
        if (this.getWorld() != null) {
            BlockState state = this.getWorld().getBlockState(this.getPos());
            this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 3);
        }
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
                ItemStack remainingStack = this.addItemStack(itemEntity.getItem());
                if (remainingStack == ItemStack.EMPTY) {
                    //when fully added, remove entity
                    itemEntity.remove();
                } else {
                    //when not or partly added, set new stack
                    itemEntity.setItem(remainingStack);
                    //itemEntity shouldn't stay inside of FairyCircleTileEntity (performance issue)
                    //so, push remaining stack to border.
                    Vec3d centerToStack = itemEntity.getPositionVec().subtract(this.getCenter());
                    double scaleFactor = (1.8 - centerToStack.length()) * 0.08; //1.8 is sqrt(3) | 0.08 is speed
                    Vec3d calculatedMotion = new Vec3d(centerToStack.x, 0, centerToStack.z).normalize().scale(scaleFactor);
                    itemEntity.setMotion(itemEntity.getMotion().add(calculatedMotion));
                }
            } else if (entity instanceof PlayerEntity) {
                //Give entering player all stored items.
                PlayerEntity playerEntity = (PlayerEntity) entity;
                for (int i = 0; i < this.getSizeInventory(); i++) {
                    ItemStack stack = this.getStackInSlot(i);
                    if (playerEntity.inventory.addItemStackToInventory(stack)) {
                        this.decrStackSize(i, stack.getCount());
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        if (this.isMaster() && this.getWorld() != null) {
            //some particles in center
            this.getWorld().addBlockEvent(this.getPos(), ExtendedMushroomsBlocks.FAIRY_CIRCLE, EFFECT_EVENT, 0);
            //TODO do cool stuff!
        }
    }

    /**
     * Is called by client when server sends a block event via World#addBlockEvent.
     * @return Should return true, when event is correct and has an effect.
     */
    @Override
    public boolean receiveClientEvent(int id, int param) {
        switch (id) {
            case EFFECT_EVENT: {
                if (this.getWorld() != null && this.getWorld().isRemote) {
                    Vec3d center = this.getCenter();
                    //TODO some nice effects!
                    this.getWorld().addParticle(ParticleTypes.MYCELIUM, center.x, center.y, center.z, 0.0D, 0.0D, 0.0D);
                }
                return true;
            }
            default: return super.receiveClientEvent(id, param);
        }
    }

    /**
     *
     * @param stack ItemStack which should be added.
     * @return The remaining ItemStack, which cannot be added or ItemStack.EMPTY when fully added
     */
    public ItemStack addItemStack(ItemStack stack) {
        FairyCircleTileEntity master = this.getMaster();
        if (stack != null && master != null && !stack.isEmpty()) {
            boolean changed = false;
            //each slot has only a stack size of 1
            for (int i = 0; i < master.items.size(); i++) {
                if (master.items.get(i).isEmpty()) {
                    master.setInventorySlotContents(i, stack.split(1));
                    changed = true;
                    if (stack.isEmpty()) {
                        break;
                    }
                }
            }
            if (changed) {
                this.sendUpdates();
            }
        }
        return stack;
    }

    @Override
    public int getSizeInventory() {
        FairyCircleTileEntity master = this.getMaster();
        if (master != null) {
            return master.items.size();
        }
        return 0;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        FairyCircleTileEntity master = this.getMaster();
        if (master != null) {
            for (ItemStack itemstack : master.items) {
                if (!itemstack.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        FairyCircleTileEntity master = this.getMaster();
        if (master != null && slot >= 0 && slot < master.items.size()) {
            return master.items.get(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int slot, int count) {
        FairyCircleTileEntity master = this.getMaster();
        ItemStack stack = ItemStack.EMPTY;
        if (master != null && count > 0 && slot >= 0 && slot < master.items.size()) {
            stack = ItemStackHelper.getAndSplit(master.items, slot, count);
            this.sendUpdates();
        }
        return stack;
    }

    @Override
    @Nonnull
    public ItemStack removeStackFromSlot(int slot) {
        FairyCircleTileEntity master = this.getMaster();
        if (master != null && slot < master.items.size()) {
            return ItemStackHelper.getAndRemove(master.items, slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int slot, @Nonnull ItemStack itemStack) {
        FairyCircleTileEntity master = this.getMaster();
        if (master != null && slot >= 0 && slot < master.items.size()) {
            master.items.set(slot, itemStack);
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
