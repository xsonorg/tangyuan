package org.xson.tangyuan;

public class Version {

	public final static int	MAJOR		= 1;
	public final static int	MINOR		= 0;
	public final static int	REVISION	= 3;

	public static String getVersionNumber() {
		return MAJOR + "." + MINOR + "." + REVISION;
	}

}
