package com.movie_hub.android.data.model.api.response.MovieItem;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.video.VideoResponse;

import lombok.Data;

@Data
public class MovieItemResponse implements Parcelable {
    private Long id;
    private String title;
    private String description;
    private String releaseDate;
    private Integer status;
    private Integer ordering;
    private String label;
    private Integer kind;
    private String createdDate;
    private String modifiedDate;
    private MovieResponse movie;
    private VideoResponse video;

    protected MovieItemResponse(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        title = in.readString();
        description = in.readString();
        releaseDate = in.readString();
        if (in.readByte() == 0) {
            status = null;
        } else {
            status = in.readInt();
        }
        if (in.readByte() == 0) {
            ordering = null;
        } else {
            ordering = in.readInt();
        }
        label = in.readString();
        if (in.readByte() == 0) {
            kind = null;
        } else {
            kind = in.readInt();
        }
        createdDate = in.readString();
        modifiedDate = in.readString();
        movie = in.readParcelable(MovieResponse.class.getClassLoader());
        video = in.readParcelable(VideoResponse.class.getClassLoader());
    }

    public static final Creator<MovieItemResponse> CREATOR = new Creator<MovieItemResponse>() {
        @Override
        public MovieItemResponse createFromParcel(Parcel in) {
            return new MovieItemResponse(in);
        }

        @Override
        public MovieItemResponse[] newArray(int size) {
            return new MovieItemResponse[size];
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
        parcel.writeString(releaseDate);
        if (status == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(status);
        }
        if (ordering == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(ordering);
        }
        parcel.writeString(label);
        if (kind == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(kind);
        }
        parcel.writeString(createdDate);
        parcel.writeString(modifiedDate);
        parcel.writeParcelable(movie, i);
        parcel.writeParcelable(video, i);
    }
}

