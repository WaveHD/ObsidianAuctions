package me.virustotal.floauction.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.flobi.floauction.FloAuction;

public class MigrationUtil {
	
	public static void mapOldStrings()
	{
		HashMap<String, String> map = new HashMap<String, String>();
		File mappingsFile = new File(FloAuction.plugin.getDataFolder().getPath(), "mappings.yml");
		if(!mappingsFile.exists())
		{
			FloAuction.plugin.saveResource("mappings.yml", false);
		}
		FileConfiguration mapFile = YamlConfiguration.loadConfiguration(mappingsFile);
		
		List<String> mappings = mapFile.getStringList("mappings");
		for(String m : mappings)
		{
			String[] split = m.split(",");
			map.put(split[0], split[1]);
		}
		
		FloAuction.plugin.getLogger().log(Level.INFO, "Checking to see if strings need to be mapped");
		File languageFile = new File(FloAuction.plugin.getDataFolder().getPath(), "language.yml");
		FileConfiguration language = YamlConfiguration.loadConfiguration(languageFile);
		
		for(String key : language.getKeys(false))
		{
			if(language.isString(key))
			{
				String str = language.getString(key);
				for(String mString : map.values())
				{
					str = str.replace(mString, map.get(mString));
				}
				if(!language.getString(key).equals(str))
				{
					language.set(key, str);
				}
			}
		}
	}
	
	
	public static void migrateOldData(FloAuction plugin)
	{
		String path = plugin.getDataFolder().getAbsolutePath();
		String strippedPath = path.substring(0, path.lastIndexOf(File.separator));

		for(File file : new File(strippedPath).listFiles())
		{
			if(file.isFile())
			{
				String fileName = file.getName();
				if(fileName.contains("floAuction") && fileName.endsWith(".jar"))
				{
					plugin.getLogger().log(Level.INFO, "Disabling floAuction");
					try 
					{
						MigrationUtil.copyFile(file, new File(file.getAbsolutePath() + ".dis"));
						file.delete();
						break;
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
						break;
					}
				}
			}
		}

		File pathFolder = new File(path);
		plugin.getLogger().log(Level.INFO, "Checking to see if migration is needed...");
		if(pathFolder.exists())
		{
			plugin.getLogger().log(Level.INFO, "Migration not needed, skipping migration!");
		}
		else
		{

			String floAuctionPath = strippedPath + File.separator + "floAuction";
			File floAuctionFolder = new File(floAuctionPath);
			if(floAuctionFolder.exists())
			{
				pathFolder.mkdirs();
				for(File file : floAuctionFolder.listFiles())
				{
					if(file.getName().toLowerCase().endsWith(".yml"))
					{
						try 
						{
							File newFile = new File(path + File.separator + file.getName());
							MigrationUtil.copyFile(file, newFile);
						} 
						catch (IOException e) 
						{
							e.printStackTrace();
						}
						plugin.getLogger().log(Level.INFO, "Migrated file " + file.getName() + " to the ObsidianAuctions folder!");
					}
				}
			}
			else
			{
				plugin.getLogger().log(Level.INFO, "No data to migrate, will create default data!");
			}
		}
	}

	private static void copyFile(File sourceFile, File destFile) throws IOException 
	{
		if(!destFile.exists()) 
		{
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try 
		{
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		}
		finally 
		{
			if(source != null) 
			{
				source.close();
			}
			if(destination != null) 
			{
				destination.close();
			}
		}
	}
}