package com.movie_hub.android.data.model.api.response.video;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import lombok.Data;

@Data
public class VideoResponse implements Parcelable {
    private Long id;
    private String name;
    private String description;
    private String shortDescription;
    private String content;
    private String relativeContentPath;
    private String thumbnailUrl;
    private String spriteUrl;
    private String vttUrl;
    private Long duration;
    private Long introStart;
    private Long introEnd;
    private Long outroStart;
    private Integer state;
    private Integer status;
    private String createdDate;
    private String modifiedDate;

    protected VideoResponse(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        name = in.readString();
        description = in.readString();
        shortDescription = in.readString();
        content = in.readString();
        relativeContentPath = in.readString();
        thumbnailUrl = in.readString();
        spriteUrl = in.readString();
        vttUrl = in.readString();
        if (in.readByte() == 0) {
            duration = null;
        } else {
            duration = in.readLong();
        }
        if (in.readByte() == 0) {
            introStart = null;
        } else {
            introStart = in.readLong();
        }
        if (in.readByte() == 0) {
            introEnd = null;
        } else {
            introEnd = in.readLong();
        }
        if (in.readByte() == 0) {
            outroStart = null;
        } else {
            outroStart = in.readLong();
        }
        if (in.readByte() == 0) {
            state = null;
        } else {
            state = in.readInt();
        }
        if (in.readByte() == 0) {
            status = null;
        } else {
            status = in.readInt();
        }
        createdDate = in.readString();
        modifiedDate = in.readString();
    }

    public static final Creator<VideoResponse> CREATOR = new Creator<VideoResponse>() {
        @Override
        public VideoResponse createFromParcel(Parcel in) {
            return new VideoResponse(in);
        }

        @Override
        public VideoResponse[] newArray(int size) {
            return new VideoResponse[size];
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
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(shortDescription);
        parcel.writeString(content);
        parcel.writeString(relativeContentPath);
        parcel.writeString(thumbnailUrl);
        parcel.writeString(spriteUrl);
        parcel.writeString(vttUrl);
        if (duration == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(duration);
        }
        if (introStart == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(introStart);
        }
        if (introEnd == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(introEnd);
        }
        if (outroStart == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(outroStart);
        }
        if (state == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(state);
        }
        if (status == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(status);
        }
        parcel.writeString(createdDate);
        parcel.writeString(modifiedDate);
    }
}
