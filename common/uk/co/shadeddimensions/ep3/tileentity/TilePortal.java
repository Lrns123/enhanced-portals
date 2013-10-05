package uk.co.shadeddimensions.ep3.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import uk.co.shadeddimensions.ep3.network.packet.MainPacket;
import uk.co.shadeddimensions.ep3.network.packet.PacketRequestData;
import uk.co.shadeddimensions.ep3.tileentity.frame.TilePortalController;
import uk.co.shadeddimensions.ep3.util.ChunkCoordinateUtils;
import cpw.mods.fml.common.network.PacketDispatcher;

public class TilePortal extends TileEP implements IInventory
{
    public ChunkCoordinates controller;

    public TilePortal()
    {

    }

    public boolean activate(EntityPlayer player)
    {
        return false;
    }

    @Override
    public void closeChest()
    {
    }

    @Override
    public ItemStack decrStackSize(int i, int j)
    {
        return null;
    }

    public TilePortalController getController()
    {
        if (controller != null)
        {
            TileEntity tile = worldObj.getBlockTileEntity(controller.posX, controller.posY, controller.posZ);

            if (tile != null && tile instanceof TilePortalController)
            {
                return (TilePortalController) tile;
            }
        }

        return null;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 0;
    }

    @Override
    public String getInvName()
    {
        return null;
    }

    @Override
    public int getSizeInventory()
    {
        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int i)
    {
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i)
    {
        return null;
    }

    @Override
    public boolean isInvNameLocalized()
    {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return false;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        return true;
    }

    @Override
    public void openChest()
    {
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);

        controller = ChunkCoordinateUtils.loadChunkCoord(tagCompound, "controller");
    }

    public void selfBroken()
    {

    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack)
    {
    }

    @Override
    public void validate()
    {
        super.validate();

        if (worldObj.isRemote)
        {
            PacketDispatcher.sendPacketToServer(MainPacket.makePacket(new PacketRequestData(this)));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);

        ChunkCoordinateUtils.saveChunkCoord(tagCompound, controller, "controller");
    }
}