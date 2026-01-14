package com.social.bookshare.utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
public class GeometryUtils {

	private static GeometryFactory factory;
	
	public GeometryUtils(GeometryFactory geometryFactory) {
        GeometryUtils.factory = geometryFactory;
    }
	
	public static Point createPoint(double lon, double lat) {
        if (factory == null) {
        	throw new IllegalStateException("GeometryFactory not initialized.");
        }
        return factory.createPoint(new Coordinate(lon, lat));
    }
	
	/** Calculate Haversine distance between two locations */
	public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
	    final int R = 6371; // kilometer

	    double dLat = Math.toRadians(lat2 - lat1);
	    double dLon = Math.toRadians(lon2 - lon1);

	    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
	               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	               Math.sin(dLon / 2) * Math.sin(dLon / 2);

	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    
	    return R * c * 1000; // meter
	}
}
