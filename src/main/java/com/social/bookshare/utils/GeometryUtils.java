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
	public static double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
		double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + 
                      Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        
        return dist * 60 * 1.1515 * 1609.344; // 미터 단위 변환
	}
	
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0); 
	}
	
    private static double rad2deg(double rad) {
    	return (rad * 180 / Math.PI);
    }
}
