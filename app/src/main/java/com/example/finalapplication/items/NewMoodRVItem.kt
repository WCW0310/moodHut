package com.example.finalapplication.items

import android.os.Parcel
import android.os.Parcelable
import com.example.finalapplication.utils.IType

class NewMoodRVItem(val imageResource: String?) : IType, Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()) {
    }

    override fun getType(): Int {
        return TYPE
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(imageResource)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NewMoodRVItem> {
        const val TYPE = 5
        override fun createFromParcel(parcel: Parcel): NewMoodRVItem {
            return NewMoodRVItem(parcel)
        }

        override fun newArray(size: Int): Array<NewMoodRVItem?> {
            return arrayOfNulls(size)
        }
    }
}