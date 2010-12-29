import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URL;


public class BlockData {
	private static Image[] blockImages;
	private static String[] names = {"Air", "Rock", "Grass", "Dirt", "Cobblestone", "Wood", "Sapling", "Adminium", "Water", "Still water", "Lava", "Still lava", "Sand", "Gravel", "Gold ore", "Iron ore", "Coal ore", "Tree", "Leaves", "Sponge", "Glass", "Cloth", "Flower", "Rose", "Brown mushroom", "Red mushroom", "Gold block", "Iron block", "Double stone slab", "Stone slab", "Brick", "TNT", "Bookshelf", "Mossy cobblestone", "Obsidian", "Torch", "Fire", "Mob spawner", "Wood stairs", "Chest", "Redstone dust", "Diamond ore", "Diamond", "Workbench", "Crop", "Soil", "Furnace", "Lit furnace", "Sign block", "Wood door block", "Ladder", "Rails", "Cobblestone stairs", "Wall sign", "Lever", "Rock pressure plate", "Iron door block", "Wood pressure plate", "Redstone ore ", "Lit redstone ore", "Redstone torch", "Lit redstone torch", "Button", "Snow", "Ice", "Snow block", "Cactus", "Clay block", "Reed block", "Jukebox", "Fence", "Pumpkin", "Netherstone", "Slow sand", "Lightstone", "Portal", "Jack-o'-lantern"};
	private static String[] IDs = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "10", "11", "12", "13", "14", "23", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B", "3C", "3D", "3E", "3F", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4A", "4B", "4C", "4D", "4E", "4F", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5A", "5B"};
	
	public static int toDec(String id){
		return Integer.parseInt(id, 16);
	}
	
	public static String toHex (int i){
		return Integer.toString(i, 16);
	}
	
	public static Image getImage(String id){
		for (int i = 0 ; i < IDs.length ; i++){
			if (IDs[i].equals(id)){
				return blockImages[i];
			}
		}
		return null;
	}
	
	public static String getName (String id){
		for (int i = 0 ; i < IDs.length ; i++){
			if (IDs[i].equals(id)){
				return names[i];
			}
		}
		return null;
	}
	
	public static void initImages(){
		blockImages = new Image[IDs.length];
		for (int i = 0 ; i < IDs.length ; i++){
			try{
				Toolkit tk = Toolkit.getDefaultToolkit();
				URL url = BlockData.class.getResource("rsc/" + IDs[i] + ".png");
				Image img = tk.createImage(url);
				tk.prepareImage(img, -1, -1, null);
				blockImages[i] = img;
			}
			catch (Exception e) {
				//not found/other error, generate stand-in
				blockImages[i] = (Image) new BufferedImage (16, 16, BufferedImage.TYPE_INT_RGB);
			}
		}
	}
}
