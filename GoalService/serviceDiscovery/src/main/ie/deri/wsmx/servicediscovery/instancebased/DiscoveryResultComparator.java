package ie.deri.wsmx.servicediscovery.instancebased;

import java.util.Comparator;
 
public class DiscoveryResultComparator implements Comparator<DiscoveryResult> {

	    // Comparator interface requires defining compare method.
//	    public int compare(File filea, File fileb) {
//	        //... Sort directories before files,
//	        //    otherwise alphabetical ignoring case.
//	        if (filea.isDirectory() && !fileb.isDirectory()) {
//	            return -1;
//
//	        } else if (!filea.isDirectory() && fileb.isDirectory()) {
//	            return 1;
//
//	        } else {
//	            return filea.getName().compareToIgnoreCase(fileb.getName());
//	        }
//	    }

	    /*
	     * 1  arg0 > arg1
	     * 0   arg0 = arg1
	     * -1   arg0 < arg1
	     * 
	     */
		public int compare(DiscoveryResult arg0, DiscoveryResult arg1) {
			if ((arg0.entails) && (!arg1.entails))
				return -1;
			else if ((!arg0.entails) && (arg1.entails))
				return 1;
			else if ((!arg0.entails) && (!arg1.entails))
				return 0;

			//both results entail			
			String rankingCriteria = arg0.rankingCriteria;
			
			Float arg0F = arg0.getVariableValue(rankingCriteria);
			Float arg1F = arg1.getVariableValue(rankingCriteria);
			
			if (arg0F > arg1F)
				return 1;
			else if (arg0F < arg1F)
				return -1;

			return 0;
			
//	        if (filea.isDirectory() && !fileb.isDirectory()) {
//	            return -1;
//
//	        } else if (!filea.isDirectory() && fileb.isDirectory()) {
//	            return 1;
//
//	        } else {
//	            return filea.getName().compareToIgnoreCase(fileb.getName());
//	        }
		}
	}