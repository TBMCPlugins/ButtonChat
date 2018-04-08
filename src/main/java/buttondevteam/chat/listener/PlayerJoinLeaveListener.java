package buttondevteam.chat.listener;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.FlairStates;
import buttondevteam.chat.PlayerJoinTimerTask;
import buttondevteam.chat.PluginMain;
import buttondevteam.chat.commands.UnlolCommand;
import buttondevteam.lib.player.TBMCPlayerJoinEvent;
import buttondevteam.lib.player.TBMCPlayerLoadEvent;
import buttondevteam.lib.player.TBMCPlayerSaveEvent;
import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
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
		if (PluginMain.essentials == null)
			PluginMain.essentials = ((Essentials) Bukkit.getPluginManager().getPlugin("Essentials"));
		ChatPlayer cp = e.GetPlayer().asPluginPlayer(ChatPlayer.class);
		Player p = Bukkit.getPlayer(cp.getUUID());

		if (!cp.FlairState().get().equals(FlairStates.NoComment)) {
			PluginMain.ConfirmUserMessage(cp);
			Timer timer = new Timer();
			PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {
				@Override
				public void run() {
					p.setPlayerListName(p.getName() + mp.GetFormattedFlair());
				}
			};
			tt.mp = cp;
			timer.schedule(tt, 1000);
		} else {
			/*Timer timer = new Timer();
			PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {

				@Override
				public void run() {
					Player player = Bukkit.getPlayer(mp.PlayerName().get());
					if (player == null)
						return;

					if (mp.FlairState().get().equals(FlairStates.NoComment)) {
						String json = String.format(
								"[\"\",{\"text\":\"If you're from Reddit and you'd like your /r/TheButton flair displayed ingame, write your Minecraft name to \",\"color\":\"aqua\"},{\"text\":\"[this thread].\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"%s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click here to go to the Reddit thread\",\"color\":\"aqua\"}]}}}]",
								PluginMain.FlairThreadURL);
						PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console,
								"tellraw " + mp.PlayerName() + " " + json);
						json = "[\"\",{\"text\":\"If you aren't from Reddit or don't want the flair, type /u ignore to prevent this message after next login.\",\"color\":\"aqua\"}]";
						PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console,
								"tellraw " + mp.PlayerName() + " " + json);
					}
				}
			};
			tt.mp = cp;
			timer.schedule(tt, 15 * 1000);*/ //TODO: Better Reddit integration (OAuth)
		}

		String nwithoutformatting = PluginMain.essentials.getUser(p).getNickname();

        p.setDisplayName();

		int index;
		if (nwithoutformatting != null) {
			while ((index = nwithoutformatting.indexOf("§k")) != -1)
				nwithoutformatting = nwithoutformatting.replace("§k" + nwithoutformatting.charAt(index + 2), ""); // Support for one random char
			while ((index = nwithoutformatting.indexOf('§')) != -1)
				nwithoutformatting = nwithoutformatting.replace("§" + nwithoutformatting.charAt(index + 1), "");
		} else
			nwithoutformatting = p.getName();
		PlayerListener.nicknames.put(nwithoutformatting, p.getUniqueId());

		cp.FlairUpdate();

		if (cp.ChatOnly || p.getGameMode().equals(GameMode.SPECTATOR)) {
			cp.ChatOnly = false;
			p.setGameMode(GameMode.SURVIVAL);
		}
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
