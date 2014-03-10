package cherrytea.minecraft.inverseworld.blocks;

import java.awt.Color;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.Icon;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cherrytea.minecraft.inverseworld.InverseWorld;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockLuminousGrass extends InvBlock {
    
    @SideOnly(Side.CLIENT)
    private Icon iconGrassTop;
    @SideOnly(Side.CLIENT)
    private Icon iconSnowSide;
    @SideOnly(Side.CLIENT)
    private Icon iconGrassSideOverlay;
    
    /**
     * Construct Luminous Grass, set values for 
     * @param id
     */
    public BlockLuminousGrass(int id) {
        super(id, Material.grass, 0.6F, "grass");
        
        this.setLightValue(0.7F);
        this.setStepSound(Block.soundGrassFootstep);
        this.setUnlocalizedName("grassLuminous");
        this.setTickRandomly(true);
        
        this.setCreativeTab(CreativeTabs.tabBlock);
    }
    
    /**
     * Ticks the block if it's been scheduled
     */
    public void updateTick(World world, int X, int Y, int Z, Random RNG)
    {
        if (!world.isRemote)
        {
            if (world.getBlockLightOpacity(X, Y + 1, Z) > 2)
            {
                world.setBlock(X, Y, Z, Block.dirt.blockID);
            }
            else if (world.getBlockLightValue(X, Y + 1, Z) >= 9)
            {
                for (int l = 0; l < 4; ++l)
                {
                    int randX = X + RNG.nextInt(3) - 1;
                    int randY = Y + RNG.nextInt(5) - 3;
                    int randZ = Z + RNG.nextInt(3) - 1;

                    if (world.getBlockId(randX, randY, randZ) == Block.dirt.blockID && world.getBlockLightOpacity(randX, randY + 1, randZ) <= 2)
                    {
                        world.setBlock(randX, randY, randZ, InverseBlocks.luminousGrass.blockID);
                    }
                }
            }
        }
    }


    @SideOnly(Side.CLIENT)

    /**
     * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
     */
    public Icon getIcon(int par1, int par2)
    {
        return par1 == 1 ? this.iconGrassTop : (par1 == 0 ? Block.dirt.getBlockTextureFromSide(par1) : this.blockIcon);
    }
    

    /**
     * Returns the ID of the items to drop on destruction.
     */
    public int idDropped(int par1, Random RNG, int par3)
    {
        return Block.dirt.idDropped(0, RNG, par3);
    }

    @SideOnly(Side.CLIENT)

    /**
     * Retrieves the block texture to use based on the display side. Args: iBlockAccess, x, y, z, side
     */
    public Icon getBlockTexture(IBlockAccess par1IBlockAccess, int x, int y, int z, int side)
    {
        if (side == 1)
        {
            return this.iconGrassTop;
        }
        else if (side == 0)
        {
            return Block.dirt.getBlockTextureFromSide(side);
        }
        else
        {
            Material material = par1IBlockAccess.getBlockMaterial(x, y + 1, z);
            return material != Material.snow && material != Material.craftedSnow ? this.blockIcon : this.iconSnowSide;
        }
    }

    @SideOnly(Side.CLIENT)

    /**
     * When this method is called, your block should register all the icons it needs with the given IconRegister. This
     * is the only chance you get to register icons.
     */
    public void registerIcons(IconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon(this.getTextureName() + "_side");
        this.iconGrassTop = par1IconRegister.registerIcon(this.getTextureName() + "_top");
        this.iconSnowSide = par1IconRegister.registerIcon(this.getTextureName() + "_side_snowed");
        this.iconGrassSideOverlay = par1IconRegister.registerIcon(this.getTextureName() + "_side_overlay");
    }

    @SideOnly(Side.CLIENT)
    public int getBlockColor()
    {
        double d0 = 0.5D;
        double d1 = 1.0D;
        return ColorizerGrass.getGrassColor(d0, d1);
    }

    @SideOnly(Side.CLIENT)

    /**
     * Returns the color this block should be rendered. Used by leaves.
     * 
     * @param par1
     */
    public int getRenderColor(int par1)
    {
        if (par1 == 0)
            return this.getBlockColor();
        else
            return 16777215;
    }

    @SideOnly(Side.CLIENT)

    /**
     * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color. Note only called
     * when first determining what to render.
     * 
     * @param par1IBlockAccess
     * 
     * @param x
     * 
     * @param y
     * 
     * @param z
     */
    public int colorMultiplier(IBlockAccess par1IBlockAccess, int x, int y, int z)
    {
        int r = 0;
        int g = 0;
        int b = 0;

        //Average the surrounding blocks to get a smooth transition
        for (int zOffset = -1; zOffset <= 1; ++zOffset)
        {
            for (int xOffset = -1; xOffset <= 1; ++xOffset)
            {
                int biomeColor = par1IBlockAccess.getBiomeGenForCoords(x + xOffset, z + zOffset).getBiomeGrassColor();
                r += (biomeColor & 16711680) >> 16;
                g += (biomeColor & 65280) >> 8;
                b += biomeColor & 255;
            }
        }

         // Convert color to HSB.
         float[] hsbVals = new float[3];
         Color.RGBtoHSB(r/9, g/9, b/9, hsbVals);
        
        //Return the final hue shifted color
        return Color.HSBtoRGB(0.5F, hsbVals[1] + 0.1F, hsbVals[2] + 0.2F);
    }

    //@SideOnly(Side.CLIENT)
    //public static Icon getIconSideOverlay()
    //{
    //    return InverseBlocks.luminousGrass.iconGrassSideOverlay;
    //}
    
}
