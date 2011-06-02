package com.pete.meetup.location.manager;

public interface Servicex {

	public void startService();
	
	public void stopService(boolean destroying);
	
	public boolean isRunning();
}
