package au.com.addstar.whatis.commands;

import au.com.addstar.whatis.utils.BadArgumentException;
import au.com.addstar.whatis.utils.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Created for the AddstarMC Project. Created by Narimm on 10/10/2018.
 */
public class ChannelCommand implements SubCommand {
    @Override
    public String getName() {
        return "channels";
    }
    
    @Override
    public String getDescription() {
        return "Shows the registered channels";
    }
    
    @Override
    public String[] getAliases() {
        return null;
    }
    
    @Override
    public String getUsage() {
        return "";
    }
    
    @Override
    public String getPermission() {
        return null;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, String[] args) throws BadArgumentException {
        sender.sendMessage(TextComponent.fromLegacyText(" Registered Proxy Channels: "));
        sender.sendMessage(TextComponent.fromLegacyText(" -------------------------- "));
        for (String name : ProxyServer.getInstance().getChannels()) {
            sender.sendMessage(TextComponent.fromLegacyText("   "+name));
        };
        sender.sendMessage(TextComponent.fromLegacyText(" -------------------------- "));
        return true;
    }
    
    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }
}
