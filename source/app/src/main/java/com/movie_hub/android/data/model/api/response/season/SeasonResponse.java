package com.movie_hub.android.data.model.api.response.season;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.movie_hub.android.data.model.api.response.video.VideoResponse;

import lombok.Data;

@Data
public class SeasonResponse implements Parcelable {
    private Long id;
    private String title;
    private String description;
    private String label;
    private Integer kind;
    private Integer ordering;
    private Integer status;
    private String releaseDate;
    private String createdDate;
    private String modifiedDate;
    private VideoResponse video;

    protected SeasonResponse(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        title = in.readString();
        description = in.readString();
        label = in.readString();
        if (in.readByte() == 0) {
            kind = null;
        } else {
            kind = in.readInt();
        }
        if (in.readByte() == 0) {
            ordering = null;
        } else {
            ordering = in.readInt();
        }
        if (in.readByte() == 0) {
            status = null;
        } else {
            status = in.readInt();
        }
        releaseDate = in.readString();
        createdDate = in.readString();
        modifiedDate = in.readString();
        video = in.readParcelable(VideoResponse.class.getClassLoader());
    }

    public static final Creator<SeasonResponse> CREATOR = new Creator<SeasonResponse>() {
        @Override
        public SeasonResponse createFromParcel(Parcel in) {
            return new SeasonResponse(in);
        }

        @Override
        public SeasonResponse[] newArray(int size) {
            return new SeasonResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(id);
        }
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(label);
        if (kind == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(kind);
        }
        if (ordering == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(ordering);
        }
        if (status == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(status);
        }
        parcel.writeString(releaseDate);
        parcel.writeString(createdDate);
        parcel.writeString(modifiedDate);
        parcel.writeParcelable(video, i);
    }
}

