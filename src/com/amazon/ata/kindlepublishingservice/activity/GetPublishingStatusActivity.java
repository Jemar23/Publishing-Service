package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.converters.BookPublishRequestConverter;
import com.amazon.ata.kindlepublishingservice.converters.PublishingStatusItemsConverter;
import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.PublishingStatusNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;
import com.amazon.ata.kindlepublishingservice.models.requests.GetPublishingStatusRequest;
import com.amazon.ata.kindlepublishingservice.models.response.GetPublishingStatusResponse;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequest;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequestManager;


import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class GetPublishingStatusActivity {
    private PublishingStatusDao publishingStatusDao;
    private BookPublishRequestManager manager;

    @Inject
    public GetPublishingStatusActivity(PublishingStatusDao publishingStatusDao, BookPublishRequestManager manager) {
        this.publishingStatusDao = publishingStatusDao;
        this.manager = manager;
    }

    public GetPublishingStatusResponse execute(GetPublishingStatusRequest publishingStatusRequest) throws PublishingStatusNotFoundException {

        List<PublishingStatusItem> itemList;
        try {
            itemList = publishingStatusDao.getPublishingStatus(
                    publishingStatusRequest.getPublishingRecordId());
        } catch (PublishingStatusNotFoundException e) {
            throw new PublishingStatusNotFoundException("");
        }

//        if (publishingStatusRequest.getPublishingRecordId() == null) {
//            throw new PublishingStatusNotFoundException("");
//        }
//
//        List<PublishingStatusItem> itemList = new ArrayList<>();
//
//        List<PublishingStatusItem> itemLists = publishingStatusDao.getPublishingStatus(publishingStatusRequest.getPublishingRecordId());
//
//        try {
//            itemList.add(publishingStatusDao.setPublishingStatus(publishingStatusRequest.getPublishingRecordId(),
//                    PublishingRecordStatus.IN_PROGRESS,                    // <-- Status
//                    itemLists.get(0).getBookId()));                       // <-- Null Pointer thrown
//
//            itemList.add(publishingStatusDao.setPublishingStatus(publishingStatusRequest.getPublishingRecordId(),
//                    PublishingRecordStatus.SUCCESSFUL,                        // <-- Status
//                    itemLists.get(0).getBookId()));   // <-- Null Pointer thrown
//        } catch (PublishingStatusNotFoundException e) {
//            itemList.add(publishingStatusDao.setPublishingStatus(publishingStatusRequest.getPublishingRecordId(),
//                    PublishingRecordStatus.FAILED,                       // <-- Status
//                    itemLists.get(0).getBookId()));  // <-- Null Pointer thrown
//            throw new PublishingStatusNotFoundException("record not found");
//        }


        return GetPublishingStatusResponse.builder()
                .withPublishingStatusHistory(PublishingStatusItemsConverter.toPublishStatusList(itemList)) 
                .build();
    }
}
