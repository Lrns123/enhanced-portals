package uk.co.shadeddimensions.ep3.tileentity.portal;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import uk.co.shadeddimensions.ep3.EnhancedPortals;
import uk.co.shadeddimensions.ep3.api.IPortalModule;
import uk.co.shadeddimensions.ep3.client.particle.PortalFX;
import uk.co.shadeddimensions.ep3.network.GuiHandler;
import uk.co.shadeddimensions.ep3.network.packet.PacketTileUpdate;
import uk.co.shadeddimensions.library.util.ItemHelper;
import cpw.mods.fml.common.network.ByteBufUtils;

public class TileModuleManipulator extends TileFrame implements IInventory
{
    ItemStack[] inventory = new ItemStack[9];
    
    @Override
    public boolean activate(EntityPlayer player, ItemStack stack)
    {
    	if (player.isSneaking())
		{
			return false;
		}
    	
        TileController controller = getPortalController();

        if (stack != null && controller != null && controller.isFinalized())
        {
            if (ItemHelper.isWrench(stack) && !player.isSneaking())
            {
                GuiHandler.openGui(player, this, GuiHandler.MODULE_MANIPULATOR);
                return true;
            }
            else if (ItemHelper.isPaintbrush(stack))
            {
                GuiHandler.openGui(player, controller, GuiHandler.TEXTURE_FRAME);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canUpdate()
    {
        return true;
    }

    @Override
    public ItemStack decrStackSize(int i, int j)
    {
        ItemStack stack = getStackInSlot(i);

        if (stack != null)
        {
            if (stack.stackSize <= j)
            {
                setInventorySlotContents(i, null);
            }
            else
            {
                stack = stack.splitStack(j);

                if (stack.stackSize == 0)
                {
                    setInventorySlotContents(i, null);
                }
            }
        }

        return stack;
    }

    @Override
    public void packetFill(ByteBuf buffer)
    {
        super.packetFill(buffer);

        for (int i = 0; i < getSizeInventory(); i++)
        {
            ByteBufUtils.writeItemStack(buffer, getStackInSlot(i));
        }
    }

    public IPortalModule[] getInstalledUpgrades()
    {
        IPortalModule[] modules = new IPortalModule[getSizeInventory()];

        for (int i = 0; i < getSizeInventory(); i++)
        {
            ItemStack s = getStackInSlot(i);

            if (s != null)
            {
                modules[i] = (IPortalModule) s.getItem();
            }
        }

        return modules;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    @Override
    public String getInventoryName()
    {
        return "tile.ep3.portalFrame.upgrade.name";
    }

    @Override
    public int getSizeInventory()
    {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int i)
    {
        return inventory[i];
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i)
    {
        return inventory[i];
    }

    public ArrayList<ItemStack> getModules()
    {
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();

        for (ItemStack i : inventory)
        {
            if (i != null && i.getItem() instanceof IPortalModule)
            {
                list.add(i);
            }
        }

        return list;
    }

    public ItemStack getModifierItem()
    {
        return inventory[9];
    }

    public boolean hasModule(String ID)
    {
        for (ItemStack i : getModules())
        {
            if (((IPortalModule) i.getItem()).getID(i).equals(ID))
            {
                return true;
            }
        }

        return false;
    }

    public boolean installUpgrade(ItemStack stack)
    {
        if (stack == null || !(stack.getItem() instanceof IPortalModule))
        {
            return false;
        }

        IPortalModule pModule = (IPortalModule) stack.getItem();

        if (!hasModule(pModule.getID(stack)) && pModule.canInstallUpgrade(this, getInstalledUpgrades(), stack))
        {
            for (int i = 0; i < getSizeInventory(); i++)
            {
                if (getStackInSlot(i) == null)
                {
                    ItemStack s = stack.copy();
                    s.stackSize = 1;

                    setInventorySlotContents(i, s);
                    EnhancedPortals.packetPipeline.sendToAllAround(new PacketTileUpdate(this), this);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack)
    {
        return stack != null && (stack.getItem() instanceof IPortalModule && !hasModule(((IPortalModule) stack.getItem()).getID(stack)));
    }

    public boolean isPortalInvisible()
    {
        for (ItemStack i : getModules())
        {
            if (((IPortalModule) i.getItem()).disablePortalRendering(this, i))
            {
                return true;
            }
        }

        return false;
    }

    public void onEntityTeleported(Entity entity)
    {
        for (ItemStack i : getModules())
        {
            ((IPortalModule) i.getItem()).onEntityTeleportEnd(entity, this, i);
        }
    }

    public void particleCreated(PortalFX portalFX)
    {
        for (ItemStack i : getModules())
        {
            ((IPortalModule) i.getItem()).onParticleCreated(this, i, portalFX);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);

        NBTTagList list = tagCompound.getTagList("Inventory", 9);
        for (int i = 0; i < list.tagCount(); i++)
        {
           // inventory[i] = ItemStack.loadItemStackFromNBT((NBTTagCompound) list.tagAt(i));
        }
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack)
    {
        inventory[i] = itemstack;
    }

    public boolean shouldKeepMomentumOnTeleport()
    {
        for (ItemStack i : getModules())
        {
            if (((IPortalModule) i.getItem()).keepMomentumOnTeleport(this, i))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void packetUse(ByteBuf buffer)
    {
        super.packetUse(buffer);

        for (int i = 0; i < getSizeInventory(); i++)
        {
            setInventorySlotContents(i, ByteBufUtils.readItemStack(buffer));
        }

        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);

        NBTTagList list = new NBTTagList();
        for (ItemStack s : inventory)
        {
            if (s != null)
            {
                NBTTagCompound compound = new NBTTagCompound();
                s.writeToNBT(compound);
                list.appendTag(compound);
            }
        }

        tagCompound.setTag("Inventory", list);
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        return true;
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return false;
    }

    @Override
    public void openInventory()
    {
        
    }

    @Override
    public void closeInventory()
    {
        
    }
}