package com.movie_hub.android.data.model.api.response.room;

import lombok.Data;

@Data
public class ParticipantDto {
    private Long id;
    private AccountDto user;
    /** 1 = host, 0 = participant */
    private Integer role;
    private Integer state;
}
