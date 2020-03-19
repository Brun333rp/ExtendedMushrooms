package cech12.extendedmushrooms.client.renderer.tileentity;

import cech12.extendedmushrooms.ExtendedMushrooms;
import cech12.extendedmushrooms.api.entity.ExtendedMushroomsEntityTypes;
import cech12.extendedmushrooms.api.recipe.FairyRingMode;
import cech12.extendedmushrooms.client.ClientTickObserver;
import cech12.extendedmushrooms.client.renderer.entity.FairyRenderer;
import cech12.extendedmushrooms.entity.passive.FairyEntity;
import cech12.extendedmushrooms.tileentity.FairyRingTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.WitchEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class FairyRingTileEntityRenderer extends TileEntityRenderer<FairyRingTileEntity> {

    private FairyRenderer fairyRenderer;
    private FairyEntity fairyEntity;
    private FairyRingWitchRenderer witchRenderer;
    private WitchEntity witchEntity;

    public FairyRingTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    private EntityRendererManager getEntityRendererManager() {
        return Minecraft.getInstance().getRenderManager();
    }

    private FairyRenderer getFairyRenderer() {
        return (this.fairyRenderer == null) ? (this.fairyRenderer = new FairyRenderer(this.getEntityRendererManager())) : this.fairyRenderer;
    }

    private FairyEntity getFairyEntity(World world) {
        if (this.fairyEntity == null) {
            this.fairyEntity = new FairyEntity((EntityType<FairyEntity>) ExtendedMushroomsEntityTypes.FAIRY, world);
            this.fairyEntity.setNoAI(true);
        }
        return this.fairyEntity;
    }

    private FairyRingWitchRenderer getWitchRenderer() {
        return (this.witchRenderer == null) ? (this.witchRenderer = new FairyRingWitchRenderer(this.getEntityRendererManager())) : this.witchRenderer;
    }

    private WitchEntity getWitchEntity(World world) {
        if (this.witchEntity == null) {
            this.witchEntity = new WitchEntity(EntityType.WITCH, world);
            this.witchEntity.setNoAI(true);
        }
        return this.witchEntity;
    }

    @Override
    public void render(@Nonnull FairyRingTileEntity fairyRing, float partticks, @Nonnull MatrixStack matrixStack, @Nonnull IRenderTypeBuffer iRenderTypeBuffer, int p1, int p2) {
        //only render inventory of master
        if (fairyRing.isMaster()) {
            matrixStack.push();
            //move to ring center
            Vec3d centerTranslation = FairyRingTileEntity.CENTER_TRANSLATION_VECTOR;
            matrixStack.translate(centerTranslation.x, centerTranslation.y, centerTranslation.z);

            int itemCount = 0;
            for (int i = 0; i < fairyRing.getSizeInventory(); i++) {
                if (!fairyRing.getStackInSlot(i).isEmpty()) {
                    itemCount++;
                }
            }

            double time = ClientTickObserver.ticksSinceStart + partticks;

            //get recipe progress
            float quadraticRecipeProgress = 0;
            float quadraticRecipeProgressInverse = 1;
            if (fairyRing.getRecipeTimeTotal() > 0) {
                float recipeProgress = (float) fairyRing.getRecipeTime() / (float) fairyRing.getRecipeTimeTotal();
                float recipeProgressInverse = 1 - recipeProgress;
                quadraticRecipeProgress = recipeProgress * recipeProgress;
                quadraticRecipeProgressInverse = recipeProgressInverse * recipeProgressInverse;
            }
            float anglePerItem = 360F / itemCount;
            Minecraft mc = Minecraft.getInstance();
            mc.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            Vector3f yAxis = new Vector3f(0, 1, 0);
            for(int i = 0; i < fairyRing.getSizeInventory(); i++) {
                matrixStack.push();
                if (itemCount > 1) {
                    //deposit items in a circle, when more than one items are in inventory
                    matrixStack.rotate(new Quaternion(yAxis, (float) Math.toRadians(anglePerItem * i), false));
                    matrixStack.translate(0.75 * quadraticRecipeProgressInverse, 0, 0);
                }
                //add some (slow) motion
                matrixStack.rotate(new Quaternion(yAxis, (float) Math.toRadians((time / 3 + i * 10) % 360), false));
                matrixStack.translate(0, Math.sin((time + i * 10) / 10.0) * 0.01 + 0.05 + (1.1 * quadraticRecipeProgress), 0);
                //render item
                ItemStack stack = fairyRing.getStackInSlot(i);
                if(!stack.isEmpty()) {
                    mc.getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.GROUND, p1, p2, matrixStack, iRenderTypeBuffer);
                }
                matrixStack.pop();
            }

            //render entity inside of ring
            if (fairyRing.getMode() != FairyRingMode.NORMAL) {
                matrixStack.push();
                //let entity look into players direction
                if (fairyRing.getWorld() != null) {
                    Vec3d center = fairyRing.getCenter();
                    PlayerEntity player = fairyRing.getWorld().getClosestPlayer(center.x, center.y, center.z);
                    if (player != null) {
                        Vec3d playerPos = player.getPositionVec();
                        if (center.distanceTo(playerPos) < 6) {
                            Vec3d flattenedPlayerPos = new Vec3d(playerPos.x, center.y, playerPos.z);
                            Vec3d south = new Vec3d(0, 0, 1);
                            Vec3d direction = flattenedPlayerPos.subtract(center).normalize();
                            double angle = Math.acos(direction.dotProduct(south));
                            if (playerPos.x < center.x) {
                                angle = -angle;
                            }
                            matrixStack.rotate(new Quaternion(yAxis, (float) angle, false));
                        }
                    }
                }
                if (fairyRing.getMode() == FairyRingMode.FAIRY) {
                    FairyEntity entity = this.getFairyEntity(fairyRing.getWorld());
                    this.getFairyRenderer().render(entity, /*yaw:*/0, partticks, matrixStack, iRenderTypeBuffer, this.getEntityRendererManager().getPackedLight(entity, partticks));
                } else if (fairyRing.getMode() == FairyRingMode.WITCH) {
                    WitchEntity entity = this.getWitchEntity(fairyRing.getWorld());
                    this.getWitchRenderer().render(entity, /*yaw:*/0, partticks, matrixStack, iRenderTypeBuffer, this.getEntityRendererManager().getPackedLight(entity, partticks));
                }
                matrixStack.pop();
            }

            matrixStack.pop();
        }
    }

    private static class FairyRingWitchRenderer extends WitchRenderer {

        private final ResourceLocation WITCH_TEXTURES = new ResourceLocation(ExtendedMushrooms.MOD_ID, "textures/entity/witch.png");

        public FairyRingWitchRenderer(EntityRendererManager p_i46131_1_) {
            super(p_i46131_1_);
        }

        @Nonnull
        @Override
        public ResourceLocation getEntityTexture(WitchEntity p_110775_1_) {
            return WITCH_TEXTURES;
        }
    }

}
