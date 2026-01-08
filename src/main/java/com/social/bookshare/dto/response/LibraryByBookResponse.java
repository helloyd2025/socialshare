package com.social.bookshare.dto.response;

import java.util.List;

public class LibraryByBookResponse {
	private Response response;

	public Response getResponse() { return response; }
	public void setResponse(Response response) { this.response = response; }
	
	public static class Response {
        private List<Lib> libs;

		public List<Lib> getLibs() { return libs; }
		public void setLibs(List<Lib> libs) { this.libs = libs; }
    }
	
	public static class Lib {
        private LibInfo lib;

		public LibInfo getLib() { return lib; }
		public void setLib(LibInfo lib) { this.lib = lib; }
    }
	
	public static class LibInfo {
        private String libName;
        private String address;
        private String latitude;
        private String longitude;
        
        public String getLibName() { return libName; }
		public String getAddress() { return address; }
		public String getLatitude() { return latitude; }
		public String getLongitude() { return longitude; }
		
		public void setLibName(String libName) { this.libName = libName; }
		public void setAddress(String address) { this.address = address; }
		public void setLatitude(String latitude) { this.latitude = latitude; }
		public void setLongitude(String longitude) { this.longitude = longitude; }
    }
}
