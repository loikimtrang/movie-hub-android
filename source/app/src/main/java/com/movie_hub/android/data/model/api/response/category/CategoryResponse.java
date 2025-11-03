package com.movie_hub.android.data.model.api.response.category;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;

@Data
public class CategoryResponse implements Parcelable {
    private Long id;
    private String name;
    private String slug;
    private Integer status;
    private String createdDate;
    private String modifiedDate;

    protected CategoryResponse(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        name = in.readString();
        slug = in.readString();
        if (in.readByte() == 0) {
            status = null;
        } else {
            status = in.readInt();
        }
        createdDate = in.readString();
        modifiedDate = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(name);
        dest.writeString(slug);
        if (status == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(status);
        }
        dest.writeString(createdDate);
        dest.writeString(modifiedDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CategoryResponse> CREATOR = new Creator<CategoryResponse>() {
        @Override
        public CategoryResponse createFromParcel(Parcel in) {
            return new CategoryResponse(in);
        }

        @Override
        public CategoryResponse[] newArray(int size) {
            return new CategoryResponse[size];
        }
    };
}
