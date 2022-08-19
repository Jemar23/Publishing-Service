package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;

import com.amazon.ata.kindlepublishingservice.publishing.KindleFormatConverter;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

import java.util.List;
import javax.inject.Inject;

public class CatalogDao {

    private final DynamoDBMapper dynamoDbMapper;

    /**
     * Instantiates a new CatalogDao object.
     *
     * @param dynamoDbMapper The {@link DynamoDBMapper} used to interact with the catalog table.
     */
    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the specified book id.
     * Throws a BookNotFoundException if the latest version is not active or no version is found.
     * @param bookId Id associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        return book;
    }

    // Returns null if no version exists for the provided bookId
    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression()
            .withHashKeyValues(book)
            .withScanIndexForward(false)
            .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    // Soft delete, We don’t want to lose previous versions of the book that we have sold to customers.
    // Instead, we’ll mark the current version as inactive so that it can never be returned by the GetBook operation, essentially deleted
    public CatalogItemVersion removeBook(String bookId) {
        CatalogItemVersion removedBook = this.getBookFromCatalog(bookId);

        removedBook.setBookId(bookId);
        removedBook.setInactive(true);

        dynamoDbMapper.save(removedBook);
        return removedBook;
    }

    // Adds the new book to the CatalogItemVersion table
    // If the request is updating an existing book -> entry will use the same bookId
    // Increment version by 1
    // if addedBook exists -> increment version +1 -> prev version = setInactive(true)
    // Otherwise generate a new bookId with version of 1 -> KindlePublishingUtils
    public CatalogItemVersion createOrUpdate(KindleFormattedBook formattedBook) {
        if (formattedBook.getBookId() == null) {
            CatalogItemVersion newVersion = new CatalogItemVersion();
            newVersion.setAuthor(formattedBook.getAuthor());
            newVersion.setVersion(1);
            newVersion.setBookId(KindlePublishingUtils.generateBookId());
            newVersion.setTitle(formattedBook.getTitle());
            newVersion.setGenre(formattedBook.getGenre());
            newVersion.setText(formattedBook.getText());
            newVersion.setInactive(false);

            dynamoDbMapper.save(newVersion);
            return newVersion;
        }


        if (formattedBook.getBookId() != null) {
                CatalogItemVersion newBook = this.getLatestVersionOfBook(formattedBook.getBookId());
                removeBook(newBook.getBookId());
                newBook.setBookId(formattedBook.getBookId());
                newBook.setVersion(newBook.getVersion() + 1);
                newBook.setInactive(false);

                dynamoDbMapper.save(newBook);
                return newBook;
        }
        return null;
    }






   // to make sure we only attempt to publish books in our catalog, you will need to add a new void method validateBookExists to the CatalogDao class
    // which the activity can call to check if the provided bookId exists in the catalog
    // and throws a BookNotFoundException if it doesn’t.
    public void validateBookExists(String bookId) {
        CatalogItemVersion validate = this.getBookFromCatalog(bookId);

        if (validate == null) {
            throw new BookNotFoundException("Book not found");
        }
    }
}
