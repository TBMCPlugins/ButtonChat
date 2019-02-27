package buttondevteam.chat.components.towncolors.admin;

import buttondevteam.chat.PluginMain;
import buttondevteam.chat.commands.ucmds.admin.AdminCommandBase;
import buttondevteam.chat.components.towncolors.TownColorComponent;
import buttondevteam.chat.components.towncolors.TownyListener;
import buttondevteam.chat.components.towny.TownyComponent;
import buttondevteam.lib.chat.Color;
import com.palmergames.bukkit.towny.object.Town;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.dynmap.towny.DynmapTownyPlugin;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TownColorCommand extends AdminCommandBase {
    @Override
    public String GetHelpText(String alias)[] { // TODO: Command path aliases
        return new String[]{ //
                "§6---- Town Color ----", //
                "This command allows setting a color for a town.", //
                "The town will be shown with this color on Dynmap and all players in the town will appear in chat with these colors.", //
                "The colors will split the name evenly.", //
                "Usage: /" + GetCommandPath() + " <town> <colorname1> [colorname2...]", //
                "Example: /" + GetCommandPath() + " Alderon blue gray" //
        };
    }

    @Override
    public boolean OnCommand(CommandSender sender, String alias, String[] args) {
        return SetTownColor(sender, alias, args);
    }

    public static boolean SetTownColor(CommandSender sender, String alias, String[] args) {
        if (args.length < 2)
            return false;
	    if (!TownyComponent.TU.getTownsMap().containsKey(args[0].toLowerCase())) {
            sender.sendMessage("§cThe town '" + args[0] + "' cannot be found.");
            return true;
        }
	    Color[] clrs = new Color[args.length - 1];
	    Town targetTown = TownyComponent.TU.getTownsMap().get(args[0].toLowerCase());
	    for (int i = 1; i < args.length; i++) {
		    val c = getColorOrSendError(args[i], sender);
		    if (!c.isPresent())
                return true;
		    clrs[i - 1] = c.get();
        }
	    Color tnc;
	    try {
		    tnc = TownColorComponent.NationColor.get(targetTown.getNation().getName().toLowerCase());
	    } catch (Exception e) {
		    tnc = null;
	    }
	    if (tnc == null) tnc = Color.White; //Default nation color - TODO: Make configurable
	    for (Map.Entry<String, Color[]> other : TownColorComponent.TownColors.entrySet()) {
		    Color nc;
		    try {
			    nc = TownColorComponent.NationColor.get(TownyComponent.TU.getTownsMap().get(other.getKey()).getNation().getName().toLowerCase());
		    } catch (Exception e) { //Too lazy for lots of null-checks and it may throw exceptions anyways
			    nc = null;
		    }
		    if (nc == null) nc = Color.White; //Default nation color
		    if (nc.getName().equals(tnc.getName())) {
			    int C = 0;
			    if (clrs.length == other.getValue().length)
				    for (int i = 0; i < clrs.length; i++)
					    if (clrs[i].getName().equals(other.getValue()[i].getName()))
						    C++;
					    else break;
			    if (C == clrs.length) {
				    sender.sendMessage("§cThis town color combination is already used!");
				    return true;
			    }
		    }
	    }
	    TownColorComponent.TownColors.put(args[0].toLowerCase(), clrs);
	    TownyListener.updateTownMembers(targetTown);

        val dtp = (DynmapTownyPlugin) Bukkit.getPluginManager().getPlugin("Dynmap-Towny");
        if (dtp == null) {
            sender.sendMessage("§cDynmap-Towny couldn't be found §6but otherwise §btown color set.");
            PluginMain.Instance.getLogger().warning("Dynmap-Towny not found for setting town color!");
            return true;
        }
	    TownColorComponent.setTownColor(dtp, targetTown.getName(), clrs, tnc);
        sender.sendMessage("§bColor(s) set.");
        return true;
    }

	public static Optional<Color> getColorOrSendError(String name, CommandSender sender) {
		val c = Arrays.stream(Color.values()).skip(1).filter(cc -> cc.getName().equalsIgnoreCase(name)).findAny();
		if (!c.isPresent()) { //^^ Skip black
			sender.sendMessage("§cThe color '" + name + "' cannot be found."); //ˇˇ Skip black
			sender.sendMessage("§cAvailable colors: " + Arrays.stream(Color.values()).skip(1).map(TownColorCommand::getColorText).collect(Collectors.joining(", ")));
			sender.sendMessage("§cMake sure to type them exactly as shown above.");
		}
		return c;
	}

	public static String getColorText(Color col) {
		return String.format("§%x%s§r", col.ordinal(), col.getName());
	}

	public static String getTownNameCased(String name) {
		return TownyComponent.TU.getTownsMap().get(name.toLowerCase()).getName();
    }
}