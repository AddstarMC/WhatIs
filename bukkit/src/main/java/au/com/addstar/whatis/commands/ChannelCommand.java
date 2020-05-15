package au.com.addstar.whatis.commands;

import au.com.addstar.whatis.WhatIs;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.messaging.PluginMessageListenerRegistration;

import java.util.List;

/**
 * Created for the AddstarMC Project. Created by Narimm on 10/10/2018.
 */
public class ChannelCommand implements ICommand {
    @Override
    public String getName() {
        return "channels";
    }
    
    @Override
    public String[] getAliases() {
        return null;
    }
    
    @Override
    public String getPermission() {
        return "whatis.channels";
    }
    
    @Override
    public String getUsageString(String label, CommandSender sender) {
        return "/whatis channels";
    }
    
    @Override
    public String getDescription() {
        return "List the registered plugin channels";
    }
    
    @Override
    public boolean canBeConsole() {
        return true;
    }
    
    @Override
    public boolean canBeCommandBlock() {
        return false;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        sender.sendMessage(" List of Outgoing Channels");
        sender.sendMessage(" -------------------------");
    
        for (String name : Bukkit.getMessenger().getOutgoingChannels()){
            sender.sendMessage(" - " + name);
        }
        sender.sendMessage(" -------------------------");
        sender.sendMessage(" List of Incoming Channels");
        sender.sendMessage(" -------------------------");
        for (String name : Bukkit.getMessenger().getIncomingChannels()) {
            sender.sendMessage(" - " + name);
            for(PluginMessageListenerRegistration reg:
                    Bukkit.getMessenger().getIncomingChannelRegistrations(name)){
                sender.sendMessage("       Plugin - " + reg.getPlugin().getName());
            }
        }
        sender.sendMessage(" -------------------------");
        return false;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
        return null;
    }
}
