package de.marvinleiers.griefdetection;

import com.google.gson.stream.JsonToken;
import de.marvinleiers.customconfig.CustomConfig;
import de.marvinleiers.griefdetection.commands.SnapshotCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.GregorianCalendar;

public final class GriefDetection extends JavaPlugin implements Listener
{
    private ArrayList<Player> suspected = new ArrayList<>();

    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);

        getCommand("snap").setExecutor(new SnapshotCommand());
    }

    public static GriefDetection getPlugin()
    {
        return getPlugin(GriefDetection.class);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (!event.getAction().toString().contains("BLOCK"))
            return;

        if (!event.hasItem())
            return;

        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (item.getType() == Material.LAVA_BUCKET)
        {
            suspected.add(player);
            takeInventoryScreenshot(player);

            for (Player all : Bukkit.getOnlinePlayers())
            {
                if (all.isOp())
                    all.sendMessage("§7§l" + player.getName() + " §c§lPLACED LAVA");
            }

            return;
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Block block = event.getBlock();

        System.out.println(block.getType());
    }

    private void takeInventoryScreenshot(Player player)
    {
        CustomConfig config = new CustomConfig(getDataFolder().getPath() + "/internal/" + player.getUniqueId().toString() + ".yml");
        Inventory inventory = player.getInventory();

        String[] inventorySnapshot = new String[inventory.getSize()];

        for (int i = 0; i < inventory.getSize(); i++)
        {
            ItemStack item = inventory.getContents()[i];

            if (item != null && item.getType() != Material.AIR)
            {
                try
                {
                    String encodedObject;
                    ByteArrayOutputStream io = new ByteArrayOutputStream();
                    BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
                    os.writeObject(item);
                    os.flush();

                    byte[] serializedObject = io.toByteArray();

                    encodedObject = new String(Base64.getEncoder().encode(serializedObject));

                    inventorySnapshot[i] = "[" + i + "] " + encodedObject;
                }
                catch (IOException e)
                {
                    System.out.println(e.getStackTrace());
                }
            }
        }

        config.set("inventory-snapshot", inventorySnapshot);

        System.out.println("Saved inventory snapshot of " + player.getName());
    }
}
