package com.social.bookshare.dto.response;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.social.bookshare.domain.Book;
import com.social.bookshare.domain.Book.Kdc;
import com.social.bookshare.domain.Location;
import com.social.bookshare.domain.UserBook.Status;

public class UserBookResponse {
	
	public UserBookResponse(Builder builder) {
		Location location = builder.location;
		Book book = builder.book;
		
		this.id = builder.id;
		this.lon = location.getLocation().getX();
		this.lat = location.getLocation().getY();
		this.isbn13 = book.getIsbn13();
		this.title = book.getTitle();
		this.volume = book.getVolume();
		this.className = book.getClassName();
		this.author = book.getAuthor();
		this.publisher = book.getPublisher();
		this.imageURL = book.getImageURL();
		this.comment = builder.comment;
		this.status = builder.status;
		this.updatedAt = builder.updatedAt;
	}

	private Long id;
	private double lon;
	private double lat;
	private String isbn13;
	private String title;
	private Short volume;
	private Kdc className;
	private String author;
	private String publisher;
	private String imageURL;
	private String comment;
	private Status status;
	
	@JsonProperty("updated_at")
	private Timestamp updatedAt;
	
	// Getters
	public Long getId() { return id; }
	public double getLon() { return lon; }
	public double getLat() { return lat; }
	public String getIsbn13() { return isbn13; }
	public String getTitle() { return title; }
	public Short getVolume() { return volume; }
	public Kdc getClassName() { return className; }
	public String getAuthor() { return author; }
	public String getPublisher() { return publisher; }
	public String getImageURL() { return imageURL; }
	public String getComment() { return comment; }
	public Status getStatus() { return status; }
	public Timestamp getUpdatedAt() { return updatedAt; }
	
	//Builder
	public static Builder builder() {
        return new Builder();
    }
	
	public static class Builder {
		private Long id;
		private Location location;
		private Book book;
		private String comment;
		private Status status;
		private Timestamp updatedAt;
	    
	    public Builder id(Long id) {
            this.id = id;
            return this;
        }
	    
	    public Builder location(Location location) {
            this.location = location;
            return this;
        }
	    
	    public Builder book(Book book) {
            this.book = book;
            return this;
        }
	    
	    public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }
	    
	    public Builder status(Status status) {
            this.status = status;
            return this;
        }
	    
	    public Builder updatedAt(Timestamp updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
	    
	    public UserBookResponse build() {
            return new UserBookResponse(this);
        }
	}
}
