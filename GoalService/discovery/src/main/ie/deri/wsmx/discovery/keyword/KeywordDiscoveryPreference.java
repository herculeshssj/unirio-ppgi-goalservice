package ie.deri.wsmx.discovery.keyword;

/**
 * Copyright (c) 2004 DERI www.deri.org
 * Created on Dec 2, 2004
 * 
 * @author Ioan Toma
 * 
 **/
import org.wsmo.common.*;

public class KeywordDiscoveryPreference {
	final static int FULL_MATCH = 1;

	final static int PARTIAL_MATCH = 0;

	private IRI NFPAttribute;

	private int matchType;

	private double threshold;

	/**
	 * KeywordDiscoveryPreference Constructor
	 * 
	 * @param NFPattribute -
	 *            the NFP attribute used for matching (i.e. dc:Subject,...)
	 * @param matchType -
	 *            the type of match: FULL or PARTIAL
	 * @param threshold -
	 *            thereshold for PARTIAL MATCH selection 0 <= thereshold <= 1 ;
	 *            0 - accept all; 1 - accept nothing
	 */
	public KeywordDiscoveryPreference(IRI NFPAttribute, int matchType,
			double threshold) {
		this.NFPAttribute = NFPAttribute;
		if (matchType == PARTIAL_MATCH) {
			this.matchType = matchType;
			if ((threshold >= 0) && (threshold <= 1)) {
				this.threshold = threshold;
			} else {
				this.threshold = 0;
			}
		} else {
			this.matchType = FULL_MATCH;
			this.threshold = 0;
		}
	}

	/**
	 * @return Returns the matchType.
	 */
	public int getMatchType() {
		return matchType;
	}

	/**
	 * @param matchType
	 *            The matchType to set.
	 */
	public void setMatchType(int matchType) {
		this.matchType = matchType;
	}

	/**
	 * @return Returns the threshold.
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold
	 *            The threshold to set.
	 */
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * @return Returns the nFPAttribute.
	 */
	public IRI getNFPAttribute() {
		return NFPAttribute;
	}

	/**
	 * @param attribute
	 *            The nFPAttribute to set.
	 */
	public void setNFPAttribute(IRI attribute) {
		NFPAttribute = attribute;
	}
}
