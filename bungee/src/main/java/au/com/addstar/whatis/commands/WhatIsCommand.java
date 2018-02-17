package au.com.addstar.whatis.commands;

import au.com.addstar.whatis.utils.CommandDispatcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class WhatIsCommand extends Command implements TabExecutor {
	private final CommandDispatcher dispatcher;
	
	public WhatIsCommand() {
		super("!whatis", "whatisbungee.use");
		dispatcher = new CommandDispatcher();
		
		dispatcher.registerCommand(new PrintFieldCommand());
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		dispatcher.dispatchCommand(sender, args);
	}
	
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		return dispatcher.dispatchTabComplete(sender, args);
	}
}
