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
        } else {
        	return factory.createPoint(new Coordinate(lon, lat)); // lon-lat
        }
    }
	
	public static double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
		return GeometryUtils.calculateDistance(lon1, lat1, lon2, lat2);
	}
}
