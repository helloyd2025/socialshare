package com.social.bookshare.repository;

import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.social.bookshare.domain.Book;
import com.social.bookshare.domain.Book.Kdc;
import com.social.bookshare.dto.response.BookSearchResult;

public interface BookRepository extends JpaRepository<Book, Long> {

	public Optional<Book> findByIsbn13(String isbn);
	
	@Query(value = "SELECT DISTINCT new com.social.bookshare.dto.BookSearchResult("
			+ "b.title, b.volume, b.className, b.author, b.publisher, b.isbn13, b.imageUrl, FALSE)"
			+ " FROM Book b"
			+ " WHERE b.isbn13 = :isbn13"
			+ "	OR ("
			+ "		(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))"
			+ "		AND (:volume IS NULL OR b.volume = :volume)"
			+ "		AND (:className IS NULL OR b.className = :className)"
			+ " 	AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')))"
			+ " 	AND (:publisher IS NULL OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :publisher, '%')))"
			+ "		AND (:title IS NOT NULL OR :author IS NOT NULL OR :publisher IS NOT NULL)"
			+ "	)"
			+ " ORDER BY b.title ASC")
	public List<BookSearchResult> searchPrivateBooks(
				@Param("isbn13") String isbn13,
				@Param("title") String title,
				@Param("volume") Short volume,
				@Param("className") Kdc className,
		        @Param("author") String author, 
		        @Param("publisher") String publisher
	        );
	
	@Query(value = "SELECT DISTINCT new com.social.bookshare.dto.BookSearchResult("
			+ "b.title, b.volume, b.className, b.author, b.publisher, b.isbn13, b.imageUrl, FALSE)"
			+ " FROM Book b"
			+ " WHERE b.isbn13 = :isbn13"
			+ "	OR ("
			+ "		(:title IS NULL OR LOWER(b.title) = LOWER(:title))"
			+ "		AND (:volume IS NULL OR b.volume = :volume)"
			+ "		AND (:className IS NULL OR b.className = :className)"
			+ " 	AND (:author IS NULL OR LOWER(b.author) = LOWER(:author))"
			+ " 	AND (:publisher IS NULL OR LOWER(b.publisher) = LOWER(:publisher))"
			+ "		AND (:title IS NOT NULL OR :author IS NOT NULL OR :publisher IS NOT NULL)"
			+ "	)"
			+ " ORDER BY b.title ASC")
	public List<BookSearchResult> searchPrivateBooksStrictly(
				@Param("isbn13") String isbn13,
				@Param("title") String title,
				@Param("volume") Short volume,
				@Param("className") Kdc className,
		        @Param("author") String author, 
		        @Param("publisher") String publisher
	        );
	
	public interface BookLocationProjection {
	    String getLabel();
	    String getAddress();
	    Double getLon();
	    Double getLat();
	    Double getDistance();
	}

	@Query(value = "SELECT ul.label as label, ul.address as address,"
			+ " ST_X(ul.location) as lon, ST_Y(ul.location) as lat,"
			+ " ST_DistanceSphere(ul.location, :userPoint) / 1000.0 as distance"
			+ " FROM user_books ub"
			+ " JOIN user_locations ul ON ub.location_id = ul.id"
			+ " WHERE ub.isbn13 = :isbn13"
			+ " AND ub.status = 'AVAILABLE'"
			+ "	AND ub.loaner_id IS NULL"
			+ " AND ul.is_active IS TRUE"
			+ " ORDER BY distance ASC"
			+ " LIMIT :pageSize", 
			nativeQuery = true)
	public List<BookLocationProjection> findPrivateBookLocationsWithDistance(
				@Param("isbn13") String isbn13, 
	            @Param("userPoint") Point userPoint,
	            @Param("pageSize") int pageSize
			);
}