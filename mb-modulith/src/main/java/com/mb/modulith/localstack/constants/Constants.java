package com.mb.modulith.localstack.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    /* METRICS */
    public static final String START_SCHEDULE_JOB = "startScheduleJob";
    public static final String STOP_SCHEDULE_JOB = "stopScheduleJob";
    public static final String SIZE = "size";
    public static final String UNPROCESSED_DELETE_ITEMS_FOR_TABLE = "unprocessedDeleteItemsForTable";
    public static final String FAILED_TO_PROCESS_PUBLISHER = "failedToProcessPublisher";

    /* METRIC TAGS */
    public static final String EXCEPTION_TAG = "exception";
    public static final String SIZE_TAG = "size";

    public static final String LOCALSTACK_DOCKER_IMAGE = "localstack/localstack:2026.10.4";
}
