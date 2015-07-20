package tk.sznp.thebuttonautoflair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;

public class PluginMain extends JavaPlugin
{ //Translated to Java: 2015.07.15.
	//A user, which flair isn't obtainable:
	//https://www.reddit.com/r/thebutton/comments/31c32v/i_pressed_the_button_without_really_thinking/
    // Fired when plugin is first enabled
    @Override
    public void onEnable()
    {
		System.out.println("The Button Auto-flair Plugin by NorbiPeti (:P)");
		//System.out.println("Original C# version: http://pastebin.com/tX8xCPbp");
		//System.out.println("The Java version is... Also made by the same person.");
		//System.out.println("With the help of StackOverflow and similar.");
		/*catch(MalformedURLException e)
		{
		}
		catch(IOException e)
		{
		}*/
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		//System.out.println("Registering commands...");
		this.getCommand("u").setExecutor(new Commands());
		this.getCommand("u").setUsage(this.getCommand("u").getUsage().replace('&', '�'));
		try {
    		File file=new File("flairsaccepted.txt");
    		if(file.exists())
    		{
				BufferedReader br=new BufferedReader(new FileReader("flairsaccepted.txt"));
				String line;
				while ((line = br.readLine()) != null)
				{
					AcceptedPlayers.add(line.replace("\n", ""));
				}
				br.close();
			}
    		file=new File("flairsignored.txt");
    		if(file.exists())
    		{
				BufferedReader br=new BufferedReader(new FileReader("flairsignored.txt"));
				String line;
				while ((line = br.readLine()) != null)
				{
					IgnoredPlayers.add(line.replace("\n", ""));
				}
				br.close();
    		}
    		file=new File("autoflairconfig.txt");
    		if(file.exists())
    		{
				BufferedReader br=new BufferedReader(new FileReader(file));
				String line;
				while((line=br.readLine())!=null)
				{
					String[] s=line.split(" ");
					TownColors.put(s[0], s[1]);
				}
				br.close();
    		}
		} catch (IOException e) {
			System.out.println("Error!\n"+e);
		}
		//System.out.println("Registering done.");
		Runnable r=new Runnable(){public void run(){ThreadMethod();}};
		Thread t=new Thread(r);
		t.start();
    }
    Boolean stop=false;
    // Fired when plugin is disabled
    @Override
    public void onDisable()
    {
    	try
    	{
			FileWriter fw;
			fw = new FileWriter("flairsaccepted.txt");
			fw.close();
			fw = new FileWriter("flairsignored.txt");
			fw.close();
    	}
    	catch(Exception e)
    	{
			System.out.println("Error!\n"+e);
    	}
    	for(String player : AcceptedPlayers)
    	{
    		File file=new File("flairsaccepted.txt");
			try {
				BufferedWriter bw=new BufferedWriter(new FileWriter(file, true));
				bw.write(player+"\n");
				bw.close();
			} catch (IOException e) {
				System.out.println("Error!\n"+e);
			}
    	}
    	for(String player : IgnoredPlayers)
    	{
    		File file=new File("flairsignored.txt");
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
				bw.write(player+"\n");
				bw.close();
			} catch (IOException e) {
				System.out.println("Error!\n"+e);
			}
    	}
		stop=true;
    }
    
    public void ThreadMethod() //<-- 2015.07.16.
    {
    	/*System.out.println("Sleeping for 5 seconds..."); //2015.07.20.
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} //2015.07.20.*/
    	while(!stop)
    	{
			try
			{
				String body=DownloadString("https://www.reddit.com/r/TheButtonMinecraft/comments/3d25do/autoflair_system_comment_your_minecraft_name_and/.json?limit=1000");
				JSONArray json=new JSONArray(body).getJSONObject(1).getJSONObject("data").getJSONArray("children");
				for(Object obj : json)
				{
					JSONObject item = (JSONObject)obj;
					String author=item.getJSONObject("data").getString("author");
					String ign=item.getJSONObject("data").getString("body");
	                int start = ign.indexOf("IGN:") + "IGN:".length();
	                int end = ign.indexOf(' ', start);
	                if (end == -1 || end == start)
	                	end=ign.indexOf('\n', start); //2015.07.15.
	                if (end == -1 || end == start)
	                    ign = ign.substring(start);
	                else
	                    ign = ign.substring(start, end);
	                ign = ign.trim();
	                if(HasIGFlair(ign))
	                	continue;
					//System.out.println("Author: "+author);
					try {
					    Thread.sleep(10);
					} catch(InterruptedException ex) {
					    Thread.currentThread().interrupt();
					}
	                String[] flairdata = DownloadString("http://karmadecay.com/thebutton-data.php?users=" + author).replace("\"", "").split(":");
	                String flair;
	                if(flairdata.length > 1) //2015.07.15.
	                	flair = flairdata[1];
	                else
	                	flair="";
	                if (flair != "-1")
	                    flair = flair + "s";
	                else
	                    flair = "non-presser";
					String flairclass;
					//System.out.println("flairdata.length:"+flairdata.length);
					if(flairdata.length>2)
						flairclass = flairdata[2];
					else
						flairclass="unknown";
	                SetFlair(ign, flair, flairclass, author);
				}
				Thread.sleep(10000);
			}
			catch(Exception e)
			{
				System.out.println("Error!\n"+e);
			}
    	}
    }
    
    public String DownloadString(String urlstr) throws MalformedURLException, IOException
    {
		URL url = new URL(urlstr);
		URLConnection con = url.openConnection();
		con.setRequestProperty("User-Agent", "TheButtonAutoFlair");
		InputStream in = con.getInputStream();
		String encoding = con.getContentEncoding();
		encoding = encoding == null ? "UTF-8" : encoding;
		String body = IOUtils.toString(in, encoding);
		in.close();
		return body;
    }

    //It has to store offline player flairs too, therefore it can't use Player object
    public static Map<String, String> PlayerFlairs=new HashMap<String, String>();
    public static Map<String, String> PlayerUserNames=new HashMap<String, String>();
    //public Map<Player, String> PlayerFlairs=new HashMap<Player, String>();
    public static ArrayList<Player> Players=new ArrayList<Player>();
    public static ArrayList<String> AcceptedPlayers=new ArrayList<String>(); //2015.07.16.
    public static ArrayList<String> IgnoredPlayers=new ArrayList<String>(); //2015.07.16.
    //public static Map<String, String> PlayerTowns=new HashMap<String, String>(); //2015.07.20.
    public static Map<String, String> TownColors=new HashMap<String, String>(); //2015.07.20.
    public Boolean HasIGFlair(String playername)
    {
    	/*Player player=null;
    	for(Player p : Players)
    	{
    		if(p.getName()==playername)
    		{
				player=p;
				break;
    		}
    	}
    	if(player==null)
    		return false;*/
    	return PlayerFlairs.containsKey(playername);
    }
    
    public void SetFlair(String playername, String text, String flairclass, String username)
    {
    	String finalflair;
    	switch(flairclass)
    	{
    	case "press-1":
    		finalflair="�c("+text+")�r";
    		break;
    	case "press-2":
    		finalflair="�6("+text+")�r";
    		break;
    	case "press-3":
    		finalflair="�e("+text+")�r";
    		break;
    	case "press-4":
    		finalflair="�a("+text+")�r";
    		break;
    	case "press-5":
    		finalflair="�9("+text+")�r";
    		break;
    	case "press-6":
    		finalflair="�5("+text+")�r";
    		break;
    	case "no-press":
    		finalflair="�7(non-pr.)�r";
    		break;
    	case "cheater":
    		finalflair="�5("+text+")�r";
    		break;
		default:
			finalflair="";
			break;
    	}
    	if(finalflair.length()==0) //<-- 2015.07.20.
    		return;
    	PlayerFlairs.put(playername, finalflair);
    	PlayerUserNames.put(playername, username);
    	/*for(Player player : Players)
    	{
    		if(player.getName()==playername)
    		{
    			PlayerFlairs.put(player, finalflair);
    			break;
    		}
    	}*/
    	//System.out.println("SetFlair - playername: "+playername+" text: "+text+" flairclass: "+flairclass);
    	System.out.println("Added new flair to "+playername+": "+finalflair);
    	for(Player player : Players)
    	{
    		//System.out.println("Online player: "+player.getName());
    		//System.out.println("player.getName ("+player.getName()+") == playername ("+playername+"): "+(player.getName()==playername));
    		if(player.getName().equals(playername))
    		{
        		//System.out.println("DisplayName: "+player.getDisplayName());
    			//player.setDisplayName(player.getDisplayName()+finalflair);
    			AppendPlayerDisplayFlair(player, username, finalflair);
        		//System.out.println("DisplayName: "+player.getDisplayName());
    			break;
    		}
    	}
    }
    
    public static String GetFlair(Player player)
    { //2015.07.16.
    	String flair=PlayerFlairs.get(player.getName());
    	return flair==null ? "" : flair;
    }
    
    public static void AppendPlayerDisplayFlair(Player player, String username, String flair)
    {
    	if(IgnoredPlayers.contains(player.getName()))
    		return;
    	if(AcceptedPlayers.contains(player.getName()))
    		//player.setDisplayName(player.getDisplayName()+flair);
    		AppendPlayerDisplayFlairFinal(player, flair); //2015.07.20.
    	else
    		player.sendMessage("�9Are you Reddit user "+username+"?�r �6Type /u accept or /u ignore�r");
    }
    
    public static void AppendPlayerDisplayFlairFinal(Player player, String flair)
    { //2015.07.20.
    	//System.out.println("A");
    	String color = GetColorForTown(GetPlayerTown(player)); //TO!DO: Multiple colors put on first capital letters
    	String[] colors = color.substring(1).split("�");
    	String displayname=player.getDisplayName();
    	ArrayList<Integer> Positions=new ArrayList<>();
    	//System.out.println("B");
    	for(int i=0; i<displayname.length(); i++) {
            if(Character.isUpperCase(displayname.charAt(i))) {
                Positions.add(i);
            }
    	}
    	//System.out.println("C: Positions.size(): "+Positions.size());
    	String finalname="";
    	if(Positions.size()>=colors.length)
    	{
        	//System.out.println("D");
    		int x=0;
    		for(int i=0; i<Positions.size(); i++)
    		{
    			int pos=Positions.get(i);
    			int nextpos;
    			if(i!=Positions.size()-1)
        			nextpos=Positions.get(i+1);
    			else
    				nextpos=displayname.length();
    			//System.out.println("pos: "+pos+" nextpos: "+nextpos);
    			//System.out.println("nextpos-pos: "+(nextpos-pos));
    			//String substr="�"+colors[x++]+displayname.substring(pos, nextpos-pos)+"�r";
    			String substr="�"+colors[x++]+displayname.substring(pos, nextpos)+"�r";
    			finalname+=substr;
    		}
        	//System.out.println("F");
    	}
    	else
    	{
        	//System.out.println("E");
    		Positions.clear();
    		int unit=displayname.length()/colors.length;
    		int x=0;
    		for(int i=0; i<displayname.length()-unit; i+=unit)
    		{
    			int pos=i;
    			int nextpos;
    			if(i<displayname.length()-unit-unit)
    				nextpos=i+unit;
    			else
    				nextpos=displayname.length();
    			//System.out.println("pos: "+pos+" nextpos: "+nextpos);
    			//System.out.println("nextpos-pos: "+(nextpos-pos));
    			String substr="�"+colors[x++]+displayname.substring(pos, nextpos)+"�r";
    			finalname+=substr;
    		}
        	//System.out.println("G");
    	}
		//player.setDisplayName(color+displayname+"�r"+flair);
    	player.setDisplayName(finalname+flair);
    }
    
    public static String GetColorForTown(String townname)
    { //2015.07.20.
    	if(TownColors.containsKey(townname))
    		return TownColors.get(townname);
    	return "";
    }
    
    public static String GetPlayerTown(Player player)
    { //2015.07.20.
    	//List<Town> towns = TownyUniverse.getDataSource().getTowns();
    	try {
			Town town = WorldCoord.parseWorldCoord(player).getTownBlock().getTown();
			return town.getName();
		} catch (Exception e) {
			return "";
		}
    }
    
    public static void RemovePlayerDisplayFlairFinal(Player player, String flair)
    { //2015.07.20.
    	String color = GetColorForTown(GetPlayerTown(player));
    	String dname=player.getDisplayName();
		player.setDisplayName(dname.substring(dname.indexOf(color)+3, dname.indexOf(flair)));
    }
}
