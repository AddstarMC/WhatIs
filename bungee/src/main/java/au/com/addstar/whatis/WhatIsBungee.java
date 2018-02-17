package au.com.addstar.whatis;

import au.com.addstar.whatis.commands.WhatIsCommand;
import net.md_5.bungee.api.plugin.Plugin;

public class WhatIsBungee extends Plugin {
	@Override
	public void onEnable() {
		getProxy().getPluginManager().registerCommand(this, new WhatIsCommand());
	}
}
