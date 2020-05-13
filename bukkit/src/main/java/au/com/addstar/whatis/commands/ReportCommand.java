package au.com.addstar.whatis.commands;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.messaging.PluginMessageListenerRegistration;
import org.bukkit.scheduler.BukkitTask;

import au.com.addstar.whatis.EventHelper;
import au.com.addstar.whatis.WhatIs;
import au.com.addstar.whatis.EventHelper.EventCallback;
import au.com.addstar.whatis.util.CommandFinder;
import org.kitteh.pastegg.Paste;
import org.kitteh.pastegg.PasteBuilder;
import org.kitteh.pastegg.PasteContent;
import org.kitteh.pastegg.PasteFile;

public class ReportCommand implements ICommand {
    private File mParent;

    public ReportCommand(File parent) {
        mParent = parent;
    }

    @Override
    public String getName() {
        return "report";
    }

    @Override
    public String[] getAliases() {
        return null;
    }

    @Override
    public String getPermission() {
        return "whatis.report";
    }

    @Override
    public String getUsageString(String label, CommandSender sender) {
        return label;
    }

    @Override
    public String getDescription() {
        return "Creates a report inside the 'WhatIs/reports' directory";
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
        if (args.length > 1)
            return false;
        final PrintWriter writer;
        String arg;
        if(args.length == 1){
            arg = args[0];
        } else {
            arg = "report";
        }
        switch (arg.toLowerCase()) {
            case "paste":
                StringWriter out = new StringWriter();
                writer = createReport(out);
                writer.flush();
                PasteContent content  = new PasteContent(PasteContent.ContentType.TEXT,out.toString());
                PasteFile report  = new PasteFile("What Is Report",content);
                Bukkit.getScheduler().runTaskAsynchronously(WhatIs.instance, () -> {
                    PasteBuilder.PasteResult result = new PasteBuilder().addFile(report)
                          .name("What Is Report")
                          .expireIn(60*60*1000)
                          .build();
                    if (result.getPaste().isPresent()) {
                        Paste paste = result.getPaste().get();
                        String date = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(paste.getExpires());
                        WhatIs.instance.getLogger().info(
                              "Paste: "+paste.getId() +" / Deletion Key: " + paste.getDeletionKey() + " / Expires: "
                                    + date);
                        sender.sendMessage("Report pasted to https://paste.gg/" +paste.getId());
                        sender.sendMessage("Report deletion key: " + paste.getDeletionKey());
                        sender.sendMessage("Report Expiry: " + date);
                    }else{
                        sender.sendMessage("Error creating paste");
                    }
                });
                break;
            case "report":
            default:
                try {
                    File file = new File(mParent, new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(new Date()) + ".txt");
                    writer = createReport(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))));
                    writer.flush();
                    writer.close();
                    sender.sendMessage("Report saved to " + file.getPath());
                    return true;
                } catch (FileNotFoundException e) {
                    sender.sendMessage(ChatColor.RED + "An internal error occured");
                    e.printStackTrace();
                    return true;
                }
        }
        return true;
    }
    private PrintWriter createReport(Writer output){
        PrintWriter writer = new PrintWriter(output);

        // Write header
        writer.println("-------------------------------------------");
        writer.println();
        writer.println(" WhatIs Report");
        writer.println("  Date: " + DateFormat.getDateTimeInstance().format(new Date()));
        writer.println("  Server: " + Bukkit.getServer().getVersion());
        writer.println("  Bukkit: " + Bukkit.getServer().getBukkitVersion());
        writer.println("  Plugins: " + Bukkit.getPluginManager().getPlugins().length);
        writer.println();
        writer.println("-------------------------------------------");
        writer.println();

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {

            writer.println(" " + plugin.getName());
            writer.println("   Version: " + plugin.getDescription().getVersion());
            File source = WhatIs.getPluginSource(plugin);
            if (source != null)
                writer.println("   Source: " + source.getPath());

            // Events
            List<EventCallback> callbacks = EventHelper.getEventCallbacks(plugin);
            writer.println("   Event Handlers: " + callbacks.size());
            for (EventCallback callback : callbacks)
                writer.println("     - " + callback.toString());

            // Commands
            List<Command> commands = CommandFinder.getCommands(plugin);
            writer.println("   Commands: " + commands.size());
            for (Command command : commands) {
                writer.println("     - " + command.getName());
                if (!command.getDescription().isEmpty())
                    writer.println("       Description: " + command.getDescription());
                if (command.getPermission() != null)
                    writer.println("       Perm: " + command.getPermission());

                if (command.getAliases() != null && !command.getAliases().isEmpty()) {
                    StringBuilder aliases = new StringBuilder();
                    for (String alias : command.getAliases()) {
                        if (aliases.length() > 0)
                            aliases.append(", ");
                        aliases.append(alias);
                    }
                    writer.println("       Aliases: " + aliases);
                }
            }

            // Services
            List<RegisteredServiceProvider<?>> services = Bukkit.getServicesManager().getRegistrations(plugin);
            writer.println("   Services: " + services.size());
            for (RegisteredServiceProvider<?> service : services)
                writer.println("     - " + service.getService().getName() + "   Priority: " + service.getPriority().toString());

            // Tasks
            List<BukkitTask> tasks = WhatIs.getPluginTasks(plugin);
            writer.println("   Tasks: " + tasks.size());
            for (BukkitTask task : tasks) {
                String taskSource = WhatIs.getTaskSource(task);
                writer.println("     - Task " + task.getTaskId() + " sync:" + task.isSync() + " class:" + taskSource);
            }

            // Permissions
            List<Permission> perms = plugin.getDescription().getPermissions();
            writer.println("   Permissions (loadtime): " + perms.size());
            for (Permission perm : perms)
                writer.println("     - " + perm.getName() + "   Default: " + perm.getDefault());

            // Plugin channels
            Set<PluginMessageListenerRegistration> channels = Bukkit.getMessenger().getIncomingChannelRegistrations(plugin);
            writer.println("   Incoming Channels: " + channels.size());
            for (PluginMessageListenerRegistration channel : channels)
                writer.println("     - " + channel.getChannel() + ": " + channel.getListener().getClass().getName());

            Set<String> outChannels = Bukkit.getMessenger().getOutgoingChannels(plugin);
            writer.println("   Outgoing Channels: " + outChannels.size());
            for (String channel : outChannels)
                writer.println("     - " + channel);

            writer.println("===========================================");
        }
        return writer;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
        return null;
    }

}
