package com.pete.meetup.config;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pete.meetup.location.PersonLocation;

public class AppConfigManager {

	private static final String DATABASE_NAME = "Config";
	
	private ConfigOpenHelper databaseHelper;
	
	private class ConfigOpenHelper extends SQLiteOpenHelper {

	    private static final int DATABASE_VERSION = 1;
	    
	    // configuration table
	    private static final String TABLE_CONFIG 				= "Configuration";
	    private static final String CONFIG_COLUMN_PERSON 		= "PersonId";
	    private static final String CONFIG_COLUMN_SESSION		= "Session";
	    private static final String CONFIG_COLUMN_NAME			= "Name";
	    private static final String CONFIG_COLUMN_ONLINE		= "Online";
	    //private static final String CONFIG_COLUMN_URI			= "Uri";
	    
	    // location table
	    private static final String TABLE_LOCATION 				= "Location";
	    private static final String LOCATION_COLUMN_SESSION 	= "Session";
	    private static final String LOCATION_COLUMN_PERSON		= "PersonId";
	    private static final String LOCATION_COLUMN_LATITUDE 	= "Latitude";
	    private static final String LOCATION_COLUMN_LONGITUDE 	= "Longitude";
	    private static final String LOCATION_COLUMN_DESCRIPTION	= "Description";
	    
	    private static final String TABLE_CONFIG_CREATE =
	                "CREATE TABLE " + TABLE_CONFIG + " (" +
	                CONFIG_COLUMN_PERSON + " TEXT, " +
	                CONFIG_COLUMN_SESSION + " TEXT, " +
	    			CONFIG_COLUMN_NAME + " TEXT, " +
	    			CONFIG_COLUMN_ONLINE + " INTEGER);";// +
	                //CONFIG_COLUMN_URI + " TEXT);";
	    
	    private static final String TABLE_LOCATION_CREATE =
            "CREATE TABLE " + TABLE_LOCATION + " (" +
            LOCATION_COLUMN_SESSION + " TEXT," +
            LOCATION_COLUMN_PERSON + " TEXT," +
            LOCATION_COLUMN_LATITUDE + " TEXT, " +
            LOCATION_COLUMN_LONGITUDE + " TEXT," +
            LOCATION_COLUMN_DESCRIPTION + " TEXT);";

	    ConfigOpenHelper(Context context) {
	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	        db.execSQL(TABLE_CONFIG_CREATE);
	        db.execSQL(TABLE_LOCATION_CREATE);
	    }

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
		}
	}
	
	public AppConfigManager(Context context) {
		this.databaseHelper = new ConfigOpenHelper(context);
	}
	
	public void loadConfiguration(AppConfig config) {
		SQLiteDatabase database = null;
		try {
			database = databaseHelper.getReadableDatabase();
			
			loadConfig(database, config);
			loadLocations(database, config);
		}
		finally {
			if (database != null) {
				database.close();
			}
		}
	}
	
	private void loadConfig(SQLiteDatabase database, AppConfig config) {
		Cursor c = null;
		
		try {
			c = database.query(ConfigOpenHelper.TABLE_CONFIG,
					new String[] {ConfigOpenHelper.CONFIG_COLUMN_PERSON,
					ConfigOpenHelper.CONFIG_COLUMN_SESSION,
					ConfigOpenHelper.CONFIG_COLUMN_NAME,
					ConfigOpenHelper.CONFIG_COLUMN_ONLINE},
					null, null, null, null, null);
			
			// iterate cursor returned from query
			if (c != null) {
				if (c.moveToFirst()) {
					int column = 0;
					config.setPersonId(c.getString(column++));
					config.setSessionId(c.getString(column++));
					config.setName(c.getString(column++));
					config.setOnline(c.getInt(column++) != 0);
				}
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}
	
	private void loadLocations(SQLiteDatabase database, AppConfig config) {
		Cursor c = null;
		
		try {
			c = database.query(ConfigOpenHelper.TABLE_LOCATION,
					new String[] {
					ConfigOpenHelper.LOCATION_COLUMN_SESSION,
					ConfigOpenHelper.LOCATION_COLUMN_PERSON,
					ConfigOpenHelper.LOCATION_COLUMN_DESCRIPTION,
					ConfigOpenHelper.LOCATION_COLUMN_LATITUDE,
					ConfigOpenHelper.LOCATION_COLUMN_LONGITUDE},
					null, null, null, null, null);
			
			// hold key/value pairs in a map
			List<PersonLocation> locations = new ArrayList<PersonLocation>();
			
			// iterate cursor returned from query
			if (c != null) {
				if (c.moveToFirst()) {
					do {
						int column = 0;
						locations.add(new PersonLocation(c.getString(column++), // session
								c.getString(column++), // person
								c.getString(column++), 		// name/description
								c.getString(column++), 		// latitude
								c.getString(column++),		// longitude
								true)); 					// remembered
					} while (c.moveToNext());
				}
			}
			
			config.setRememberedLocations(locations);
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}
	
	public void saveConfiguration(AppConfig config) {
		SQLiteDatabase database = null;
		
		try {
			database = databaseHelper.getWritableDatabase();
			
			saveConfig(database, config);
			saveLocations(database, config.getRememberedLocations());
			
		} finally {
			if (database != null) {
				database.close();
			}
		}
	}
	
	public void saveRememberedLocations(List<PersonLocation> locations) {
		SQLiteDatabase database = null;
		try {
			database = databaseHelper.getWritableDatabase();
			
			saveLocations(database, locations);
			
		} finally {
			if (database != null) {
				database.close();
			}
		}
	}
	
	private void saveConfig(SQLiteDatabase database, AppConfig config) {
		// delete all first then re-add config
		database.delete(ConfigOpenHelper.TABLE_CONFIG, null, null);
		
		if (config != null) {
			ContentValues values = new ContentValues();
			values.put(ConfigOpenHelper.CONFIG_COLUMN_PERSON, config.getPersonId());
			values.put(ConfigOpenHelper.CONFIG_COLUMN_SESSION, config.getSessionId());
			values.put(ConfigOpenHelper.CONFIG_COLUMN_NAME, config.getName());
			values.put(ConfigOpenHelper.CONFIG_COLUMN_ONLINE, config.isOnline() ? 1 : 0);
			database.insert(ConfigOpenHelper.TABLE_CONFIG, null, values);	
		}
	}   
	
	private void saveLocations(SQLiteDatabase database, List<PersonLocation> locations) {
		// delete all first then re-add all locations
		database.delete(ConfigOpenHelper.TABLE_LOCATION, null, null);
		
		if (locations != null) {
			for (PersonLocation location : locations) {
				ContentValues values = new ContentValues();
				values.put(ConfigOpenHelper.LOCATION_COLUMN_SESSION, location.getSession());
				values.put(ConfigOpenHelper.LOCATION_COLUMN_PERSON, location.getPerson());
				values.put(ConfigOpenHelper.LOCATION_COLUMN_LATITUDE, location.getLatitude());
				values.put(ConfigOpenHelper.LOCATION_COLUMN_LONGITUDE, location.getLongitude());
				values.put(ConfigOpenHelper.LOCATION_COLUMN_DESCRIPTION, location.getName());
				database.insert(ConfigOpenHelper.TABLE_LOCATION, null, values);
			}
		}
	}
}
