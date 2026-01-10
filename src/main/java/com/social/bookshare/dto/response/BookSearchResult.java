package com.social.bookshare.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.social.bookshare.domain.Book.Kdc;

public class BookSearchResult {

	public BookSearchResult(String title, short volume, String className, String author, 
			String publisher, String isbn13, String image, boolean fromLibrary) {
		this.title = title;
		this.volume = volume;
		this.className = Kdc.valueOf(className.toUpperCase());
		this.author = author;
		this.publisher = publisher;
		this.isbn13 = isbn13;
		this.image = image;
		this.fromLibrary = fromLibrary;
	}
	
	private BookSearchResult(Builder builder) {
		this.title = builder.title;
		this.volume = builder.volume;
		this.className = builder.className;
		this.author = builder.author;
		this.publisher = builder.publisher;
		this.isbn13 = builder.isbn13;
		this.image = builder.image;
		this.fromLibrary = builder.fromLibrary;
	}
	
    private final String title;
    private final short volume;
    
    @JsonProperty("class_nm")
    private final Kdc className;
    
    private final String author;
    private final String publisher;
	private final String isbn13;
    private final String image;
    
    @JsonProperty("from_library")
    private boolean fromLibrary; // LIBRARY or PRIVATE
    
    // Getters
    public String getTitle() { return title; }
    public short getVolume() { return volume; }
	public Kdc getClassName() { return className; }
	public String getAuthor() { return author; }
	public String getPublisher() { return publisher; }
	public String getIsbn13() { return isbn13; }
	public String getImage() { return image; }
	public boolean fromLibrary() { return fromLibrary; }
	
	//Builder
	public static Builder builder() {
        return new Builder();
    }
	
	public static class Builder {
		private String title;
		private short volume;
	    private Kdc className;
	    private String author;
	    private String publisher;
		private String isbn13;
	    private String image;
	    private boolean fromLibrary;
	    
	    public Builder title(String title) {
            this.title = title;
            return this;
        }
	    
	    public Builder volume(short volume) {
            this.volume = volume;
            return this;
        }
        
        public Builder className(String className) {
            this.className = Kdc.valueOf(className.toUpperCase());
            return this;
        }
	    
	    public Builder author(String author) {
            this.author = author;
            return this;
        }
	    
	    public Builder publisher(String publisher) {
            this.publisher = publisher;
            return this;
        }
	    
	    public Builder isbn13(String isbn13) {
            this.isbn13 = isbn13;
            return this;
        }
	    
	    public Builder image(String image) {
            this.image = image;
            return this;
        }
	    
	    public Builder fromLibrary(boolean fromLibrary) {
            this.fromLibrary = fromLibrary;
            return this;
        }
	    
	    public BookSearchResult build() {
            return new BookSearchResult(this);
        }
	}
}
