package com.movie_hub.android.data.model.api.response.person;

import lombok.Data;
import java.util.List;

@Data
public class PersonResponse {
    private String avatarPath;
    private String bio;
    private String country;
    private String createdDate;
    private String dateOfBirth;
    private Integer gender;
    private Long id;
    private List<Integer> kinds;
    private String modifiedDate;
    private String name;
    private String otherName;
    private Integer status;
}
