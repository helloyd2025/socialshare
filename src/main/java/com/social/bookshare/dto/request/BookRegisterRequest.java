package com.social.bookshare.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.social.bookshare.domain.Location;
import com.social.bookshare.dto.response.BookSearchResult;

public class BookRegisterRequest {
	
	public BookRegisterRequest() {}
	
	public BookRegisterRequest(
			String isbn13, String title, short volume, String className, String author, String publisher, String imageUrl, String comment,
			double userLat, double userLon, String label, String address, String status) {
		this.isbn13 = isbn13;
		this.title = title;
		this.volume = volume;
		this.className = className;
		this.author = author;
		this.publisher = publisher;
		this.imageUrl = imageUrl;
		this.comment = comment;
		this.userLat = userLat;
		this.userLon = userLon;
		this.label = label;
		this.address = address;
		this.status = status;
	}

	// book
	private String isbn13;
	private String title;
	private Short volume;
	
	@JsonProperty("class_nm")
	private String className;
	
	private String author;
	private String publisher;
	private String imageUrl;
	private String comment;
	
	// location
	private Double userLat;
	private Double userLon;
	private String label;
	private String address;
	
	// user
	private String status;
	
	// server side set
	private Long bookId;
	private Long locationId;
	private Boolean isFirstBook;
	
	public boolean isEitherOr() { // must include only one
		return (title == null) ^ (isbn13 == null);
	}
	
	public void setBookSearchResult(BookSearchResult bookResult) {
		this.isbn13 = bookResult.getIsbn13();
		this.title = bookResult.getTitle();
		this.volume = bookResult.getVolume();
		this.className = String.valueOf(bookResult.getClassName());
		this.author = bookResult.getAuthor();
		this.publisher = bookResult.getPublisher();
		this.imageUrl = bookResult.getImage();
		this.isFirstBook = bookResult.getFromLibrary(); // This is because searching DB before API here
	}
	
	// Getters
	public String getTitle() { return title; }
	public Short getVolume() { return volume; }
	public String getClassName() { return className; }
	public String getAuthor() { return author; }
	public String getPublisher() { return publisher; }
	public String getIsbn13() { return isbn13; }
	public String getImageUrl() { return imageUrl; }
	public String getComment() { return comment; }
	public Long getLocationId() { return locationId; }
	public Double getUserLat() { return userLat; }
	public Double getUserLon() { return userLon; }
	public String getLabel() { return label; }
	public String getAddress() { return address; }
	public String getStatus() { return status; }
	public Boolean getIsFirstBook() { return isFirstBook; }
	
	// Setters
	public void setIsbn13(String isbn13) { this.isbn13 = isbn13; } // to set fake ISBN for independent books
	public void setBookId(Long bookId) { this.bookId = bookId; }
	public void setLocationId(Long locationId) { this.locationId = locationId; }
}
