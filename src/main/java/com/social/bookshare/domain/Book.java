package com.social.bookshare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "books")
public class Book {
	
	protected Book() {}
	
	public Book(Builder builder) {
		this.id = builder.id;
		this.isbn13 = builder.isbn13;
		this.title = builder.title;
		this.volume = builder.volume;
		this.className = builder.className;
        this.author = builder.author;
        this.publisher = builder.publisher;
        this.imageURL = builder.imageURL;
	}
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 13, unique = true, nullable = false)
    @Pattern(regexp = "^(\\d{10}|\\d{13}|U[0-9A-F]{12})$")
    private String isbn13;	// Unofficial books start with 'U' (e.g. U3192...)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;
    
    @Min(value = 1)
    @Column(nullable = false)
    private Short volume = 1;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "class_nm", nullable = false, length = 10)
    private Kdc className;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String author;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String publisher;
    
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageURL;
    
    public enum Kdc {
    	GENERALIA, PHILOSOPHY, RELIGION, 
    	SOCIAL, NATURAL, TECHNOLOGY, 
    	ARTS, LANGUAGE, LITERATURE, HISTORY
    }
    
    // Getters
    public Long getId() { return id; }
    public String getIsbn13() { return isbn13; }
	public String getTitle() { return title; }
	public Short getVolume() { return volume; }
	public Kdc getClassName() { return className; }
	public String getAuthor() { return author; }
	public String getPublisher() { return publisher; }
	public String getImageURL() { return imageURL; }
	
	// Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
    	private Long id;
    	private String isbn13;
        private String title;
        private Short volume;
        private Kdc className;
        private String author;
        private String publisher;
        private String imageURL;
        
        public Builder id(Long id) {
        	this.id = id;
        	return this;
        }
        
        public Builder isbn13(String isbn13) {
            this.isbn13 = isbn13;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder volume(Short volume) {
            this.volume = volume;
            return this;
        }
        
        public Builder className(String className) {
            this.className = Kdc.valueOf(className.toUpperCase());
            return this;
        }
        
        public Builder className(Kdc className) {
            this.className = className;
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
        
        public Builder imageURL(String imageURL) {
            this.imageURL = imageURL;
            return this;
        }

        public Book build() {
            return new Book(this);
        }
    }
}