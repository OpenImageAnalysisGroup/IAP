/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * Created on 13.10.2004 by Christian Klukas
 */
package org;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class ObjectAttributeService implements HelperClass {
	
	public static String objectToStringMappingPossible_StringPrefix = "$STRINGOBJECT$";
	
	public static String getStringRepresentationFor(Object myInstance) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		// ObjectOutputStream oos = new ObjectOutputStream(os);
		// oos.writeObject(myInstance);
		byte[] obj = os.toByteArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < obj.length; i++) {
			String number;
			if (obj[i] < 0)
				number = new Integer(-obj[i] + Byte.MAX_VALUE).toString();
			else
				number = new Integer(obj[i]).toString();
			while (number.length() < 3)
				number = "0" + number;
			sb.append(number);
		}
		return objectToStringMappingPossible_StringPrefix + sb.toString();
	}
	
	/**
	 * @param string
	 * @return
	 * @throws InvalidClassException
	 */
	public static Object getObjectFromString(String serializedObject)
						throws InvalidClassException, IOException, ClassNotFoundException {
		if (serializedObject.startsWith(objectToStringMappingPossible_StringPrefix)) {
			serializedObject = serializedObject.substring(objectToStringMappingPossible_StringPrefix.length());
			List<Byte> bytes = new LinkedList<Byte>();
			while (serializedObject.length() >= 3) {
				String curNumber = serializedObject.substring(0, 3);
				Integer curVal = new Integer(curNumber);
				if (curVal.intValue() > Byte.MAX_VALUE)
					bytes.add(new Byte((byte) -(curVal.intValue() - Byte.MAX_VALUE)));
				else
					bytes.add(new Byte(curVal.byteValue()));
				serializedObject = serializedObject.substring(3);
			}
			byte[] buf = new byte[bytes.size()];
			int idx = 0;
			for (Iterator<Byte> it = bytes.iterator(); it.hasNext();) {
				buf[idx++] = ((Byte) it.next()).byteValue();
			}
			ByteArrayInputStream is = new ByteArrayInputStream(buf);
			ObjectInputStream ois = new ObjectInputStream(is);
			return ois.readObject();
		} else
			throw new InvalidClassException("The given String is not a valid serialized StringObjectAttribute!");
	}
	
	public static void main(String[] args) {
		System.out.println("Serialization - Test (IPK, CK)");
		
	}
	
	/**
	 * @param list
	 * @return
	 */
	public static String getStringPrefix(Object instance) {
		return objectToStringMappingPossible_StringPrefix + instance.getClass().getCanonicalName() + "$";
	}
	
	/**
	 * @param string
	 * @return
	 */
	public static Object createAndInitObjectFromString(String objstring) {
		String string = objstring;
		if (!string.startsWith(objectToStringMappingPossible_StringPrefix))
			return objstring;
		else {
			string = string.substring(objectToStringMappingPossible_StringPrefix.length());
			try {
				String cn = string.substring(0, string.indexOf("$"));
				Class<?> cl = Class.forName(cn);
				Object o = cl.newInstance();
				if (o instanceof ProvidesStringInitMethod) {
					((ProvidesStringInitMethod) o).fromString(objstring);
				}
				return o;
			} catch (ClassNotFoundException e) {
				ErrorMsg.addErrorMessage(e.getLocalizedMessage());
				return null;
			} catch (InstantiationException e) {
				ErrorMsg.addErrorMessage(e.getLocalizedMessage());
				return null;
			} catch (IllegalAccessException e) {
				ErrorMsg.addErrorMessage(e.getLocalizedMessage());
				return null;
			}
		}
	}
}
