package com.amazon.ata.kindlepublishingservice.publishing;

/*
If the BookPublishRequestManager has no publishing requests the BookPublishTask should return immediately without taking action.
You will also need to update CatalogDao with new methods for the BookPublishTask to publish new books to our Kindle catalog.
 */

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;

import javax.inject.Inject;

// a new class BookPublishTask, that implements Runnable and processes a publish request from the BookPublishRequestManager.
public class BookPublishTask implements Runnable {
    private BookPublishRequestManager manager;
    private CatalogDao catalogDao;
    private PublishingStatusDao publishingStatusDao;

    @Inject
    public BookPublishTask(BookPublishRequestManager manager, CatalogDao catalogDao, PublishingStatusDao publishingStatusDao) {
        this.manager = manager;
        this.catalogDao = catalogDao;
        this.publishingStatusDao = publishingStatusDao;
    }

    /*
    If the BookPublishRequestManager has no publishing requests the
    BookPublishTask should return immediately without taking action.
     */

//    public BookPublishRequest getManager(BookPublishRequest request) {
//        PublishingStatusItem item;
//        if (request == null) {
//            return null;
//        } else {
//            manager.addBookPublishRequest(request);
//             item = publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(),
//                    PublishingRecordStatus.IN_PROGRESS,
//                    request.getBookId());
//        }
//        // format book -> return kindleFormattedBook
//        try {
//            manager.getBookPublishRequestToProcess();
//        } catch (Exception e) {
//             item = publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(),
//                    PublishingRecordStatus.FAILED,
//                    request.getBookId(), e.getMessage());
//        }
//
//        if (manager.getBookPublishRequestToProcess() != null) {
//            item = publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(),
//                    PublishingRecordStatus.SUCCESSFUL,
//                    request.getBookId());
//        }
//
//        // catalogDao.createOrUpdate() <- KindleFormattedBook
//
//        return BookPublishRequest.builder()
//                .withBookId(KindleFormatConverter.format(item));
//    }

    // Add a entry to the publishing status table with state IN_PROGRESS
    // convert book

    @Override
    public void run() {
        BookPublishRequest request = manager.getBookPublishRequestToProcess();
        if (request == null) {
            return;
        }
        publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(),
                PublishingRecordStatus.IN_PROGRESS,
                request.getBookId());

        KindleFormattedBook formatted = KindleFormatConverter.format(request);
        try {
             CatalogItemVersion formattedBook = catalogDao.createOrUpdate(formatted);

            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(),
                    PublishingRecordStatus.SUCCESSFUL,
                    formattedBook.getBookId());
        } catch (Exception e) {
            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(),
                    PublishingRecordStatus.FAILED,
                    request.getBookId(), e.getMessage());
        }
    }
}
