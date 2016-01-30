/* 
 * QoS Discovery Component
 * Copyright (C) 2006 Sebastian Gerlach
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package ch.epfl.qosdisc.operators;

/**
 * The ranking parameters for a specific concept.
 *
 * @author Sebastian Gerlach
 */
public class ConceptRankingInfo {
    
    /**
     * Preference weight. 
     */
    private double weight;
    
    /**
     * Matching threshold. 
     */
    private double threshold;
    
    /**
     * Default reputation score. 
     */
    private double defaultReputation;
    
    /**
     * The comparison operator to apply to this concept.
     */
    private String comparison;
    
    /**
     * Constructor.
     * 
     * @param w Preference weight.
     * @param t Matching threshold.
     * @param r Default reputation score.
     */
    public ConceptRankingInfo(double w, double t, double r, String c) {
        weight = w;
        threshold = t;
        defaultReputation = r;
        comparison = c;
    }

    /**
     * @return Returns the default reputation score.
     */
    public double getDefaultReputation() {
        return defaultReputation;
    }

    /**
     * @return Returns the matching threshold.
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * @return Returns the preference weight.
     */
    public double getWeight() {
        return weight;
    }
    
    /**
     *  @return Returns the comparison operator.
     */
    public String getComparison() {
    	return comparison;
    }
}
