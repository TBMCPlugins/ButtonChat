package buttondevteam.chat.commands;

import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import buttondevteam.chat.PluginMain;
import buttondevteam.discordplugin.TBMCDiscordAPI;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.chat.TBMCCommandBase;

public class CommandCaller implements CommandExecutor {

	private CommandCaller() {
	}

	private static CommandCaller instance;

	public static void RegisterCommands() {
		if (instance == null)
			instance = new CommandCaller();
		for (TBMCCommandBase c : TBMCChatAPI.GetCommands().values()) {
			if (!c.GetCommandPath().contains("/")) // Top-level command
			{
				PluginCommand pc = ((JavaPlugin) c.getPlugin()).getCommand(c.GetCommandPath());
				if (pc == null)
					new Exception("Can't find top-level command: " + c.GetCommandPath()).printStackTrace();
				else
					pc.setExecutor(instance);
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		String path = command.getName();
		for (String arg : args)
			path += "/" + arg;
		TBMCCommandBase cmd = TBMCChatAPI.GetCommands().get(path);
		int argc = 0;
		while (cmd == null && path.contains("/")) {
			path = path.substring(0, path.lastIndexOf('/'));
			argc++;
			cmd = TBMCChatAPI.GetCommands().get(path);
		}
		if (cmd == null) {
			sender.sendMessage("§cInternal error: Command not registered to CommandCaller");
			if (sender != Bukkit.getConsoleSender())
				Bukkit.getConsoleSender().sendMessage("§cInternal error: Command not registered to CommandCaller");
			return true;
		}
		if (cmd.GetModOnly() && !PluginMain.permission.has(sender, "tbmc.admin")) {
			sender.sendMessage("§cYou need to be a mod to use this command.");
			return true;
		}
		if (cmd.GetPlayerOnly() && !(sender instanceof Player)) {
			sender.sendMessage("§cOnly ingame players can use this command.");
			return true;
		}
		final String[] cmdargs = args.length > 0 ? Arrays.copyOfRange(args, args.length - argc, args.length) : args;
		try {
			if (!cmd.OnCommand(sender, alias, cmdargs))
				sender.sendMessage(cmd.GetHelpText(alias));
		} catch (Exception e) {
			TBMCDiscordAPI.SendException(e,
					"Failed to execute command " + cmd.GetCommandPath() + " with arguments " + cmdargs);
		}
		return true;
	}

	public static String[] GetSubCommands(TBMCCommandBase command) {
		ArrayList<String> cmds = new ArrayList<String>();
		cmds.add("§6---- Subcommands ----");
		for (TBMCCommandBase cmd : TBMCChatAPI.GetCommands().values()) {
			if (cmd.GetCommandPath().startsWith(command.GetCommandPath() + "/")) {
				int ind = cmd.GetCommandPath().indexOf('/', command.GetCommandPath().length() + 2);
				if (ind >= 0)
					continue;
				cmds.add(cmd.GetCommandPath().replace('/', ' '));
			}
		}
		return cmds.toArray(new String[cmds.size()]);
	}
}
