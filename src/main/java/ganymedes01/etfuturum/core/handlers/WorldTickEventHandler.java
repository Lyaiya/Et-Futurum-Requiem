package ganymedes01.etfuturum.core.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import codechicken.lib.math.MathHelper;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ganymedes01.etfuturum.IConfigurable;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.client.sound.NetherAmbience;
import ganymedes01.etfuturum.client.sound.NetherAmbienceLoop;
import ganymedes01.etfuturum.client.sound.WeightedSoundPool;
import ganymedes01.etfuturum.configuration.ConfigurationHandler;
import ganymedes01.etfuturum.lib.Reference;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenWorking;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class WorldTickEventHandler {

    private static Map<Block, Block> replacements;
    private Random rand = new Random();

    @SideOnly(Side.CLIENT)
	public WeightedSoundPool netherWastes = new WeightedSoundPool();
    @SideOnly(Side.CLIENT)
	public WeightedSoundPool basaltDeltas = new WeightedSoundPool();
    @SideOnly(Side.CLIENT)
	public WeightedSoundPool crimsonForest = new WeightedSoundPool();
    @SideOnly(Side.CLIENT)
	public WeightedSoundPool warpedForest = new WeightedSoundPool();
    @SideOnly(Side.CLIENT)
	public WeightedSoundPool soulSandValley = new WeightedSoundPool();
    
    public WorldTickEventHandler() {
    	netherWastes.addEntry("ambient.nether_wastes.additions.w1", 9);
    	netherWastes.addEntry("ambient.nether_wastes.additions.w3", 3);
    	netherWastes.addEntry("ambient.nether_wastes.additions.w5", 15);
    	
    	basaltDeltas.addEntry("ambient.basalt_deltas.additions.w1", 4);
    	basaltDeltas.addEntry("ambient.basalt_deltas.additions.w10", 60);
    	basaltDeltas.addEntry("ambient.basalt_deltas.additions.w20", 160);
    	basaltDeltas.addEntry("ambient.basalt_deltas.additions.w25", 150);
    	basaltDeltas.addEntry("ambient.basalt_deltas.additions.w40", 240);

    	crimsonForest.addEntry("ambient.crimson_forest.additions.w2", 8);
    	crimsonForest.addEntry("ambient.crimson_forest.additions.w3", 6);
    	crimsonForest.addEntry("ambient.crimson_forest.additions.w4", 16);
    	crimsonForest.addEntry("ambient.crimson_forest.additions.w6", 18);
    	crimsonForest.addEntry("ambient.crimson_forest.additions.w35", 105);
    	
    	warpedForest.addEntry("ambient.warped_forest.additions.w1", 4);
    	warpedForest.addEntry("ambient.warped_forest.additions.w2", 4);
    	warpedForest.addEntry("ambient.warped_forest.additions.w3", 12);
    	warpedForest.addEntry("ambient.warped_forest.additions.w3_p0.8", 3);
    	warpedForest.addEntry("ambient.warped_forest.additions.w3_p0.7", 6);
    	warpedForest.addEntry("ambient.warped_forest.additions.w3_p0.1", 3);
    	warpedForest.addEntry("ambient.warped_forest.additions.w6", 12);
    	warpedForest.addEntry("ambient.warped_forest.additions.w10", 10);
    	warpedForest.addEntry("ambient.warped_forest.additions.w40", 120);

    	soulSandValley.addEntry("ambient.soul_sand_valley.additions.w2", 2);
    	soulSandValley.addEntry("ambient.soul_sand_valley.additions.w4", 4);
    	soulSandValley.addEntry("ambient.soul_sand_valley.additions.w5", 65);
    	soulSandValley.addEntry("ambient.soul_sand_valley.additions.w5_p0.7", 5);
    	soulSandValley.addEntry("ambient.soul_sand_valley.additions.w25", 150);
    	soulSandValley.addEntry("ambient.soul_sand_valley.additions.w25_p0.75", 100);
    }

    static {
        if (replacements == null) {
            replacements = new HashMap<Block, Block>();
            if (ConfigurationHandler.enableBrewingStands)
                replacements.put(Blocks.brewing_stand, ModBlocks.brewing_stand);
            if (ConfigurationHandler.enableColourfulBeacons)
                replacements.put(Blocks.beacon, ModBlocks.beacon);
            if (ConfigurationHandler.enableEnchants)
                replacements.put(Blocks.enchanting_table, ModBlocks.enchantment_table);
            if (ConfigurationHandler.enableInvertedDaylightSensor)
                replacements.put(Blocks.daylight_detector, ModBlocks.daylight_sensor);
        }
    }
    
    @SideOnly(Side.CLIENT)
    NetherAmbienceLoop ambience = null;
    @SideOnly(Side.CLIENT)
    int ticksToNextAmbience = rand.nextInt(80) + 1;
    @SideOnly(Side.CLIENT)
    String biomeName = null;
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(ClientTickEvent event)
    {
        World world = FMLClientHandler.instance().getClient().theWorld;
        EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
        if(!ConfigurationHandler.enableNewMiscSounds || world == null || event.phase != Phase.START || Minecraft.getMinecraft().isGamePaused()) {
        	return;
        }
        if(world.getTotalWorldTime() == 0) {
        	if(ambience != null && FMLClientHandler.instance().getClient().getSoundHandler().isSoundPlaying(ambience))
        		FMLClientHandler.instance().getClient().getSoundHandler().stopSound(ambience);
            ambience = null;
            biomeName = null;
            return;
        }

        if(player.dimension == -1 && ConfigurationHandler.enableNetherAmbience) {
        	Minecraft mc = FMLClientHandler.instance().getClient();
        	int x = MathHelper.floor_double(player.posX);
        	int y = MathHelper.floor_double(player.posY);
        	int z = MathHelper.floor_double(player.posZ);
        	biomeName = world.getChunkFromBlockCoords(x, z).getBiomeGenForWorldCoords(x & 15, z & 15, world.getWorldChunkManager()).biomeName;
        	if(getAmbienceLoopForBiome(biomeName) != null && !mc.getSoundHandler().isSoundPlaying(ambience)) {
        		Boolean flag = ambience == null || ambience.getVolume() <= 0;
            		ambience = new NetherAmbienceLoop(Reference.MOD_ID + ":ambient." + getAmbienceLoopForBiome(biomeName) + ".loop");
                	mc.getSoundHandler().playSound(ambience);
        		if(flag)
            		ambience.fadeIn();
        	} else if (biomeName == null || getAmbienceLoopForBiome(biomeName) == null || !ambience.getPositionedSoundLocation().getResourcePath().split("\\.")[1].equals(getAmbienceLoopForBiome(biomeName))) {
        		if(ambience != null)
            		ambience.stop();
        	} else if (mc.getSoundHandler().isSoundPlaying(ambience) && ambience.isStopping && ambience.getPositionedSoundLocation().getResourcePath().split("\\.")[1].equals(getAmbienceLoopForBiome(biomeName))){
        		ambience.isStopping = false;
        	}
            if(getAmbienceLoopForBiome(biomeName) != null && ambience != null && ticksToNextAmbience-- <= 0) {
            	Minecraft.getMinecraft().getSoundHandler().playSound(new NetherAmbience(Reference.MOD_ID + ":" + getAmbienceForBiome().getRandom(), 0.5F, 1));
            	ticksToNextAmbience = 40 + rand.nextInt(40) + 1;
            }
        }

        boolean tickchecktime = world.rand.nextInt(Math.toIntExact((world.getTotalWorldTime() % 10) + 1)) == 0;
        List<TileEntity> list = world.loadedTileEntityList;
        for(TileEntity tile : list) {
			if (tickchecktime && ConfigurationHandler.enableNewMiscSounds && tile.getBlockType() == Blocks.lit_furnace) {
	            int x = tile.xCoord;
	            int y = tile.yCoord;
	            int z = tile.zCoord;
				if(world.rand.nextDouble() < 0.1D)
					world.playSound(x + .5D, y + .5D, z + .5D,
							Reference.MOD_ID + ":block.furnace.fire_crackle", 1,
							(world.rand.nextFloat() * 0.1F) + 0.9F, false);
			}
        }
    }

    public static String getAmbienceLoopForBiome(String string) {
    	if(string.equals("Basalt Deltas")) {
    		return "basalt_deltas";
    	}
    	if(string.equals("Warped Forest")) {
    		return "warped_forest";
    	}
    	if(string.equals("Crimson Forest") || string.equals("Foxfire Swamp")) {
    		return "crimson_forest";
    	}
    	if(string.equals("Soul Sand Valley")) {
    		return "soul_sand_valley";
    	}
    	if(string.contentEquals("Abyssal Shadowland")) {
    		return null;
    	}
    	return "nether_wastes";
    }

    private WeightedSoundPool getAmbienceForBiome() {
    	String string = ambience.getPositionedSoundLocation().getResourcePath().split("\\.")[1];
    	if(string.equals("null"))
    		return null;
    	if(string.equals("basalt_deltas")) {
    		return basaltDeltas;
    	}
    	if(string.equals("warped_forest")) {
    		return warpedForest;
    	}
    	if(string.equals("crimson_forest")) {
    		return crimsonForest;
    	}
    	if(string.equals("soul_sand_valley")) {
    		return soulSandValley;
    	}
    	return netherWastes;
    }
    
    @SubscribeEvent
    public void tick(WorldTickEvent event) {
        if (event.side != Side.SERVER || event.phase != Phase.END)
            return;

        if (replacements.isEmpty() && !ConfigurationHandler.enableNewMiscSounds)
            return;

        World world = event.world;

        if(world.loadedTileEntityList.isEmpty())
        	return;

        List<TileEntity> list = world.loadedTileEntityList;
        for(TileEntity tile : list) {
            if (!replacements.isEmpty()) {
            	Block replacement = replacements.get(tile.getBlockType());
            	if(replacement != null && ((IConfigurable) replacement).isEnabled()) {
                    NBTTagCompound nbt = new NBTTagCompound();
                    tile.writeToNBT(nbt);
                    if (tile instanceof IInventory) {
                        IInventory invt = (IInventory) tile;
                        for (int j = 0; j < invt.getSizeInventory(); j++)
                            invt.setInventorySlotContents(j, null);
                    }
                    int x = tile.xCoord;
                    int y = tile.yCoord;
                    int z = tile.zCoord;
                    world.setBlock(x, y, z, replacement);
                    TileEntity newTile = world.getTileEntity(x, y, z);
                    newTile.readFromNBT(nbt);
                    break;
            	}
            }
        }
    }
}