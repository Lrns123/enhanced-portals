package uk.co.shadeddimensions.enhancedportals.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import uk.co.shadeddimensions.enhancedportals.network.ClientProxy;
import uk.co.shadeddimensions.enhancedportals.network.CommonProxy;
import uk.co.shadeddimensions.enhancedportals.tileentity.TileEP;
import uk.co.shadeddimensions.enhancedportals.tileentity.TilePortalFrame;
import uk.co.shadeddimensions.enhancedportals.tileentity.TilePortalFrameController;
import uk.co.shadeddimensions.enhancedportals.tileentity.TilePortalFrameRedstone;
import uk.co.shadeddimensions.enhancedportals.util.ConnectedTextures;

public class BlockFrame extends BlockEP
{
    public static Icon controllerOverlay, redstoneOverlay, connectedToPortal;
    static ConnectedTextures connectedTextures;

    public BlockFrame(int id, String name)
    {
        super(id, Material.rock, true);
        setHardness(10);
        setResistance(2000);
        setUnlocalizedName(name);
        setStepSound(soundStoneFootstep);
        connectedTextures = new ConnectedTextures("enhancedportals:frame/portalFrame_%s", blockID, -1);
    }
    
    @Override
    public void registerIcons(IconRegister register)
    {
        controllerOverlay = register.registerIcon("enhancedportals:portalFrame_controller");
        redstoneOverlay = register.registerIcon("enhancedportals:portalFrame_redstone");
        connectedToPortal = register.registerIcon("enhancedportals:Portal_inside_DRAFT1");

        connectedTextures.registerIcons(register);
    }

    @Override
    public Icon getIcon(int side, int meta)
    {
        return connectedTextures.getNormalIcon();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void addCreativeItems(ArrayList list)
    {
        for (int i = 0; i < 3; i++)
        {
            list.add(new ItemStack(this, 1, i));
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void getSubBlocks(int par1, CreativeTabs creativeTab, List list)
    {
        for (int i = 0; i < 3; i++)
        {
            list.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public Icon getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
        TileEP frame = (TileEP) blockAccess.getBlockTileEntity(x, y, z);
        Icon frameIcon = frame != null ? frame.getTexture(side, ClientProxy.renderPass) : null;

        return frameIcon == null ? connectedTextures.getIconForFace(blockAccess, x, y, z, side) : frameIcon;
    }
    
    @Override
    public int getRenderType()
    {
        return ClientProxy.portalFrameRenderType;
    }
    
    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }
    
    @Override
    public boolean isOpaqueCube()
    {
        return true;
    }
    
    @Override
    public void breakBlock(World world, int x, int y, int z, int par5, int par6)
    {
        ((TilePortalFrame) world.getBlockTileEntity(x, y, z)).selfBroken();
        super.breakBlock(world, x, y, z, par5, par6);
    }

    @Override
    public TileEntity createNewTileEntity(World world)
    {
        return null;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
        if (metadata == 0)
        {
            return new TilePortalFrame();
        }
        else if (metadata == 1)
        {
            return new TilePortalFrameController();
        }
        else if (metadata == 2)
        {
            return new TilePortalFrameRedstone();
        }

        return null;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int id)
    {
        ((TilePortalFrame) world.getBlockTileEntity(x, y, z)).neighborChanged(id);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
    {
        return ((TilePortalFrame) world.getBlockTileEntity(x, y, z)).activate(player);
    }

    @Override
    public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side)
    {
        return true;
    }

    @Override
    public boolean isBlockNormalCube(World world, int x, int y, int z)
    {
        return false;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
        return ((TilePortalFrame) blockAccess.getBlockTileEntity(x, y, z)).isProvidingStrongPower(side);
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
        return ((TilePortalFrame) blockAccess.getBlockTileEntity(x, y, z)).isProvidingWeakPower(side);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random)
    {
        ((TilePortalFrame) world.getBlockTileEntity(x, y, z)).scheduledTick(random);
    }

    @Override
    public boolean canCreatureSpawn(EnumCreatureType type, World world, int x, int y, int z)
    {
        return false;
    }

    @Override
    public boolean canBeReplacedByLeaves(World world, int x, int y, int z)
    {
        return false;
    }

    @Override
    public int damageDropped(int par1)
    {
        return par1;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
    {
        return new ItemStack(CommonProxy.blockFrame.blockID, 1, world.getBlockMetadata(x, y, z));
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z)
    {
        if (world.isRemote)
        {
            return;
        }

        for (int i = 0; i < 6; i++)
        {
            ForgeDirection d = ForgeDirection.getOrientation(i);

            if (world.getBlockId(x + d.offsetX, y + d.offsetY, z + d.offsetZ) == blockID)
            {
                TilePortalFrame frame = (TilePortalFrame) world.getBlockTileEntity(x + d.offsetX, y + d.offsetY, z + d.offsetZ);

                if (frame.validateController())
                {
                    TilePortalFrameController controller = frame.getControllerValidated();
                    TilePortalFrame self = (TilePortalFrame) world.getBlockTileEntity(x, y, z);
                    self.controller = frame.controller;

                    controller.portalFrame.add(new ChunkCoordinates(x, y, z));

                    if (self instanceof TilePortalFrameRedstone)
                    {
                        controller.portalFrameRedstone.add(new ChunkCoordinates(x, y, z));
                    }

                    CommonProxy.sendUpdatePacketToAllAround(controller);
                    return;
                }
            }
        }
    }
}
