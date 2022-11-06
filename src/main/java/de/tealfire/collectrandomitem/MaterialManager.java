package de.tealfire.collectrandomitem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class MaterialManager {

    boolean won = false;
    public Material wantedMaterial;
    ArrayList<Material> materials = new ArrayList<Material>();
    ArrayList<Material> finishedMaterials = new ArrayList<Material>();
    ArrayList<Material> skippedMaterials = new ArrayList<Material>();

    public MaterialManager() {
        System.out.println("Loading materials...");
        for (Material material : Material.values()
        ) {
            if (material != null) {
                if (material.isItem() && !material.name().toLowerCase().contains("spawn_egg")) {
                    // System.out.println(material.name());
                    materials.add(material);
                }
            }
        }
        System.out.println("Loading save files...");
        loadFromFile();
        System.out.println("Updating wanted material...");
        if (wantedMaterial == null) {
            updateWantedMaterial();
        }

    }

    public Material getRandomMaterial() {
        return materials.get(new Random().nextInt(materials.size()));
    }

    public int countMaxMaterials() {
        return materials.size();
    }

    public int countClosedMaterials() {
        return finishedMaterials.size() + skippedMaterials.size();
    }

    public int countRemainingMaterials() {
        return countMaxMaterials() - countClosedMaterials();
    }

    public void broadcastMessage(String msg) {
        System.out.println(msg);
        for (Player p :
                Bukkit.getOnlinePlayers()
        ) {
            p.sendMessage(msg);
        }
    }

    public boolean scanInventoryForItem(PlayerInventory inv, Material material) {
        for (ItemStack i : inv.getContents()) {
            if (i != null) {
                if (i.getType() == wantedMaterial) {
                    return true;
                }
            }
        }
        return false;
    }

    public void won() {
        won = true;
        broadcastMessage("You finished all!");
    }

    public void finishWantedMaterial() {
        if (!won) {

            broadcastMessage(ChatColor.GREEN + wantedMaterial.toString().toLowerCase() + " finished");
            finishedMaterials.add(wantedMaterial);
            updateWantedMaterial();
        }

    }

    public void skipWantedMaterial() {
        if (!won) {
            broadcastMessage(ChatColor.GRAY + wantedMaterial.toString().toLowerCase() + " skipped");
            skippedMaterials.add(wantedMaterial);
            updateWantedMaterial();
        }

    }

    public void unskipLast() {
        if (skippedMaterials.size() > 0) {
            Material material = skippedMaterials.get(skippedMaterials.size() - 1);
            skippedMaterials.remove(skippedMaterials.size() - 1);
            setWantedMaterial(material);
        }
    }

    public void showSkipped() {
        for (Material material : skippedMaterials
        ) {
            broadcastMessage("" + ChatColor.BOLD + ChatColor.GRAY + "Skipped: " + material.toString().toLowerCase());
        }
    }

    public void showFinished() {
        for (Material material : finishedMaterials
        ) {
            broadcastMessage("" + ChatColor.BOLD + ChatColor.GREEN + "Finished: " + material.toString().toLowerCase());
        }
    }

    public void showClosed() {
        showSkipped();
        showFinished();
    }

    public void showRemaining() {
        for (Material material : materials
        ) {
            if ((finishedMaterials.contains(material) || skippedMaterials.contains(material))) {
                broadcastMessage("" + ChatColor.BOLD + ChatColor.YELLOW + "Remaining: " + material.toString().toLowerCase());
            }
        }
    }

    public void updateWantedMaterial() {
        if (countRemainingMaterials() <= 0) {
            won();
        } else {
            Material newMaterial = getRandomMaterial();

            while (finishedMaterials.contains(newMaterial) || skippedMaterials.contains(newMaterial)) {
                newMaterial = getRandomMaterial();
            }
            setWantedMaterial(newMaterial);

        }
    }

    public void setWantedMaterial(Material newMaterial) {
        wantedMaterial = newMaterial;
        broadcastMessage("[" + Integer.toString(countClosedMaterials()) + "/" + Integer.toString(countMaxMaterials()) + "]"
                + " -> " + ChatColor.GOLD + ChatColor.BOLD + newMaterial.toString());
    }


    public void loadFromFile() {
        System.out.println("Loading from files...");
        System.out.println("Loading file plugins/wantedItem.txt...");
        try {
            BufferedReader reader = new BufferedReader(new FileReader("plugins/wantedItem.txt"));
            setWantedMaterial(Material.getMaterial(reader.readLine()));
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loading file plugins/skippedItems.txt...");
        try {
            BufferedReader reader = new BufferedReader(new FileReader("plugins/skippedItems.txt"));
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                skippedMaterials.add(Material.getMaterial(line));
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Loading file plugins/finishedItems.txt...");
        try {
            BufferedReader reader = new BufferedReader(new FileReader("plugins/finishedItems.txt"));
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                finishedMaterials.add(Material.getMaterial(line));
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToFile() {
        System.out.println("Saving to files...");

        System.out.println("Saving wantedItem");
        try {
            PrintWriter writer = new PrintWriter("plugins/wantedItem.txt", "UTF-8");
            writer.println(wantedMaterial.name());
            writer.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        System.out.println("Saving skippedItems");
        try {
            PrintWriter writer = new PrintWriter("plugins/skippedItems.txt", "UTF-8");
            for (Material material : skippedMaterials
            ) {
                writer.println(material.name());

            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        System.out.println("Saving finishedItems");
        try {
            PrintWriter writer = new PrintWriter("plugins/finishedItems.txt", "UTF-8");
            for (Material material : finishedMaterials
            ) {
                writer.println(material.name());

            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
