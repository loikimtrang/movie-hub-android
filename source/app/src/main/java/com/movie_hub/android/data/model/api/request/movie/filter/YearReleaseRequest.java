package com.movie_hub.android.data.model.api.request.movie.filter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import lombok.Data;

@Data
public class YearReleaseRequest {
    private Integer releaseYear;
    private boolean isSelect;

    public static List<YearReleaseRequest> generateYearReleaseList() {
        List<YearReleaseRequest> yearList = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        for (int year = currentYear; year >= 1950; year--) {
            YearReleaseRequest item = new YearReleaseRequest();
            item.setReleaseYear(year);
            item.setSelect(false); // ban đầu chưa chọn gì
            yearList.add(item);
        }

        return yearList;
    }

}
