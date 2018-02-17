package au.com.addstar.whatis.utils;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandDispatcher {
	public static Pattern usageArgumentPattern = Pattern.compile("(\\[<.*?>\\])|(\\[.*?\\])|(<.*?>)");
	
	private final Map<String, SubCommand> commandMap;
	private final List<SubCommand> commands;
	
	public CommandDispatcher() {
		commands = Lists.newArrayList();
		commandMap = Maps.newHashMap();
	}
	
	public void registerCommand(SubCommand command) {
		commands.add(command);
		
		commandMap.put(command.getName().toLowerCase(), command);
		if (command.getAliases() != null) {
			for (String alias : command.getAliases()) {
				commandMap.put(alias.toLowerCase(), command);
			}
		}
	}
	
	public void unregisterCommand(SubCommand command) {
		commands.remove(command);
		
		commandMap.remove(command.getName().toLowerCase(), command);
		if (command.getAliases() != null) {
			for (String alias : command.getAliases()) {
				commandMap.remove(alias.toLowerCase(), command);
			}
		}
	}
	
	private SubCommand getCommand(String commandName) {
		return commandMap.get(commandName.toLowerCase());
	}
	
	private void displayCommands(CommandSender sender, String commandName) {
		StringBuilder builder = new StringBuilder();
				
		boolean first = true;
		boolean odd = true;
		
		// Build the list
		for (SubCommand command : commands) {
			// Check that they have permission
			if (!Strings.isNullOrEmpty(command.getPermission()) && !sender.hasPermission(command.getPermission())) {
				continue;
			}
			
			if (!first) {
				builder.append(ChatColor.WHITE);
				builder.append(", ");
			}
			
			first = false;
			
			// Alternating colouring
			if (odd) {
				builder.append(ChatColor.WHITE);
			} else {
				builder.append(ChatColor.GRAY);
			}
			odd = !odd;
			
			builder.append(command.getName());
		}
		
		if (commandName == null) {
			sender.sendMessage(ChatColor.RED + "No command specified:");
		} else {
			sender.sendMessage(ChatColor.RED + "Unknown command: " + ChatColor.GOLD + commandName);
		}
		
		sender.sendMessage("Available commands:");
		sender.sendMessage(builder.toString());
	}
	
	private String colorUsage(String usage) {
		Matcher matcher = usageArgumentPattern.matcher(usage);
		StringBuffer buffer = new StringBuffer();
		
		while (matcher.find()) {
			String str;
			if (matcher.group(1) != null) {
				str = ChatColor.GREEN + matcher.group(1);
			} else if (matcher.group(2) != null) {
				str = ChatColor.GREEN + matcher.group(2);
			} else {
				str = ChatColor.GOLD + matcher.group(3);
			}
			
			matcher.appendReplacement(buffer, str);
		}
		
		matcher.appendTail(buffer);
		
		return buffer.toString();
	}
	
	public void dispatchCommand(CommandSender sender, String[] args) {
		if (args.length == 0) {
			// Display help
			displayCommands(sender, null);
			return;
		}
		
		SubCommand command = getCommand(args[0]);
		
		if (command == null) {
			// Display help
			displayCommands(sender, args[0]);
			return;
		}
		
		if (!Strings.isNullOrEmpty(command.getPermission()) && !sender.hasPermission(command.getPermission())) {
			// Display no permission
			sender.sendMessage(ChatColor.RED + String.format("You do not have permission to use %s", args[0]));
			return;
		}
		
		// Execute the command
		try {
			if (!command.onCommand(sender, Arrays.copyOfRange(args, 1, args.length))) {
				// Display usage
				sender.sendMessage(ChatColor.RED + "Incorrect number of arguments:");
				sender.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.YELLOW + args[0] + ChatColor.GRAY + " " + colorUsage(command.getUsage()));
			}
		} catch (BadArgumentException e) {
			String cmdString = ChatColor.GRAY.toString();
			for (int i = 0; i < args.length; ++i) {
				if (i == e.getIndex() + 1) {
					cmdString += ChatColor.RED + args[i] + ChatColor.GRAY;
				} else {
					cmdString += args[i];
				}
				
				cmdString += " ";
			}
			
			if (e.getIndex() >= args.length - 1) {
				cmdString += ChatColor.RED + "?";
			}
			
			sender.sendMessage(ChatColor.RED + "Error in command: " + cmdString);
			sender.sendMessage(ChatColor.RED + " " + e.getMessage());
			
			for (String line : e.getInfo()) {
				sender.sendMessage(ChatColor.GRAY + " " + line);
			}
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}
	}
	
	public Iterable<String> dispatchTabComplete(CommandSender sender, String[] args) {
		String input = args[args.length-1].toLowerCase();
		
		if (args.length == 1) {
			List<String> commands = Lists.newArrayList();
			for (Entry<String, SubCommand> entry : commandMap.entrySet()) {
				if (Strings.isNullOrEmpty(entry.getValue().getPermission()) || sender.hasPermission(entry.getValue().getPermission())) {
					commands.add(entry.getKey());
				}
			}
			
			return Iterables.filter(commands, (entry) -> entry.startsWith(input));
		} else {
			SubCommand command = getCommand(args[0]);
			if (command == null) {
				return null;
			}
			
			if (!Strings.isNullOrEmpty(command.getPermission()) && !sender.hasPermission(command.getPermission())) {
				return null;
			}
			
			return command.onTabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
		}
	}
}
