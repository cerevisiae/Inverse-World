package cherrytea.minecraft.inverseworld.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cherrytea.minecraft.inverseworld.util.PlayerMovementHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * @author cerevisiae
 *
 */
public class HangGlider extends Item 
{

    boolean gliding;
    
    double motionX = 1;
    double motionY = 1;
    double motionZ = 1;
    double motionYaw = 0;
    
    public HangGlider( int id ) {
        super( id );
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.tabMisc);
        setUnlocalizedName("flightGlider");
    }
    
    @SideOnly(Side.CLIENT)
    public void onUpdate(ItemStack item, World world, Entity player, int invSlot, boolean isHeld)
    {
        //Make sure we should even be checking
        //if ( !gliding )
        //    return;
        
        //Make sure they're holding it
        if ( !isHeld )
        {
            gliding = false;
            return;
        }
        
        //Make sure the player is off the ground
        //if ( !player.isAirBorne )
        //    return;

        
        Vec3 newMotion = PlayerMovementHelper.glide(player, 15);
        //System.out.println("MotionX = " + newMotion.xCoord + " MotionY = " + newMotion.yCoord + " MotionZ = " + newMotion.zCoord);
        
        player.motionX = newMotion.xCoord;
        player.motionY = newMotion.yCoord;
        player.motionZ = newMotion.zCoord;
    }
    
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player)
    {
        gliding = true;
        return item;
    }

}
