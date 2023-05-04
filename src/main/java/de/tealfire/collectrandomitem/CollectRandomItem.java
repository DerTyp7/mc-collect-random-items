package de.tealfire.collectrandomitem;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class CollectRandomItem extends JavaPlugin implements Listener {
    private MaterialManager materialManager;

    private long timerTicks = 0L; // Ticks for how long a player needed
    private boolean isRunning = false; // Is the currently running?
    private boolean isPaused = false; // Is the game currently paused? (isRunning should be true if isPaused is true)

    private void start() {
        if (!isRunning) {
            materialManager = new MaterialManager();
            isRunning = true;
            loadTimerFromFile();
            getLogger().info("Started!");
        }
    }

    private void restart() {
        if (isRunning) {
            stop();
            start();
            resume();
        }
    }

    private void stop() {
        if (isRunning) {
            materialManager = null;
            File skippedItemsFile = new File("plugins/skippedItems.txt");
            File finishedItemsFile = new File("plugins/finishedItems.txt");
            File timerFile = new File("plugins/timer.txt");

            skippedItemsFile.delete();
            finishedItemsFile.delete();
            timerFile.delete();

            isRunning = false;
            getLogger().info("Stopped!");
        }

    }

    private void pause() {
        if (isRunning) {
            isPaused = true;
            getLogger().info("Paused!");
        }
    }

    private void resume() {
        if (isPaused) {
            isPaused = false;
            getLogger().info("Resumed!");
        }
    }

    private String getTimerString() {
        long seconds = (long) (timerTicks) / 20L;
        SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss");
        sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date timerDate = new Date(seconds * 1000);
        return sdfDate.format(timerDate);
    }

    private void saveTimerToFile() {
        System.out.println("Saving timer to file...");
        try {
            PrintWriter writer = new PrintWriter("plugins/timer.txt", "UTF-8");
            writer.println(Long.toString(timerTicks));
            writer.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    private void saveTimerStringToFile() {
        try {
            PrintWriter writer = new PrintWriter("plugins/timerString.txt", "UTF-8");
            writer.println(getTimerString());
            writer.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    private void loadTimerFromFile() {
        System.out.println("Loading file plugins/timer.txt...");
        try {
            BufferedReader reader = new BufferedReader(new FileReader("plugins/timer.txt"));
            timerTicks = Long.parseLong(reader.readLine());
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            timerTicks = 0L;
        }
    }

    @Override
    public void onEnable() {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        getServer().getPluginManager().registerEvents(this, this);

        File skippedItemsFile = new File("plugins/skippedItems.txt");
        File finishedItemsFile = new File("plugins/finishedItems.txt");
        if ((skippedItemsFile.exists() && !skippedItemsFile.isDirectory()) || (finishedItemsFile.exists() && !finishedItemsFile.isDirectory())) {
            start();
            pause();
        }

        long updateInterval = 3L;

        scheduler.runTaskTimer(this, () -> {
            if (isRunning) {
                if (!isPaused) {
                    timerTicks += updateInterval;
                    saveTimerStringToFile();
                    for (Player player :
                            Bukkit.getOnlinePlayers()) {
                        if (player != null) {
                            if (materialManager.scanInventoryForItem(player.getInventory(), materialManager.wantedMaterial)) {
                                materialManager.finishWantedMaterial();
                            }
                        }
                    }
                    if (Bukkit.getOnlinePlayers().size() == 0) {
                        pause();
                    }
                }


                if (materialManager != null) {
                    String msg = "";
                    if (isPaused) {
                        msg = "" + ChatColor.BOLD + ChatColor.GRAY + "[PAUSED] " + "[" + Integer.toString(materialManager.countClosedMaterials()) + "/"
                                + Integer.toString(materialManager.countMaxMaterials()) + "] " + ChatColor.GOLD + materialManager.wantedMaterial.toString().toLowerCase();
                    } else {
                        msg = "" + ChatColor.BOLD + ChatColor.GRAY + "[" + Integer.toString(materialManager.countClosedMaterials()) + "/"
                                + Integer.toString(materialManager.countMaxMaterials()) + "] " + ChatColor.GOLD + materialManager.wantedMaterial.toString().toLowerCase();
                    }

                    for (Audience a : Bukkit.getServer().audiences()
                    ) {
                        a.sendActionBar(Component.text(msg));
                    }
                }
            }
        }, updateInterval /*<-- the initial delay */, updateInterval /*<-- the interval */);

        Bukkit.getLogger().info("CollectRandomItems started!");
    }

    @Override
    public void onDisable() {
        if (materialManager != null) {
            materialManager.saveToFile();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (command.getName().equalsIgnoreCase("skip")) {
            if (isRunning) {
                if (args.length > 0) {
                    for (int i = 0; i <= Integer.parseInt(args[0]); i++) {
                        materialManager.skipWantedMaterial();
                    }
                } else {
                    materialManager.skipWantedMaterial();
                }
            }

        } else if (command.getName().equalsIgnoreCase("unskip")) {
            if (isRunning) {
                if (args.length > 0) {
                    for (int i = 0; i <= Integer.parseInt(args[0]); i++) {
                        materialManager.unskipLast();
                    }
                } else {
                    materialManager.unskipLast();
                }
            }
        } else if (command.getName().equalsIgnoreCase("itemsstop")) {
            stop();
        } else if (command.getName().equalsIgnoreCase("pause")) {
            pause();
        } else if (command.getName().equalsIgnoreCase("resume")) {
            resume();
        } else if (command.getName().equalsIgnoreCase("itemsrestart")) {
            restart();
        } else if (command.getName().equalsIgnoreCase("itemsstart")) {
            start();
        } else if (command.getName().equalsIgnoreCase("reroll")) {
            if (isRunning) {
                materialManager.updateWantedMaterial();
            }
        } else if (command.getName().equalsIgnoreCase("finishedItems")) {
            materialManager.showFinished();
        } else if (command.getName().equalsIgnoreCase("skippedItems")) {
            materialManager.showSkipped();
        } else if (command.getName().equalsIgnoreCase("closedItems")) {
            materialManager.showClosed();
        } else if (command.getName().equalsIgnoreCase("remainingItems")) {
            materialManager.showRemaining();
        } else if (command.getName().equalsIgnoreCase("itemsSave")) {
            saveTimerToFile();
            materialManager.saveToFile();
        } else if (command.getName().equalsIgnoreCase("timer")) {
            sender.sendMessage("Timer -> " + ChatColor.BOLD + ChatColor.YELLOW + "[" + getTimerString() + "]");
        } else if (command.getName().equalsIgnoreCase("timerReset")) {
            timerTicks = 0L;
        }
        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (isRunning) {
            event.getPlayer().sendMessage("Current Item -> " + materialManager.wantedMaterial.toString());
        }
    }
}
