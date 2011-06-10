package com.pete.meetup;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.pete.meetup.config.AppConfig;
import com.pete.meetup.location.PersonLocation;
import com.pete.meetup.location.manager.Locations;
import com.pete.meetup.map.BalloonManager;
import com.pete.meetup.map.LocationAlert;
import com.pete.meetup.map.LocationTapListener;
import com.pete.meetup.map.MapZoomController;
import com.pete.meetup.map.MyOverlay;
import com.pete.meetup.map.NavigationHandler;
import com.pete.meetup.map.OtherPeopleOverlay;
import com.pete.meetup.map.PersonOverlay;
import com.pete.meetup.map.RememberedOverlay;
import com.pete.meetup.prompt.AbstractPrompt;
import com.pete.meetup.prompt.AlertPrompt;
import com.pete.meetup.prompt.ChangeViewPrompt;
import com.pete.meetup.prompt.EnterTextPrompt;
import com.pete.meetup.prompt.GPSPrompt;
import com.pete.meetup.prompt.PromptDismissListener;
import com.pete.meetup.prompt.ReturnCode;
import com.pete.meetup.prompt.SelectPersonPrompt;
import com.pete.meetup.prompt.YesNoPrompt;
import com.pete.meetup.service.MyLocationService;
import com.pete.meetup.service.RemoteUpdateService;
import com.pete.meetup.sms.InviteManager;
import com.pete.meetup.sms.PhoneContact;
import com.pete.meetup.utils.MyUncaughExceptionHandler;
import com.pete.meetup.utils.NameFormatter;
import com.pete.meetup.utils.PackageUtils;
import com.pete.meetup.utils.TextFormatter;
import com.pete.meetup.utils.UUIDUtils;

public class MeetUp extends MapActivity implements PromptDismissListener,
 LocationTapListener {
	
	public static final String LOG_TAG = "MeetUp";
	private static final int MAX_NAME_LENGTH = 8;
	
	public static final String ACTION_MY_LOCATION_UPDATE 	= "com.pete.meetup.action.MY_LOCATION_UPDATE";
	public static final String ACTION_LOCATIONS_UPDATE 		= "com.pete.meetup.action.LOCATIONS_UPDATE";
	
	private static final int MENU_GROUP_NAVIGATION 		= 1;
	private static final int MENU_ITEM_NAVIGATE_DRIVE 	= 1;
	private static final int MENU_ITEM_NAVIGATE_WALK 	= 2;
	private static final int MENU_ITEM_REMOVE 			= 3;
	private static final int MENU_ITEM_REMEMBER 		= 4;
	
	private static final int DIALOG_PROMPT_GPS 				= 0;
	private static final int DIALOG_PROMPT_NAME				= 1;
	private static final int DIALOG_PROMPT_REMEMBER_DESC	= 2;
	private static final int DIALOG_PROMPT_SELECT_PERSON	= 3;
	private static final int DIALOG_PROMPT_CONTINUE_SESSION	= 4;
	private static final int DIALOG_PROMPT_CONFIRM_INVITE	= 5;
	private static final int DIALOG_PROMPT_TEST_MODE		= 6;
	private static final int DIALOG_PROMPT_INSTALL_NAV		= 7;
	private static final int DIALOG_PROMPT_EXIT_APP			= 8;
	private static final int DIALOG_PROMPT_CHANGE_VIEW		= 9;
	private static final int DIALOG_HELP					= 10;
	
	private static final int OVERLAY_ME						= 1;
	private static final int OVERLAY_OTHERS					= 2;
	private static final int OVERLAY_REMEMBERED				= 3;
	
	private static final String INTENT_READ 				= "IntentRead";
	
	private static final int CONTACT_PICKER_RESULT 	= 1001;
	private static final int GPS_RESULT				= 1002;

	private static ActivityState currentState = ActivityState.STATE_START_UP;
	private static TextFormatter nameFormatter = new NameFormatter(MAX_NAME_LENGTH);
	private static CentreMode centreMode = CentreMode.EVERYONE;
	private static PersonLocation centrePerson = null;
	private static ViewType viewType = ViewType.STREET;
	
	private PersonOverlay myOverlay;
	private PersonOverlay activeOverlay;
	//private PersonOverlay inactiveOverlay;
	private PersonOverlay rememberedOverlay;
	private MapView mapView;
	private PersonLocation selectedLocation;
	private ProgressDialog progressDialog;
	private boolean launchedFromIntent = false;
	private PhoneContact inviteContact;
	private BroadcastReceiver sendInviteReceiver;
	private BroadcastReceiver serviceIntentReceiver;
	private boolean navWalking;
	private boolean continueSession = true;
	private BalloonManager balloonManager = null;
	private boolean firstUsage = false;
	private boolean intentRead = false;
	
	private enum ActivityState {
		STATE_START_UP,
		STATE_INITIAL_INVOCATION,
		STATE_AWAITING_USER_INPUT,
		STATE_PROMPT_TEST_MODE,
		STATE_PROMPT_CONTINUE_SESSSION,
		STATE_START_NEW_SESSION,
		STATE_CONTINUE_LAST_SESSION,
		STATE_PROMPT_NAME_ENTRY,
		STATE_NAME_ENTERED,
		STATE_GPS_PROMPT,
		STATE_AWAITING_LOCATION,
		STATE_FOUND_LOCATION
	};
	
	private enum ServiceType {
		MY_LOCATION,
		REMOTE,
	}

	public enum CentreMode {
		DISABLED,
		SINGLE_PERSON,
		EVERYONE
	}
	
	public enum ViewType {
		SATELLITE,
		STREET
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		
		if (savedInstanceState != null) {
			intentRead = savedInstanceState.getBoolean(INTENT_READ, false);
		}
		
		// global exception handler tied into flurry
		Thread.setDefaultUncaughtExceptionHandler(
				new MyUncaughExceptionHandler(this));

		initialise();
		
		initialiseMap();
		
		activityStateMachine(currentState);
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		
		initialise();
		
		activityStateMachine(currentState);
	}
	
	private void initialise() {
		launchedFromIntent = initialiseConfiguration(getIntent());
		
		firstUsage = (AppConfig.getInstance().getSessionId() == null);
		
		if (launchedFromIntent) {
			// TODO why is the state getting set here?
			//currentState = ActivityState.STATE_START_UP;
		}
		
		if (sendInviteReceiver == null) {
			sendInviteReceiver = new InviteSendReceiver();
			registerReceiver(sendInviteReceiver,
					new IntentFilter(InviteManager.ACTION_SMS_SENT));
		}
		
		if (serviceIntentReceiver == null) {
			serviceIntentReceiver = new IntentReceiver();
			IntentFilter intentToReceiveFilter = new IntentFilter();
			intentToReceiveFilter.addAction(ACTION_MY_LOCATION_UPDATE);
			intentToReceiveFilter.addAction(ACTION_LOCATIONS_UPDATE);
			
			this.registerReceiver(serviceIntentReceiver,
					intentToReceiveFilter);
		}
	}
	
	private void setCurrentState(ActivityState newState) {
		currentState = newState;
	}
	
	private void activityStateMachine(ActivityState newState) {
		currentState = newState;
		
		switch (currentState) {
		case STATE_START_UP:
			if (AppConfig.getInstance().isTestPhone()) {
				activityStateMachine(ActivityState.STATE_PROMPT_TEST_MODE);
			} else {
				// otherwise just continue normal path
				activityStateMachine(ActivityState.STATE_INITIAL_INVOCATION);
			}
			break;
		case STATE_PROMPT_TEST_MODE:
			showDialog(DIALOG_PROMPT_TEST_MODE);
			setCurrentState(ActivityState.STATE_AWAITING_USER_INPUT);
			break;
		case STATE_INITIAL_INVOCATION:
			if (AppConfig.getInstance().getSessionId() == null) {
				setNewSessionId(AppConfig.getInstance().getNewSessionId());
				activityStateMachine(ActivityState.STATE_PROMPT_NAME_ENTRY);
			} else {
				// session id already set
				if (launchedFromIntent) {
					// if launched from intent then continue prompting for name etc
					activityStateMachine(ActivityState.STATE_PROMPT_NAME_ENTRY);
				} else {
					// if launched by used prompt to continue last session
					activityStateMachine(ActivityState.STATE_PROMPT_CONTINUE_SESSSION);
				}
			}
			break;
		case STATE_PROMPT_CONTINUE_SESSSION:
			showDialog(DIALOG_PROMPT_CONTINUE_SESSION);
			setCurrentState(ActivityState.STATE_AWAITING_USER_INPUT);
			break;
		case STATE_START_NEW_SESSION:
			setNewSessionId(AppConfig.getInstance().getNewSessionId());
			activityStateMachine(ActivityState.STATE_PROMPT_NAME_ENTRY);
			break;
		case STATE_CONTINUE_LAST_SESSION:
			activityStateMachine(ActivityState.STATE_GPS_PROMPT);
			break;
		case STATE_PROMPT_NAME_ENTRY:
			showDialog(DIALOG_PROMPT_NAME);
			setCurrentState(ActivityState.STATE_AWAITING_USER_INPUT);
			break;
		case STATE_NAME_ENTERED:
			activityStateMachine(ActivityState.STATE_GPS_PROMPT);
			break;
		case STATE_GPS_PROMPT:
			if (!isLocationServiceEnabled(LocationManager.GPS_PROVIDER)/* &&
				!isLocationServiceEnabled(LocationManager.NETWORK_PROVIDER)*/) {
				showDialog(DIALOG_PROMPT_GPS);
				setCurrentState(ActivityState.STATE_AWAITING_USER_INPUT);
	    	} else {
	    		activityStateMachine(ActivityState.STATE_AWAITING_LOCATION);
	    	}
			break;
		case STATE_AWAITING_LOCATION:
			showWaitingLocation();
			startService(ServiceType.MY_LOCATION);
			break;
		case STATE_FOUND_LOCATION:
			// this is required in order to reset my location
			startService(ServiceType.MY_LOCATION);
			
			// recentre map as orientation may have changed
			centreMap(centreMode);
			
			if (goOnline()) {
				startService(ServiceType.REMOTE);
			}
			break;
		}
	}
	
	private boolean goOnline() {
		// go online if 
		// - app launched from intent
		// - test phone
		// - continuing previous session which was online
		if (launchedFromIntent ||
			AppConfig.getInstance().isTestPhone() ||
			(continueSession && AppConfig.getInstance().isOnline())) {
			return true;
		}
		return false;
	}
	
	private void setNewSessionId(String sessionId) {
		continueSession = false;
		AppConfig.getInstance().setOnline(false);
		centrePerson = null;
		setCentreMode(CentreMode.EVERYONE, false);
		AppConfig.getInstance().setSessionId(sessionId);
		clearLocations();
	}
	
	private void clearLocations() {
		AppConfig.getInstance().clearRememberedLocations();
		Locations.clearOtherLocations();
	}
	
	public boolean isLocationServiceEnabled(String provider)
	{	
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		if (locationManager != null) {
			return locationManager.isProviderEnabled(provider);
		}
		return false;
	}
	
	private void startService(ServiceType serviceType) {
		switch (serviceType) {
		case MY_LOCATION:
			// initialise my location
			initialiseMyLocation();
			
			initialiseMapOverlays();
			
			startService(new Intent(this, MyLocationService.class));
			break;
		case REMOTE:
			startService(new Intent(this, RemoteUpdateService.class));
			AppConfig.getInstance().setOnline(true);
		}
	}
	
	private void stopService(ServiceType serviceType) {
		
		switch (serviceType) {
			case MY_LOCATION:
				try {
					stopService(new Intent(this, MyLocationService.class));
				} catch (Exception e) {
					// swallow
				}
				break;
			case REMOTE:
				try {
					stopService(new Intent(this, RemoteUpdateService.class));
				} catch (Exception e) {
					// swallow
				}
				break;
		}
	}

	private void initialiseMyLocation() {
		PersonLocation myLocation = new PersonLocation(
				AppConfig.getInstance().getSessionId(),
				AppConfig.getInstance().getPersonId(),
				AppConfig.getInstance().getName());
		
		// initialise with last known location
		PersonLocation lastLocation = Locations.getMyLocation();
		
		if (lastLocation != null) {
			myLocation.setLocation(lastLocation.getLocation());
		}
		
		myLocation.setMe(true);
		Locations.setMyLocation(myLocation);
	}
	
	private void initialiseMap() {
		mapView = (MapView) findViewById(R.id.mapview);      
		mapView.setLongClickable(true);
		this.registerForContextMenu(mapView);

		List<Overlay> overlays = mapView.getOverlays();
		
		balloonManager = new BalloonManager(mapView);
		
		Drawable personPinGreen = this.getResources().getDrawable(R.drawable.marker_green);
		activeOverlay = new OtherPeopleOverlay(this, mapView, personPinGreen,
				nameFormatter, OVERLAY_OTHERS, balloonManager);
		
		Drawable personPinGray = this.getResources().getDrawable(R.drawable.marker_grey);
		rememberedOverlay = new RememberedOverlay(this, mapView, personPinGray,
				nameFormatter, OVERLAY_REMEMBERED, balloonManager);
		
		/*
		Drawable personPinRed = this.getResources().getDrawable(R.drawable.marker_red);
		inactiveOverlay = new OtherPeopleOverlay(this, mapView, personPinRed, Color.BLACK, nameFormatter);
		*/
		
		Drawable personPinRed = this.getResources().getDrawable(R.drawable.marker_red);
		myOverlay = new MyOverlay(this, mapView, personPinRed, OVERLAY_ME, balloonManager);
		
		overlays.add(activeOverlay);
		//overlays.add(inactiveOverlay);
		overlays.add(rememberedOverlay);
		overlays.add(myOverlay);
		
		setCentreMode(centreMode, false);
		setViewType(viewType);
	}
	
	private void initialiseMapOverlays() {
		activeOverlay.setLocations(Locations.getOtherLocations());
		rememberedOverlay.setLocations(
				AppConfig.getInstance().getRememberedLocations());
		//inactiveOverlay.setLocations(Locations.getLoggedOutLocations());
		myOverlay.setLocation(Locations.getMyLocation());
	}
	
	private boolean initialiseConfiguration(Intent intent) {

		boolean lauchedFromIntent = false;
		
		AppConfig.getInstance().readAllConfig(this);
		
		String personId = AppConfig.getInstance().getPersonId();
		
		// set person id to device id - IMEI number of phone
		if (personId == null) {
			personId = getDeviceId();
			AppConfig.getInstance().setPersonId(personId);
		}
		
		// if application launched from a SMS read the session id
		if (!intentRead &&
			intent != null &&
			intent.getData() != null &&
			intent.getAction().equals(Intent.ACTION_VIEW)) {
			
        	String param = intent.getData().getQueryParameter(InviteManager.PARAM_SESSION);
        	
        	setNewSessionId(param);

        	lauchedFromIntent = true;
        	
        	// avoid intent being used multiple times i.e. when screen orientation changes
        	intentRead = true;
        } 
		
		return lauchedFromIntent;
	}
	
	private void myLocationChanged() {

		PersonLocation myLocation = Locations.getMyLocation();

		if (myLocation != null) {
			//myLocation.setLocation(location);

			myOverlay.setLocation(myLocation);

			// if this is the first fix zoom to this location
			if (currentState.equals(ActivityState.STATE_AWAITING_LOCATION)) {

				if (progressDialog != null) {
					progressDialog.dismiss();
				}

				activityStateMachine(ActivityState.STATE_FOUND_LOCATION);
				
				if (firstUsage) {
					//display prompt - how to invite people
					showDialog(DIALOG_HELP);
				}
			}

			centreMap(centreMode);
		}
	}

	private void locationsChanged() {
		activeOverlay.setLocations(Locations.getOtherLocations());
		//activeOverlay.setLocations(Locations.getLoggedInLocations());
		//inactiveOverlay.setLocations(Locations.getLoggedOutLocations());
		
		// if new locations prompt + zoom to middle
		if (Locations.hasNewLocations()) {	
			vibrate();
			
			// zoom to middle of points
			centreMap(CentreMode.EVERYONE);
		} else {
			centreMap(centreMode);
		}
		
		// display alerts for people joining, logging in and logging out
		displayAlerts();
	}
	
	private void displayAlerts() {
		/*
		LocationAlert newAlert = new LocationAlert(getApplicationContext(),
				Locations.getNewLocations(), "just joined in.");
		newAlert.display();
		*/
		if (!Locations.getLoggedInNotifications().isEmpty() ||
			!Locations.getLoggedOutNotifications().isEmpty()) {
			vibrate();
		}
		
		LocationAlert loginAlert = new LocationAlert(this,
				Locations.getLoggedInNotifications(), "just logged in.");
		loginAlert.display();
		
		LocationAlert logoutAlert = new LocationAlert(this,
				Locations.getLoggedOutNotifications(), "just logged out.");
		logoutAlert.display();
	}
	
	private String getDeviceId() {
		try {
			TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			return tManager.getDeviceId();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this.getApplicationContext(), "IEZK4X4B2VEGT7PWIL5K");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		try {
			AppConfig.getInstance().saveAllConfig(this);
		} catch (Exception e) {
			//log and swallow
		}
		
	    super.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this.getApplicationContext());
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (isFinishing()) {
			stopService(ServiceType.REMOTE);
			stopService(ServiceType.MY_LOCATION);
		}
		
		// unregister broadcast receivers
		if (sendInviteReceiver != null) {
			unregisterReceiver(sendInviteReceiver);
		}
		
		if (serviceIntentReceiver != null) {
			unregisterReceiver(serviceIntentReceiver);
		}
		
		// reset state if destroying app i.e. user clicked back button
		if (isFinishing()) {
			currentState = ActivityState.STATE_START_UP;
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putBoolean(INTENT_READ, intentRead);
		// save the config
		//AppConfig.getInstance().write(outState);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.map_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		 // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_item_invite:
	    	selectContacts();
	        return true;
	    case R.id.menu_item_zoom:
	    	showDialog(DIALOG_PROMPT_SELECT_PERSON);
	    	return true;
	    case R.id.menu_item_view:
	    	showDialog(DIALOG_PROMPT_CHANGE_VIEW);
	    	return true;
	    	
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
    @Override
    public void onCreateContextMenu(ContextMenu cm, View v, ContextMenuInfo menuInfo) {       
       super.onCreateContextMenu(cm, v, menuInfo);
       
       if (selectedLocation != null) {
	       cm.setHeaderTitle("Options: ");
	       
	       // don't want to remember a remembered location
	       if (!selectedLocation.isRemembered()) {
	    	   cm.add(0, MENU_ITEM_REMEMBER, 0, "remember this location");
	       }
	       if (!selectedLocation.isMe()) {
	    	   cm.add(0, MENU_ITEM_REMOVE, 0, "remove from map");
	    	   
		       cm.add(MENU_GROUP_NAVIGATION, MENU_ITEM_NAVIGATE_DRIVE, 0, "navigate to " + 
		    		   selectedLocation.getNameForDisplay(nameFormatter) + " (driving)");
		       
		       cm.add(MENU_GROUP_NAVIGATION, MENU_ITEM_NAVIGATE_WALK, 0, "navigate to " + 
		    		   selectedLocation.getNameForDisplay(nameFormatter) + " (walking)"); 
	       }
       }
    }
    
    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	
    	if (selectedLocation != null) {
			switch (item.getItemId()) {
			case MENU_ITEM_NAVIGATE_DRIVE:
				handleNavigation(selectedLocation, false);
				break;
			case MENU_ITEM_NAVIGATE_WALK:
				handleNavigation(selectedLocation, true);
				break;
			case MENU_ITEM_REMOVE:
				if (selectedLocation.isRemembered()) {
					AppConfig.getInstance().removeRememberedLocation(selectedLocation);
					rememberedOverlay.setLocations(AppConfig.getInstance().getRememberedLocations());
					AppConfig.getInstance().saveRememberedLocations(this);
				} else if (!selectedLocation.isMe()) {
					Locations.addRemovedLocation(selectedLocation);
					activeOverlay.setLocations(Locations.getOtherLocations());
				}
				
				// if were currently zooming on the removed person reset the centre mode
				if (centrePerson != null &&
					centreMode == CentreMode.SINGLE_PERSON &&
					centrePerson.equals(selectedLocation)) {
					setCentreMode(CentreMode.EVERYONE, true);
				}
				break;
			case MENU_ITEM_REMEMBER:
				// prompt for a place description
				showDialog(DIALOG_PROMPT_REMEMBER_DESC);
				break;
			}
    	}
		
		return super.onContextItemSelected(item);
	}
    
    private void handleNavigation(PersonLocation location, boolean walking) {
    	navWalking = walking;
    	// check if navigation installed
	    if (!PackageUtils.isIntentAvailable(getApplicationContext(), Intent.ACTION_VIEW, Uri.parse("google.navigation:ll=0,0"))) {
	    	showDialog(DIALOG_PROMPT_INSTALL_NAV);
	    } else {
	    	showDialog(DIALOG_PROMPT_EXIT_APP);
	    }
    }
    
    public void showWaitingLocation() {
		progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Waiting for location.\nThis may take a while, especially indoors.");
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			public void onCancel(DialogInterface paramDialogInterface) {
				showDialog(DIALOG_PROMPT_EXIT_APP);
			}
		});
        progressDialog.show();
    }
    
    public void selectContacts() {
    	Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
    	startActivityForResult(intent, CONTACT_PICKER_RESULT);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {  
	    
	    switch (requestCode) {  
    		case CONTACT_PICKER_RESULT:
    		if (resultCode == RESULT_OK) {  
		    	// handle contact results
		    	Uri uri = intent.getData();
		    	String contactId = uri.getLastPathSegment();
		    	inviteContact = InviteManager.readContact(contactId, getApplicationContext());
		    	
		    	// confirm user OK to send text invite
		    	showDialog(DIALOG_PROMPT_CONFIRM_INVITE);
    		}
    		break;
    		case GPS_RESULT:
    			activityStateMachine(ActivityState.STATE_GPS_PROMPT);
    			break;
	    }
	}
    
    /**
     * Receiver for invite sent messages
     * @author Pete
     *
     */
    private class InviteSendReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        String message = null;
	        switch (getResultCode()) {
	        case Activity.RESULT_OK:
	            message = "Invite text message sent.";
	            // go online if not already
	            startService(ServiceType.REMOTE);
	            break;
	        case SmsManager.RESULT_ERROR_NO_SERVICE:
	            message = "Failed to send invite text message, no coverage.";
	            break;
	        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
	        default:
	        	message = "Failed to send invite text message.";
	            break;
	        }
	        if (message != null) {
			    Toast.makeText(MeetUp.this, message,
						Toast.LENGTH_LONG).show();
		    }
	    }
    }


	@Override
	protected Dialog onCreateDialog(int id) {
		AbstractPrompt prompt = null;
		switch (id) {
		case DIALOG_PROMPT_GPS:
			prompt = new GPSPrompt(id,
					this, this, true);
			break;
		case DIALOG_PROMPT_NAME:
			prompt = new EnterTextPrompt(id,
					this, this, true, "Enter your name:", AppConfig.getInstance().getName(),
					MAX_NAME_LENGTH);
			break;
		case DIALOG_PROMPT_REMEMBER_DESC:
			prompt = new EnterTextPrompt(id,
					this, this, false, "Enter a description of the place:", null,
					MAX_NAME_LENGTH);
			break;
		case DIALOG_PROMPT_SELECT_PERSON:
			List<PersonLocation> locations = new ArrayList<PersonLocation>();
			
			if (Locations.getAllLocations() != null) {
				locations.addAll(Locations.getAllLocations());
			}
			if (AppConfig.getInstance().getRememberedLocations() != null) {
				locations.addAll(AppConfig.getInstance().getRememberedLocations());
			}
			prompt = new SelectPersonPrompt(id,
					this, this, false, locations,
					nameFormatter);
			break;
		case DIALOG_PROMPT_CONTINUE_SESSION:
			prompt = new YesNoPrompt(id, this, this, true, "Continue last session?", null); 
			break;
		case DIALOG_PROMPT_CONFIRM_INVITE:
			if (inviteContact != null) {
				String name = inviteContact.getName() != null ? "to " + inviteContact.getName() : "";
				prompt = new YesNoPrompt(id, this, this, false,
						"Send text message invite " + name + "?",
						"(This person must have an Android phone)");
			}
			break;
		case DIALOG_PROMPT_TEST_MODE:
			prompt = new EnterTextPrompt(id,
					this, this, false, "Enter test session id", null,
					MAX_NAME_LENGTH);
			break;
		case DIALOG_PROMPT_INSTALL_NAV:
			prompt = new AlertPrompt(id, this, this, false,
					"Please install Google Navigation to use this feature.", null);
			break;
		case DIALOG_PROMPT_EXIT_APP:
			prompt = new YesNoPrompt(id, this, this, false, "Exit MeetUp?", null); 
			break;
		case DIALOG_PROMPT_CHANGE_VIEW:
			prompt = new ChangeViewPrompt(id, this, this, false, viewType);
			break;
		case DIALOG_HELP:
			prompt = new AlertPrompt(id, this, this, false,
					null, "To invite people to meet up click menu then select Invite.");
		}
		if (prompt != null) {
			return prompt.create();
		}
		return null;
	}

	public void onDismiss(int id, AbstractPrompt prompt, ReturnCode code) {
		
		if (code == ReturnCode.CANCEL &&
			prompt.isExitIfCancelled()) {
			finish();
		}
		else {
			switch (id) {
				case DIALOG_PROMPT_NAME:
					if (code != ReturnCode.CANCEL) {
						EnterTextPrompt textPrompt = (EnterTextPrompt)prompt;
						AppConfig.getInstance().setName(textPrompt.getText());
						activityStateMachine(ActivityState.STATE_NAME_ENTERED);
					}
				break;
				case DIALOG_PROMPT_GPS:
					if (code != ReturnCode.CANCEL) {
						//dismissDialog(DIALOG_PROMPT_GPS);
						//activityStateMachine(ActivityState.STATE_GPS_PROMPT_CLOSED);
						
						Intent intent = new Intent(
			       				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			        	
			        	startActivityForResult(intent, GPS_RESULT);
					}
				break;
				case DIALOG_PROMPT_REMEMBER_DESC:
					EnterTextPrompt textPrompt = (EnterTextPrompt)prompt;
					
					if (code != ReturnCode.CANCEL) {
						// add location to list of remembered items
						AppConfig.getInstance().addRememberedLocation(new PersonLocation(
								AppConfig.getInstance().getSessionId(),
								UUIDUtils.generateUUID(),
								textPrompt.getText(),
								selectedLocation.getLatitude(),
								selectedLocation.getLongitude(), true));
						rememberedOverlay.setLocations(AppConfig.getInstance().getRememberedLocations());
					}
				break;
				case DIALOG_PROMPT_SELECT_PERSON:
					// zoom to selected person location
					if (code != ReturnCode.CANCEL) {
						SelectPersonPrompt personPrompt = (SelectPersonPrompt)prompt;
						
						centrePerson = personPrompt.getSelectedPerson();
						
						setCentreMode(personPrompt.getCentreMode(), true);
					}
				break;
				case DIALOG_PROMPT_CONTINUE_SESSION:
					if (code == ReturnCode.POSITIVE) {
						activityStateMachine(ActivityState.STATE_CONTINUE_LAST_SESSION);
					} else if (code == ReturnCode.NEGATIVE) {
						activityStateMachine(ActivityState.STATE_START_NEW_SESSION);
					}
				break;
				case DIALOG_PROMPT_CONFIRM_INVITE:
					if (code == ReturnCode.POSITIVE) {
						try {
							InviteManager.sendInvite(this, inviteContact,
									AppConfig.getInstance().getSessionId());
						} catch (Exception e) {
							Toast.makeText(this, "Failed to send invite text.",
									Toast.LENGTH_LONG).show();
						}
					} else if (code == ReturnCode.NEGATIVE) {
						
					}
				break;
				case DIALOG_PROMPT_TEST_MODE:
					if (code == ReturnCode.POSITIVE) {
						EnterTextPrompt sessionPrompt = (EnterTextPrompt)prompt;
						setNewSessionId(sessionPrompt.getText());
					} 
					activityStateMachine(ActivityState.STATE_PROMPT_NAME_ENTRY);
				break;
				case DIALOG_PROMPT_EXIT_APP:
					NavigationHandler handler = new NavigationHandler(this);
			    	handler.navigateTo(selectedLocation, navWalking);
					if (code == ReturnCode.POSITIVE) {
						finish();
					} else {
						activityStateMachine(currentState);
					}
				break;
				case DIALOG_PROMPT_CHANGE_VIEW:
					if (code == ReturnCode.POSITIVE) {
						ChangeViewPrompt changePrompt = (ChangeViewPrompt)prompt;
						
						setViewType(changePrompt.getViewType());
					}
				break;
			}
			
			// ensure prompt gets recreated from scratch
			removeDialog(id);
		}
	}
	
	private void vibrate() {
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		if (v != null) {
			v.vibrate(300);
		}
	}
	
	private int getScreenOrientation() {
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

		if (display != null) {
			return display.getOrientation();
		}
		return -1;
	}
	
	private void setCentreMode(CentreMode centreMode, boolean updateMap) {
		if (centreMode != null) {
			MeetUp.centreMode = centreMode;
			
			// enable zoom controls if automatic zooming disabled
			if (mapView != null) {
				mapView.setBuiltInZoomControls(centreMode == CentreMode.DISABLED);
			}
			
			if (updateMap) {
				centreMap(centreMode);
			}
		}
	}
	
	private void centreMap(CentreMode centreMode) {
		if (centreMode != null) {
			List<PersonLocation> locations = new ArrayList<PersonLocation>();
			
			if (Locations.getAllLocations() != null) {
				locations.addAll(Locations.getAllLocations());
			}
			if (AppConfig.getInstance().getRememberedLocations() != null) {
				locations.addAll(AppConfig.getInstance().getRememberedLocations());
			}
			
			int orientation = getScreenOrientation();
			
			switch (centreMode) {
			case EVERYONE:
				MapZoomController.zoomToMiddle(mapView, locations, viewType, orientation);
				break;
			case SINGLE_PERSON:
				// single person selected - re-read from list 
				if (centrePerson != null) {
					PersonLocation centre = Locations.findLocationInList(locations, centrePerson);
					
					if (centre != null) {
						MapZoomController.zoomToMiddle(mapView, centre, viewType, orientation);
					}
				}
				break;
			case DISABLED:
				// nothing to do
				break;
			}
		}
	}
	
	private void setViewType(ViewType viewType) {
		if (viewType != null) {
			MeetUp.viewType = viewType;
		
			if (mapView != null) {
				switch (viewType) {
				case SATELLITE:
					mapView.setSatellite(true);
					mapView.setStreetView(false);
					break;
				case STREET:
					mapView.setSatellite(false);
					mapView.setStreetView(true);
					break;
				}
			}
		}
	}
	
	public void onLocationTapped(PersonLocation location) {
		selectedLocation = location;
    	openContextMenu(mapView);
	}
	
	private class IntentReceiver extends BroadcastReceiver {
		  @Override
		  public void onReceive(Context context, Intent intent) {
		    // Handle reciever
		    String action = intent.getAction();

		    if (action.equals(ACTION_MY_LOCATION_UPDATE)) {
		    	myLocationChanged();
		    } else if (action.equals(ACTION_LOCATIONS_UPDATE)) {
		    	locationsChanged();
		    }
		}
	}
}