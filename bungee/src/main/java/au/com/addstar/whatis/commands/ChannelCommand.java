package au.com.addstar.whatis.commands;

import au.com.addstar.whatis.utils.BadArgumentException;
import au.com.addstar.whatis.utils.SubCommand;
import net.md_5.bungee.api.CommandSender;

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
        return null;
    }
    
    @Override
    public String[] getAliases() {
        return new String[0];
    }
    
    @Override
    public String getUsage() {
        return null;
    }
    
    @Override
    public String getPermission() {
        return null;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, String[] args) throws BadArgumentException {
        return false;
    }
    
    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }
}
