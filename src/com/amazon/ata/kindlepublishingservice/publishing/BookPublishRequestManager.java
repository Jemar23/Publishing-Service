package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

// The BookPublishRequestManager should manage a collection of book publish requests
// ensuring that requests are processed in the same order that they are submitted.
// Collection Type -> Queue
public class BookPublishRequestManager {
    private Queue<BookPublishRequest> bookRequested;

    @Inject
    public BookPublishRequestManager(Queue<BookPublishRequest> linkedQueue) {
        this.bookRequested = linkedQueue;
    }

    public void addBookPublishRequest(BookPublishRequest bookPublishRequest) {
        bookRequested.add(bookPublishRequest);
    }

    public BookPublishRequest getBookPublishRequestToProcess() {
       // which retrieves the next BookPublishRequest in line for publishing and returns it.
        // If there are no requests to publish this should return null.
        // This will be called in a later task when we implement processing the book publish requests.
        return bookRequested.poll();
    }
}
