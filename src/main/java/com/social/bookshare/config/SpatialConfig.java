package com.social.bookshare.config;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpatialConfig {

	@Bean
	GeometryFactory geometryFactory() {
		// Set SRID 4326 (WGS84) as the global reference
        return new GeometryFactory(new PrecisionModel(), 4326);
	}
}
