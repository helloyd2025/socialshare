package com.social.bookshare.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BookSearchResponse {
	private Response response;

	public Response getResponse() { return response; }
	public void setResponse(Response response) { this.response = response; }

	public static class Response {
        private List<Doc> docs;

		public List<Doc> getDocs() { return docs; }
		public void setDocs(List<Doc> docs) { this.docs = docs; }
    }
	
	public static class Doc {
        private DocInfo doc;

		public DocInfo getDoc() { return doc; }
		public void setDoc(DocInfo doc) { this.doc = doc; }
    }
	
	public static class DocInfo {
		@JsonProperty("bookname")
        private String bookName;

        @JsonProperty("class_nm")
        private String className;

        private String volume;
        private String authors;
        private String publisher;
        private String isbn13;
        private String bookImageURL;
        
        public String getBookName() { return bookName; }
		public String getClassName() { return className; }
		public short getVolume() { return Short.parseShort(volume); }
		public String getAuthors() { return authors; }
		public String getPublisher() { return publisher; }
		public String getIsbn13() { return isbn13; }
		public String getBookImageURL() { return bookImageURL; }
		
		public void setBookName(String bookName) { this.bookName = bookName; }
		public void setClassName(String className) { this.className = className; }
		public void setVolume(short volume) { this.volume = String.valueOf(volume); }
		public void setAuthors(String authors) { this.authors = authors; }
		public void setPublisher(String publisher) { this.publisher = publisher; }
		public void setIsbn13(String isbn13) { this.isbn13 = isbn13; }
		public void setBookImageURL(String bookImageURL) { this.bookImageURL = bookImageURL; }
    }
}
