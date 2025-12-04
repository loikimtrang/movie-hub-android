package com.movie_hub.android.data.model.api.request.collection;

import lombok.Data;

@Data
public class CollectionItemRequest {
    private Long collectionId;
    private String name;
    private Integer page;
    private Integer size;
    private Long offset;
    private Boolean paged;
    private Boolean unpaged;
    private Boolean randomData;
    private Integer status;
    private Integer style;
    private Integer type;

    private Boolean sortSorted;     // sort.sorted
    private Boolean sortUnsorted;   // sort.unsorted
}

