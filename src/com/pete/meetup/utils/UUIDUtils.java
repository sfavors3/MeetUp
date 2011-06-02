package com.pete.meetup.utils;

import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

public class UUIDUtils {

	public static final String generateUUID() {
		UUID uuid = UUID.randomUUID();
		
		byte[] uuidBytes = asByteArray(uuid);
		
		String uuidAsString =  new String(Base64.encodeBase64(uuidBytes));
		
		// remove 2 char padding at end of string
		uuidAsString = uuidAsString.substring(0, uuidAsString.length() -2);
		
		// remove any chars which could cause problems in a URL
		uuidAsString = uuidAsString.replace("+", "-").replace("/", "_");
		return uuidAsString;
	}
	
	private static byte[] asByteArray(UUID uuid) {

        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] buffer = new byte[16];

        for (int i = 0; i < 8; i++) {
                buffer[i] = (byte) (msb >>> 8 * (7 - i));
        }
        for (int i = 8; i < 16; i++) {
                buffer[i] = (byte) (lsb >>> 8 * (7 - i));
        }

        return buffer;
    }
}
