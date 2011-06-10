package com.pete.meetup.config;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.pete.meetup.location.PersonLocation;
import com.pete.meetup.utils.UUIDUtils;

public class AppConfig {

	private static final List<String> TEST_PHONE_IDS = new ArrayList<String>();
	
	static {
		TEST_PHONE_IDS.add("000000000000000");
		TEST_PHONE_IDS.add("353833042180437");
		TEST_PHONE_IDS.add("359145030170690");
	}
                                                        
	private static AppConfig instance = null;

	private String personId;
	private String sessionId;
	private String name;
	private boolean online;

	private List<PersonLocation> rememberedLocations;
	
	protected AppConfig() {
		// empty contructor
	}
	
	public synchronized static AppConfig getInstance() {
        if (instance == null) {
        	instance = new AppConfig();
        }
        return instance;
    }
	
	public boolean readAllConfig(Context context) {
		try {
			AppConfigManager manager = new AppConfigManager(context);
			manager.loadConfiguration(this);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean saveAllConfig(Context context) {
		try {
			AppConfigManager manager = new AppConfigManager(context);
			manager.saveConfiguration(this);
			saveRememberedLocations(context);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean saveRememberedLocations(Context context) {
		try {
			AppConfigManager manager = new AppConfigManager(context);
			manager.saveRememberedLocations(this.getRememberedLocations());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String getPersonId() {
		return personId;
	}

	public String getSessionId() {
		return sessionId;
	}
	
	public String getNewSessionId() {
		return UUIDUtils.generateUUID();
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public List<PersonLocation> getRememberedLocations() {
		return rememberedLocations;
	}

	public void setRememberedLocations(List<PersonLocation> rememberedLocations) {
		this.rememberedLocations = rememberedLocations;
	}
	
	public void addRememberedLocation(PersonLocation location) {
		if (this.rememberedLocations == null) {
			this.rememberedLocations = new ArrayList<PersonLocation>();
		}
		
		this.rememberedLocations.add(location);
	}
	
	public void removeRememberedLocation(PersonLocation location) {
		if (location != null &&
			this.rememberedLocations != null) {
			this.rememberedLocations.remove(location );
		}
	}
	
	public void clearRememberedLocations() {
		this.rememberedLocations = null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isTestPhone() {
		// TODO remove hardcoding
		return false;
		//return TEST_PHONE_IDS.contains(personId);
		//return true;
	}
	
	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}
}
