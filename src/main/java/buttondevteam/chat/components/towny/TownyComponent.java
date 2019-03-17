package buttondevteam.chat.components.towny;

import buttondevteam.chat.PluginMain;
import buttondevteam.chat.formatting.TellrawPart;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.TBMCChatAPI;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TownyComponent extends Component<PluginMain> {
	public static TownyUniverse TU;
	private static ArrayList<String> Towns;
	private static ArrayList<String> Nations;

	private Channel TownChat;
	private Channel NationChat;

	@Override
	protected void enable() {
		TU = ((Towny) Bukkit.getPluginManager().getPlugin("Towny")).getTownyUniverse();
		Towns = TU.getTownsMap().values().stream().map(Town::getName).collect(Collectors.toCollection(ArrayList::new)); // Creates a snapshot of towns, new towns will be added when needed
		Nations = TU.getNationsMap().values().stream().map(Nation::getName).collect(Collectors.toCollection(ArrayList::new)); // Same here but with nations
		TBMCChatAPI.RegisterChatChannel(
			TownChat = new Channel("§3TC§f", Color.DarkAqua, "tc", s -> checkTownNationChat(s, false)));
		TBMCChatAPI.RegisterChatChannel(
			NationChat = new Channel("§6NC§f", Color.Gold, "nc", s -> checkTownNationChat(s, true)));
		TownyAnnouncer.setup(TownChat, NationChat);
	}

	@Override
	protected void disable() {
		TownyAnnouncer.setdown();
	}

	public void handleSpies(Channel channel, TellrawPart json, Function<TellrawPart, String> toJson) {
		if (channel.ID.equals(TownChat.ID) || channel.ID.equals(NationChat.ID)) {
			((List<TellrawPart>) json.getExtra()).add(0, new TellrawPart("[SPY]"));
			String jsonstr = toJson.apply(json);
			Bukkit.getServer().dispatchCommand(PluginMain.Console, String.format(
				"tellraw @a[score_%s=1000,score_%s_min=1000] %s", channel.ID, channel.ID, jsonstr));
		}
	}

	/**
	 * Return the error message for the message sender if they can't send it and the score
	 */
	private static Channel.RecipientTestResult checkTownNationChat(CommandSender sender, boolean nationchat) {
		if (!(sender instanceof Player))
			return new Channel.RecipientTestResult("§cYou are not a player!");
		Resident resident = TU.getResidentMap().get(sender.getName().toLowerCase());
		Channel.RecipientTestResult result = checkTownNationChatInternal(nationchat, resident);
		if (result.errormessage != null && resident != null && resident.getModes().contains("spy")) // Only use spy if they wouldn't see it
			result = new Channel.RecipientTestResult(1000, "allspies"); // There won't be more than a thousand towns/nations probably
		return result;
	}

	private static Channel.RecipientTestResult checkTownNationChatInternal(boolean nationchat,
	                                                                       Resident resident) {
		try {
			/*
			 * p.sendMessage(String.format("[SPY-%s] - %s: %s", channel.DisplayName, ((Player) sender).getDisplayName(), message));
			 */
			Town town = null;
			if (resident != null && resident.hasTown())
				town = resident.getTown();
			if (town == null)
				return new Channel.RecipientTestResult("You aren't in a town.");
			Nation nation = null;
			int index;
			if (nationchat) {
				if (town.hasNation())
					nation = town.getNation();
				if (nation == null)
					return new Channel.RecipientTestResult("Your town isn't in a nation.");
				index = getTownNationIndex(nation.getName(), true);
			} else
				index = getTownNationIndex(town.getName(), false);
			return new Channel.RecipientTestResult(index, nationchat ? nation.getName() : town.getName());
		} catch (NotRegisteredException e) {
			return new Channel.RecipientTestResult("You (probably) aren't knwon by Towny! (Not in a town)");
		}
	}

	public static int getTownNationIndex(String name, boolean nation) {
		val list = nation ? Nations : Towns;
		int index = list.indexOf(name);
		if (index < 0) {
			list.add(name);
			index = list.size() - 1;
		}
		return index;
	}
}
