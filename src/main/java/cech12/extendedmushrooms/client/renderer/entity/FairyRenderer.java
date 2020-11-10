package cech12.extendedmushrooms.client.renderer.entity;

import cech12.extendedmushrooms.ExtendedMushrooms;
import cech12.extendedmushrooms.entity.passive.FairyEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;

public class FairyRenderer extends BipedRenderer<FairyEntity, BipedModel<FairyEntity>> {
    private final ResourceLocation FAIRY_TEXTURES = new ResourceLocation(ExtendedMushrooms.MOD_ID, "textures/entity/fairy.png");

    public FairyRenderer(EntityRendererManager entityRendererManager) {
        super(entityRendererManager, new BipedModel<>(RenderType::getEntityCutoutNoCull, 0.1F, 0F, 64, 64), 0.1F);
    }

    @Override
    public ResourceLocation getEntityTexture(FairyEntity entity) {
        return FAIRY_TEXTURES;
    }
}
