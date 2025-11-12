package com.movie_hub.android.data.model.api.response.movie;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;

import java.util.List;

import lombok.Data;

@Data
public class MovieResponse implements Parcelable {
    private Long id;
    private String title;
    private String originalTitle;
    private String description;
    private String posterUrl;
    private String thumbnailUrl;
    private String slug;
    private Integer ageRating;
    private Integer status;
    private Integer type;
    private Long viewCount;
    private Boolean isFeatured;
    private String country;
    private String language;
    private String releaseDate;
    private String createdDate;
    private String modifiedDate;
    private List<CategoryResponse> categories;
    private List<SeasonResponse> seasons; // 🔹 thêm field mới

    protected MovieResponse(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        title = in.readString();
        originalTitle = in.readString();
        description = in.readString();
        posterUrl = in.readString();
        thumbnailUrl = in.readString();
        slug = in.readString();
        if (in.readByte() == 0) {
            ageRating = null;
        } else {
            ageRating = in.readInt();
        }
        if (in.readByte() == 0) {
            status = null;
        } else {
            status = in.readInt();
        }
        if (in.readByte() == 0) {
            type = null;
        } else {
            type = in.readInt();
        }
        if (in.readByte() == 0) {
            viewCount = null;
        } else {
            viewCount = in.readLong();
        }
        byte tmpIsFeatured = in.readByte();
        isFeatured = tmpIsFeatured == 0 ? null : tmpIsFeatured == 1;
        country = in.readString();
        language = in.readString();
        releaseDate = in.readString();
        createdDate = in.readString();
        modifiedDate = in.readString();
        categories = in.createTypedArrayList(CategoryResponse.CREATOR);
        seasons = in.createTypedArrayList(SeasonResponse.CREATOR); // 🔹 đọc thêm seasons
    }

    public static final Creator<MovieResponse> CREATOR = new Creator<MovieResponse>() {
        @Override
        public MovieResponse createFromParcel(Parcel in) {
            return new MovieResponse(in);
        }

        @Override
        public MovieResponse[] newArray(int size) {
            return new MovieResponse[size];
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
        parcel.writeString(originalTitle);
        parcel.writeString(description);
        parcel.writeString(posterUrl);
        parcel.writeString(thumbnailUrl);
        parcel.writeString(slug);
        if (ageRating == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(ageRating);
        }
        if (status == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(status);
        }
        if (type == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(type);
        }
        if (viewCount == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(viewCount);
        }
        parcel.writeByte((byte) (isFeatured == null ? 0 : isFeatured ? 1 : 2));
        parcel.writeString(country);
        parcel.writeString(language);
        parcel.writeString(releaseDate);
        parcel.writeString(createdDate);
        parcel.writeString(modifiedDate);
        parcel.writeTypedList(categories);
        parcel.writeTypedList(seasons); // 🔹 ghi thêm seasons
    }
}
