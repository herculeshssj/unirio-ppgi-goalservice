/*
 * Copyright (c) 2005 National University of Ireland, Galway
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  
 */

package ie.deri.wsmx.core.management;

import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;

import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.OpenType;

/**
 * Contains common utilities
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/ManagementUtil.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.9 $ $Date: 2005-09-18 15:59:13 $
 */
public class ManagementUtil {
	// contains all date and date time format instances
	// for the current locale
	private static final DateFormat[] allFormats = new DateFormat[] {
			DateFormat.getDateInstance(),
			DateFormat.getTimeInstance(),
			DateFormat.getDateTimeInstance(),
			// first pure date format
			DateFormat.getDateInstance(DateFormat.SHORT),
			DateFormat.getDateInstance(DateFormat.MEDIUM),
			DateFormat.getDateInstance(DateFormat.LONG),
			DateFormat.getDateInstance(DateFormat.FULL),
			// pure time format
			DateFormat.getTimeInstance(DateFormat.SHORT),
			DateFormat.getTimeInstance(DateFormat.MEDIUM),
			DateFormat.getTimeInstance(DateFormat.LONG),
			DateFormat.getTimeInstance(DateFormat.FULL),
			// combinations
			DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT),
			DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM),
			DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG),
			DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL),

			DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT),
			DateFormat
					.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM),
			DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG),
			DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL),

			DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT),
			DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM),
			DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG),
			DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.FULL),

			DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT),
			DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM),
			DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG),
			DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL) };

	private static final String[] BASIC_TYPES = new String[] { "int", "long",
			"short", "byte", "float", "double", "boolean" };

	/**
	 * Creates a parameter object of the given type containing a given value.
	 * If the type is unknown null is returned
	 *
	 * @param parameterType  Indicates the type of the parameter, for instance java.lang.String
	 * @param parameterValue The value of the parameter as a String
	 * @return an object of parameterType type and value parameterValue or null if the type is unknown
	 * @throws Thrown in case there is a data conversion error
	 */
	public static Object createParameterValue(String parameterType,
			String parameterValue) throws Exception {
		if (parameterType.equals("java.lang.String")) {
			return parameterValue;
		} else if (parameterType.equals("java.lang.Integer")
				|| parameterType.equals("int")) {
			return Integer.valueOf(parameterValue);
		} else if (parameterType.equals("java.lang.Long")
				|| parameterType.equals("long")) {
			return Long.valueOf(parameterValue);
		} else if (parameterType.equals("java.lang.Short")
				|| parameterType.equals("short")) {
			return Short.valueOf(parameterValue);
		} else if (parameterType.equals("java.lang.Byte")
				|| parameterType.equals("byte")) {
			return Byte.valueOf(parameterValue);
		} else if (parameterType.equals("java.lang.Float")
				|| parameterType.equals("float")) {
			return Float.valueOf(parameterValue);
		}
		// changed java.lang.dobule to java.lang.double bronwen
		else if (parameterType.equals("java.lang.Double")
				|| parameterType.equals("double")) {
			return Double.valueOf(parameterValue);
		} else if (parameterType.equals("java.lang.Boolean")
				|| parameterType.equals("boolean")) {
			return Boolean.valueOf(parameterValue);
		} else if (parameterType.equals("java.lang.Void")) {
			return Void.TYPE;
		} else if (parameterType.equals("java.util.Date")) {
			// this is tricky since Date can be written in many formats
			// will use the Date format with current locale and several
			// different formats
			Date value = null;
			for (int i = 0; i < allFormats.length; i++) {
				synchronized (allFormats[i]) {
					try {
						System.out
								.println(parameterValue + " " + allFormats[i]);
						value = allFormats[i].parse(parameterValue);
						// if succeful then break
						break;
					} catch (ParseException e) {
						// ignore, the format wasn't appropriate
					}
				}
			}
			if (value == null) {
				throw new ParseException("Not possible to parse", 0);
			}
			return value;
		} else if (parameterType.equals("java.lang.Number")) {
			Number value = null;
			// try first as a long
			try {
				value = Long.valueOf(parameterValue);
			} catch (NumberFormatException e) {
				//ignore
			}
			// if not try as a double
			if (value == null) {
				try {
					value = Double.valueOf(parameterValue);
				} catch (NumberFormatException e) {
					//ignore
				}
			}
			if (value == null) {
				throw new NumberFormatException("Not possible to parse");
			}
			return value;
		}
		if (parameterType.equals("java.lang.Character")
				|| parameterType.equals("char")) {
			if (parameterValue.length() > 0) {
				return new Character(parameterValue.charAt(0));
			}
			throw new NumberFormatException(
					"Can not initialize Character from empty String");
		}
		// tests whether the classes have a single string parameter value
		// constructor. That covers the classes
		// javax.management.ObjectName
		// java.math.BigInteger
		// java.math.BigDecimal

		Class cls = null;
		java.lang.reflect.Constructor ctor = null;
		try {
			cls = Class.forName(parameterType);
			ctor = cls.getConstructor(new Class[] { String.class });
			return ctor.newInstance(new Object[] { parameterValue });
		} catch (ClassNotFoundException cnfe) {
			// Can not find class. Not in our ClassLoader?
			/** @todo Ask the MBeanServer to instantiate this class??? */
			throw new IllegalArgumentException("Invalid parameter type: "
					+ parameterType);
		} catch (NoSuchMethodException nsme) {
			// No public String constructor.
			throw new IllegalArgumentException("Invalid parameter type: "
					+ parameterType);
		} catch (Exception ex) {
			// Constructor might have thrown an exception?
			// Security Exception ?
			// IllegalAccessException? .... etc.
			// Just rethrow. We can do little here <shrug>
			/** @todo Log the exception */
			throw ex;
		}
	}

	/**
	 * Checks if the given type is primitive of can be initialized from String.<br>
	 * This is done by trying to load the class and checking if there is a public String
	 * only constructor.
	 *
	 * @param parameterType Indicates the type of the parameter, for instance java.lang.String
	 * @return true if the type is primitive or String initializable
	 * @throws Thrown in case there is a data conversion error
	 */
	public static boolean canCreateParameterValue(String parameterType) {
		int count = OpenType.ALLOWED_CLASSNAMES_LIST.size();
		for (int i = 0; i < count; i++) {
			if (OpenType.ALLOWED_CLASSNAMES_LIST.get(i).equals(parameterType)) {
				return true;
			}
		}
		count = BASIC_TYPES.length;
		for (int i = 0; i < count; i++) {
			if (BASIC_TYPES[i].equals(parameterType)) {
				return true;
			}
		}

		Class cls = null;
		try {
			cls = Class.forName(parameterType);
			cls.getConstructor(new Class[] { String.class });
			//we can load the class and it has a public String constructor
			return true;
		} catch (ClassNotFoundException cnfe) {
			//can not find class
			//TODO ask the MBeanServer to instantiate this class
			return false;
		} catch (NoSuchMethodException nsme) {
			//no public String constructor
			return false;
		} catch (Exception ex) {
			//TODO Log the exception
			return false;
		}
	}

	public static Comparator<ObjectName> createObjectNameComparator() {
		return new ToStringComparator<ObjectName>();
	}

	public static Comparator<ObjectInstance> createObjectInstanceComparator() {
		return new ObjectInstanceComparator();
	}

	public static Comparator<Constructor> createConstructorComparator() {
		return new ConstructorComparator();
	}

	public static Comparator createClassComparator() {
		return new ToStringComparator();
	}

	private static class ToStringComparator<E> implements Comparator<E> {
		public int compare(E o1, E o2) {
			return o1.toString().compareTo(o2.toString());
		}
	}

	private static class ObjectInstanceComparator implements Comparator<ObjectInstance> {
		private ToStringComparator<ObjectName> comp = new ToStringComparator<ObjectName>();
		public int compare(ObjectInstance o1, ObjectInstance o2) {
			return comp.compare(o1.getObjectName(), o2.getObjectName());
		}
	}

	private static class ConstructorComparator implements Comparator<Constructor> {
		public int compare(Constructor c1, Constructor c2) {
//			 sort them by the parameter types;
			Class[] params1 = c1.getParameterTypes();
			Class[] params2 = c2.getParameterTypes();
			if (params1.length == params2.length) {
				for (int i = 0; i < params1.length; i++) {
					if (!params1[i].equals(params2[i])) {
						return params2[i].toString().compareTo(
								params1[i].toString());
					}
				}
				return 0;
			}
			return params1.length - params2.length;
		}
	}

}
