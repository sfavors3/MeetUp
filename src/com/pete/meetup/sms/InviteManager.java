package com.pete.meetup.sms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.SmsManager;
import android.util.Log;

import com.pete.meetup.MeetUp;

public class InviteManager {
	
	public static final String PARAM_SESSION = "s";
	public static final String ACTION_SMS_SENT = "com.pete.meetup.SMS_SENT_ACTION";
	
	private static final String INTENT_URI 		= "http://www.tiny.cc/meetupapp/go?";//"http://www.meetupapp.co.uk/go?";
	private static final int 	MAX_SMS_LENGTH 	= 160;

	public static void sendInvite(Context context, PhoneContact contact, String sessionId) {
		if (contact != null &&
			contact.getNumber() != null) {
			String text = buildMessageText(sessionId);
			
			// register to receive sent sms status
			PendingIntent sentIntent = PendingIntent.getBroadcast(
					context, 0, new Intent(ACTION_SMS_SENT), 0);
			
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(contact.getNumber(), null, text, sentIntent, null);
		}
	}
	
	public static PhoneContact readContact(String contactId, Context context) {
		Cursor c = null;
		try {
			c = context.getContentResolver().query( 
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					new String[] {Phone.NUMBER, Phone.DISPLAY_NAME},
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId, null, null); 
			while (c.moveToNext()) {
				String name = c.getString(c.getColumnIndex( ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)); 
				String number = c.getString(c.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
				
				return new PhoneContact(number, name);
			}
			return null;
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}
	
	private static String buildMessageText(String sessionId) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("I would like to use MeetUp to find out where you are");
		sb.append("\nGet MeetUp from the Market then click the link");
		sb.append("\n" + INTENT_URI + PARAM_SESSION + "=" + sessionId);

		if (sb.length() > MAX_SMS_LENGTH) {
			// TODO: throw an exception
			Log.e(MeetUp.LOG_TAG, "Text message too long");
			throw new RuntimeException("Text longer than SMS limit");
		}
        return sb.toString();
	}
	
	//is the following any good?
	/*
	private void count() {
		
I would like to use MeetUp to find out where you are 53
\nGet Meetup from the MarketPlace then click the link 53
\nhttp://www.tiny.cc/meetupapp/go?s= 36
+ 22
	}
	*///= 160
}
