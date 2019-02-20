package buttondevteam.chat.listener;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.PlayerJoinTimerTask;
import buttondevteam.chat.PluginMain;
import buttondevteam.chat.components.fun.UnlolCommand;
import buttondevteam.chat.commands.ucmds.HistoryCommand;
import buttondevteam.chat.components.flair.FlairComponent;
import buttondevteam.chat.components.flair.FlairStates;
import buttondevteam.chat.components.towncolors.TownColorComponent;
import buttondevteam.core.ComponentManager;
import buttondevteam.lib.player.TBMCPlayerJoinEvent;
import buttondevteam.lib.player.TBMCPlayerLoadEvent;
import buttondevteam.lib.player.TBMCPlayerSaveEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Timer;

public class PlayerJoinLeaveListener implements Listener {

	@EventHandler
	public void onPlayerLoad(TBMCPlayerLoadEvent e) {
		ChatPlayer cp = e.GetPlayer().asPluginPlayer(ChatPlayer.class);
		cp.FlairUpdate();
	}

	@EventHandler
	public void onPlayerTBMCJoin(TBMCPlayerJoinEvent e) {
		ChatPlayer cp = e.GetPlayer().asPluginPlayer(ChatPlayer.class);
		Player p = e.getPlayer();

		if (ComponentManager.isEnabled(FlairComponent.class)) {
			if (!cp.FlairState().get().equals(FlairStates.NoComment)) {
				FlairComponent.ConfirmUserMessage(cp);
				Timer timer = new Timer();
				PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {
					@Override
					public void run() {
						mp.FlairUpdate();
					}
				};
				tt.mp = cp;
				timer.schedule(tt, 1000);
			} //TODO: Better Reddit integration (OAuth)
		}

		String nwithoutformatting = PluginMain.essentials.getUser(p).getNickname();

		int index;
		if (nwithoutformatting != null) {
			while ((index = nwithoutformatting.indexOf("§k")) != -1)
				nwithoutformatting = nwithoutformatting.replace("§k" + nwithoutformatting.charAt(index + 2), ""); // Support for one random char
			while ((index = nwithoutformatting.indexOf('§')) != -1)
				nwithoutformatting = nwithoutformatting.replace("§" + nwithoutformatting.charAt(index + 1), "");
		} else
			nwithoutformatting = p.getName();
		PlayerListener.nicknames.forcePut(nwithoutformatting.toLowerCase(), p.getUniqueId());

		TownColorComponent.updatePlayerColors(p, cp); //TO!DO: Doesn't have effect - It can help to register the listener

		if (cp.ChatOnly || p.getGameMode().equals(GameMode.SPECTATOR)) {
			cp.ChatOnly = false;
			p.setGameMode(GameMode.SURVIVAL);
		}

		HistoryCommand.showHistory(e.getPlayer(), "u history", new String[0], null);
	}

	@EventHandler
	public void onPlayerSave(TBMCPlayerSaveEvent e) {
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		PlayerListener.nicknames.inverse().remove(event.getPlayer().getUniqueId());
		UnlolCommand.Lastlol.values().removeIf(lld -> lld.getLolowner().equals(event.getPlayer()));
	}

}
