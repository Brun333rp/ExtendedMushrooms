package cech12.extendedmushrooms.init;

import cech12.extendedmushrooms.ExtendedMushrooms;
import cech12.extendedmushrooms.api.block.ExtendedMushroomsBlocks;
import cech12.extendedmushrooms.client.renderer.tileentity.FairyCircleTileEntityRenderer;
import cech12.extendedmushrooms.tileentity.FairyCircleTileEntity;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

import static cech12.extendedmushrooms.api.tileentity.ExtendedMushroomsTileEntities.*;

@Mod.EventBusSubscriber(modid= ExtendedMushrooms.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModTileEntities {

    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        FAIRY_CIRCLE = register(FairyCircleTileEntity::new, "fairy_circle", ExtendedMushroomsBlocks.FAIRY_CIRCLE, event);
    }

    private static <T extends TileEntity> TileEntityType<T> register(Supplier<T> supplier, String registryName, Block block, RegistryEvent.Register<TileEntityType<?>> registryEvent) {
        TileEntityType<T> tileEntityType = TileEntityType.Builder.create(supplier, block).build(null);
        tileEntityType.setRegistryName(registryName);
        registryEvent.getRegistry().register(tileEntityType);
        return tileEntityType;
    }

    /**
     * Setup renderers for entities. Is called at mod initialisation.
     */
    @OnlyIn(Dist.CLIENT)
    public static void setupRenderers() {
        ClientRegistry.bindTileEntityRenderer((TileEntityType<FairyCircleTileEntity>) FAIRY_CIRCLE, FairyCircleTileEntityRenderer::new);
    }


}
