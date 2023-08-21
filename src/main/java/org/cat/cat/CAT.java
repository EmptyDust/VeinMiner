package org.cat.cat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CAT extends JavaPlugin implements Listener {
    PluginManager pluginManager = this.getServer().getPluginManager();
    @Override
    public void onEnable() {
        MineListener.oreList();
        MineListener.cropList();
        // Plugin startup logic
        Bukkit.getConsoleSender().sendMessage("Hello World!");
        //注册
        pluginManager.registerEvents(new MineListener(),this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getConsoleSender().sendMessage("Bye!See you next time!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("chainmine")&&(sender instanceof Player p)){
            UUID uuid = p.getUniqueId();
            String usage = "/chainmine (open|close|sneak)";
            if (args.length==1){
                String commend = args[0];
                if (commend.equalsIgnoreCase("open")){
                    MineListener.addOpenedPlayerUUID(uuid);
                    sender.sendMessage("Chain mine has been opened!Sneak to trigger chain mining!");
                }else if (commend.equalsIgnoreCase("close")){
                    MineListener.removeOpenedPlayerUUID(uuid);
                    sender.sendMessage("Chain mine has been closed!");
                }else if (commend.equalsIgnoreCase("sneak")){
                    if (!MineListener.containOpenedPlayerUUID(uuid)){
                        sender.sendMessage("You haven't opened chain mine!\n"+usage);
                    }else if (MineListener.containSneakPlayerUUID(uuid)){
                        MineListener.removeSneakPlayerUUID(uuid);
                        sender.sendMessage("Now you don't need to sneak while chain mining!");
                    }else {
                        MineListener.addSneakPlayerUUID(uuid);
                        sender.sendMessage("Now you need to sneak while chain mining!");
                    }
                }else {
                    sender.sendMessage(usage);
                }
            }else {
                sender.sendMessage(usage);
            }
        }else{
            sender.sendMessage("You must be a player to use this command.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("chainmine")&&(sender instanceof Player p)){
            if (args.length == 1){
                String arg1 = args[0].toLowerCase();
                if (isChildString(arg1,"open"))
                    completions.add("open");
                if (isChildString(arg1,"close"))
                    completions.add("close");
                if (isChildString(arg1,"sneak")){
                    completions.add("sneak");
                }
                if (args[0].isEmpty()){
                    completions.add("open");
                    completions.add("close");
                    completions.add("sneak");
                }
            }
        }

        return completions;
    }

    private boolean isChildString(String child, String parent) {
        if (child.length()>=parent.length()){
            return false;
        }else {
            char[] childA = child.toCharArray();
            char[] parentA = parent.toCharArray();
            for (int i = 0;i<child.length();i++){
                if (childA[i]!=parentA[i]){
                    return false;
                }
            }
        }
        return true;
    }
}

