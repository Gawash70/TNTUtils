package ljfa.tntutils.handlers;

import java.util.function.Predicate;

import ljfa.tntutils.Config;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ExplosionHandler {
    @SubscribeEvent
    public void onExplosionStart(ExplosionEvent.Start event) {
        if(Config.disableExplosions)
            event.setCanceled(true);
        /*else
            event.explosion.explosionSize *= Config.sizeMultiplier;*/
    }

    @SubscribeEvent
    public void onExplosionDetonate(final ExplosionEvent.Detonate event) {
        if(event.world.isRemote)
            return;
        //Block damage
        if(Config.disableBlockDamage || (Config.disableCreeperBlockDamage && event.explosion.exploder instanceof EntityCreeper))
            event.explosion.affectedBlockPositions.clear();
        else if(Config.spareTileEntities || Config.blacklistActive) {
            //Remove blacklisted blocks and tile entities (if configured) from the list
            event.getAffectedBlocks().removeIf(new Predicate<ChunkPosition>() {
                @Override
                public boolean test(ChunkPosition pos) {
                    Block block = event.world.getBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
                    int meta = event.world.getBlockMetadata(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
                    return shouldBePreserved(block, meta);
                }
            });
        }
        
        //Entity damage
        if(Config.disableEntityDamage)
            event.getAffectedEntities().clear();
        else if(Config.disablePlayerDamage || Config.disableNPCDamage || Config.preventChainExpl) {
            //Remove certain from the list
            event.getAffectedEntities().removeIf(new Predicate<Entity>() {
                @Override
                public boolean test(Entity ent) {
                    return (Config.disableNPCDamage && ent instanceof EntityLivingBase && !(ent instanceof EntityPlayer))
                        || (Config.disablePlayerDamage && ent instanceof EntityPlayer)
                        || (Config.preventChainExpl && ent instanceof EntityMinecartTNT);
                }
            });
        }
    }

    public static boolean shouldBePreserved(Block block, int meta) {
        return Config.spareTileEntities && block.hasTileEntity(meta)
            || Config.blacklist.contains(block);
    }
}
