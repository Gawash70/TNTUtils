package ljfa.tntutils.proxy;

import static ljfa.tntutils.TNTUtils.logger;

import java.util.List;

import ljfa.tntutils.Config;
import ljfa.tntutils.TNTUtils;
import ljfa.tntutils.blocks.BlockReplacedTNT;
import ljfa.tntutils.handlers.EntityJoinHandler;
import ljfa.tntutils.handlers.ExplosionHandler;
import ljfa.tntutils.util.LogHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameData;

import com.google.common.collect.BiMap;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        Config.loadConfig(event.getSuggestedConfigurationFile());
        
        if(Config.preventChainExpl) {
            TNTUtils.replaced_tnt = new BlockReplacedTNT().setHardness(0.0F).setStepSound(Block.soundTypeGrass).setUnlocalizedName("tnt");
            replaceVanillaTNT();
        }
    }
    
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ExplosionHandler());
        if(Config.disableTNT || Config.disableTNTMinecart)
            MinecraftForge.EVENT_BUS.register(new EntityJoinHandler());
    }
    
    public void postInit(FMLPostInitializationEvent event) {
        
    }
    
    @SuppressWarnings("unchecked")
    private void replaceVanillaTNT() {
        try {
            logger.info("About to replace Vanilla TNT");
            
            //Get TNT object to replace
            Block oldTNT = (Block)Block.blockRegistry.getObject("tnt");
            int tntID = Block.blockRegistry.getIDForObject(oldTNT);

            //Replace it in the "underlyingIntegerMap"
            Block.blockRegistry.underlyingIntegerMap.put(TNTUtils.replaced_tnt, tntID);
            
            //Replace it in the "registryObjects"
            BiMap<ResourceLocation, Block> regMap = (BiMap)Block.blockRegistry.registryObjects;
            regMap.forcePut(new ResourceLocation("minecraft:tnt"), TNTUtils.replaced_tnt);
            
            //Replace it in the associated ItemBlock
            ItemBlock tntItem = (ItemBlock)Item.itemRegistry.getObjectById(tntID);
            tntItem.block = TNTUtils.replaced_tnt;
            
            //Add it to the Block -> Item map
            GameData.getBlockItemMap().put(TNTUtils.replaced_tnt, tntItem);
            
            //Add the block states to the Block State -> ID map, imitating how it is done in GameRegistry
            for(IBlockState state: (List<IBlockState>)TNTUtils.replaced_tnt.getBlockState().getValidStates()) {
                int id = tntID << 4 | TNTUtils.replaced_tnt.getMetaFromState(state);
                GameData.getBlockStateIDMap().put(state, id);
            }
            
            //Replace it in the Blocks class
            Blocks.tnt = TNTUtils.replaced_tnt;
            
            logger.info("Replaced Vanilla TNT");
        } catch(Exception ex) {
            throw new RuntimeException("Failed to replace Vanilla TNT!", ex);
        }
    }
}
