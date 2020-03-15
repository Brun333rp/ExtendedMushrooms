package cech12.extendedmushrooms.client.renderer.tileentity;

import cech12.extendedmushrooms.client.ClientTickObserver;
import cech12.extendedmushrooms.tileentity.FairyCircleTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

public class FairyCircleTileEntityRenderer extends TileEntityRenderer<FairyCircleTileEntity> {

    public FairyCircleTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
        super(p_i226006_1_);
    }

    @Override
    public void render(@Nonnull FairyCircleTileEntity fairyCircle, float partticks, @Nonnull MatrixStack matrixStack, @Nonnull IRenderTypeBuffer iRenderTypeBuffer, int p1, int p2) {
        //only render inventory of master
        if (fairyCircle.isMaster()) {
            matrixStack.push();
            //move to circle center
            Vec3d centerTranslation = FairyCircleTileEntity.CENTER_TRANSLATION_VECTOR;
            matrixStack.translate(centerTranslation.x, centerTranslation.y, centerTranslation.z);

            int itemCount = 0;
            for (int i = 0; i < fairyCircle.getSizeInventory(); i++) {
                if (!fairyCircle.getStackInSlot(i).isEmpty()) {
                    itemCount++;
                }
            }

            double time = ClientTickObserver.ticksSinceStart + partticks;

            float anglePerItem = 360F / itemCount;
            Minecraft mc = Minecraft.getInstance();
            mc.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            Vector3f yAxis = new Vector3f(0, 1, 0);
            for(int i = 0; i < fairyCircle.getSizeInventory(); i++) {
                matrixStack.push();
                if (itemCount > 1) {
                    //deposit items in a circle, when more than one items are in inventory
                    matrixStack.rotate(new Quaternion(yAxis, (float) Math.toRadians(anglePerItem * i), false));
                    matrixStack.translate(0.75, 0, 0);
                }
                //add some (slow) motion
                matrixStack.rotate(new Quaternion(yAxis, (float) Math.toRadians((time / 3 + i * 10) % 360), false));
                matrixStack.translate(0, Math.sin((time + i * 10) / 10.0) * 0.01 + 0.05, 0);
                //render item
                ItemStack stack = fairyCircle.getStackInSlot(i);
                if(!stack.isEmpty()) {
                    mc.getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.GROUND, p1, p2, matrixStack, iRenderTypeBuffer);
                }
                matrixStack.pop();
            }
            matrixStack.pop();
        }

    }

}
