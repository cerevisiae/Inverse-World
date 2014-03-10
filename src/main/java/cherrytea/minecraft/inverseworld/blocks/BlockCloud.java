package cherrytea.minecraft.inverseworld.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockCloud extends InvBlock {

    public BlockCloud(int id) {
        super(id, Material.air, 0.0F, "cloud");
        
        this.setStepSound(Block.soundClothFootstep);
        this.setUnlocalizedName("cloud");
    }

}
