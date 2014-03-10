package cherrytea.minecraft.inverseworld.items;

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.item.Item;

public class InverseItems {

	public static final Item hangGlider = new HangGlider(5000);
	public static void init ( )
	{
		registerItems ( );
	}
	
	private static void registerItems ( )
	{
		LanguageRegistry.addName(hangGlider, "Glider");
	}
	
}
