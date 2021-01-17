package com.example.finalapplication.items

import android.os.Parcel
import android.os.Parcelable
import com.example.finalapplication.utils.IType

class AddDiaryMoodItem(val imageResource: String?, val moodName: String?) : IType,
    Parcelable {


    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun getType(): Int {
        return TYPE
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(imageResource)
        parcel.writeString(moodName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AddDiaryMoodItem> {
        const val TYPE = 1
        override fun createFromParcel(parcel: Parcel): AddDiaryMoodItem {
            return AddDiaryMoodItem(parcel)
        }

        override fun newArray(size: Int): Array<AddDiaryMoodItem?> {
            return arrayOfNulls(size)
        }
    }
}