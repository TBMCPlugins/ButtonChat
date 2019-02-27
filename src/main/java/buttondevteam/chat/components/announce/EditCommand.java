package buttondevteam.chat.components.announce;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;

public class EditCommand extends AnnounceCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Edit announcement ----",
				"This command can only be used in a command block.",
				"Usage: /u annonunce edit <index> <text>" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias,
			String[] args) {
		if (!(sender instanceof BlockCommandSender)) {
			sender.sendMessage("§cError: This command can only be used from a command block. You can use add and remove, though it's not recommended.");
			return true;
		}
		if (args.length < 1) {
			return false;
		}
		StringBuilder sb1 = new StringBuilder();
		for (int i1 = 1; i1 < args.length; i1++) {
			sb1.append(args[i1]);
			if (i1 != args.length - 1)
				sb1.append(" ");
		}
		String finalmessage1 = sb1.toString().replace('&', '§');
		int index = Integer.parseInt(args[0]);
		if (index > 100)
			return false;
		AnnouncerComponent component = (AnnouncerComponent) getComponent();
		while (component.AnnounceMessages().get().size() <= index)
			component.AnnounceMessages().get().add("");
		component.AnnounceMessages().get().set(Integer.parseInt(args[0]),
				finalmessage1);
		sender.sendMessage("Announcement edited.");
		return true;
	}

}