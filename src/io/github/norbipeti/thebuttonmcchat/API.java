package io.github.norbipeti.thebuttonmcchat;

import io.github.norbipeti.thebuttonmcchat.commands.CommandCaller;
import io.github.norbipeti.thebuttonmcchat.commands.TBMCCommandBase;
import io.github.norbipeti.thebuttonmcchat.commands.ucmds.admin.PlayerInfoCommand;

public class API {

	/**
	 * <p>
	 * This method adds a command, adding it to help for example.
	 * </p>
	 * <p>
	 * The command must be registered in the caller plugin's plugin.yml.
	 * Otherwise the plugin will output a messsage to console.
	 * </p>
	 * 
	 * @param plugin
	 *            The caller plugin
	 * @param cmd
	 *            The command to add
	 */
	public void AddCommand(Plugin plugin, TBMCCommandBase cmd) {
		CommandCaller.AddCommand(plugin, cmd);
	}

	/**
	 * <p>
	 * Add player information for {@link PlayerInfoCommand}. Only mods can see
	 * the given information.
	 * </p>
	 * 
	 * @param player
	 * @param infoline
	 */
	public void AddPlayerInfoForMods(TBMCPlayer player, String infoline) {
		// TODO
	}

	/**
	 * <p>
	 * Add player information for hover text at {@link ChatProcessing}. Every
	 * online player can see the given information.
	 * </p>
	 * 
	 * @param player
	 * @param infoline
	 */
	public void AddPlayerInfoForHover(TBMCPlayer player, String infoline) {
		// TODO
	}
}
