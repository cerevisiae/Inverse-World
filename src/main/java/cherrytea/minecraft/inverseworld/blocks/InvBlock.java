package cherrytea.minecraft.inverseworld.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.StepSound;
import net.minecraft.block.material.Material;

public class InvBlock extends Block {

	public InvBlock(int ID, Material blockMaterial, float blockHardness, String texture) {
		super(ID, blockMaterial);
		
		this.setHardness(blockHardness);
		this.setTextureName(texture);
	}

}
