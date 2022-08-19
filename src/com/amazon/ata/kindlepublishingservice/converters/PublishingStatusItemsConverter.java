package com.amazon.ata.kindlepublishingservice.converters;


import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;

import java.util.ArrayList;
import java.util.List;


public class PublishingStatusItemsConverter {

    public static List<PublishingStatusRecord> toPublishStatusList(List<PublishingStatusItem> publishingStatusItem) {
        List<PublishingStatusRecord> records = new ArrayList<>();

        for (PublishingStatusItem item : publishingStatusItem) {
            records.add(toPublishStatus(item));
        }
        return records;
    }

    public static PublishingStatusRecord toPublishStatus(PublishingStatusItem records) {
        return PublishingStatusRecord.builder()
                .withBookId(records.getBookId())
                .withStatus(records.getStatus().toString())
                .withStatusMessage(records.getStatusMessage())
                .build();
    }
}
