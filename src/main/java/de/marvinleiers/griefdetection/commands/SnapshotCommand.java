package de.marvinleiers.griefdetection.commands;

import de.marvinleiers.customconfig.CustomConfig;
import de.marvinleiers.griefdetection.GriefDetection;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SnapshotCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage("§cNur für Spieler!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1)
        {
            player.sendMessage("§cUsage: /" + label + " <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        File file = new File(GriefDetection.getPlugin().getDataFolder().getPath() + "/internal/" + target.getUniqueId().toString() + ".yml");

        if (!file.exists())
        {
            player.sendMessage("§eFür " + target.getName() + " liegt bisher kein Inventar-Snapshot vor!");
            return true;
        }

        CustomConfig config = new CustomConfig(GriefDetection.getPlugin().getDataFolder().getPath() + "/internal/" + target.getUniqueId().toString() + ".yml");

        if (!config.isSet("inventory-snapshot"))
        {
            player.sendMessage("§eFür " + target.getName() + " liegt bisher kein Inventar-Snapshot vor!");
            return true;
        }

        List<String> inventorySnapshot = config.getConfig().getStringList("inventory-snapshot");
        Inventory inventory = Bukkit.createInventory(null, 27, target.getName() + "'s Inventar");

        for (String str : inventorySnapshot)
        {
            int slot =  Integer.parseInt(str.split(" ")[0].replace("[", "").replace("]", ""));
            String serializedItem = str.split(" ")[1];

            try
            {
                byte[] serializedObject = Base64.getDecoder().decode(serializedItem);

                ByteArrayInputStream in = new ByteArrayInputStream(serializedObject);
                BukkitObjectInputStream is = new BukkitObjectInputStream(in);

                ItemStack newItem = (ItemStack) is.readObject();

                inventory.setItem(slot, newItem);
            }
            catch (IOException | ClassNotFoundException e)
            {
                e.printStackTrace();
            }

            player.openInventory(inventory);
        }

        return true;
    }
}
