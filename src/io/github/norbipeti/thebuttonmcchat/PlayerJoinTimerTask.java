package io.github.norbipeti.thebuttonmcchat;

import java.util.TimerTask;

public abstract class PlayerJoinTimerTask extends TimerTask {

	@Override
	public abstract void run();

	public MaybeOfflinePlayer mp;

}