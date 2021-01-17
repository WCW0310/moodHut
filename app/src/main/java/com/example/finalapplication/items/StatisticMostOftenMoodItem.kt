package com.example.finalapplication.items

import android.os.Parcel
import android.os.Parcelable
import com.example.finalapplication.utils.IType

class StatisticMostOftenMoodItem(val moodName: String?, val moodTimes: String?, val defaultEvent: String?) : IType,
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun getType(): Int {
        return TYPE
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(moodName)
        parcel.writeString(moodTimes)
        parcel.writeString(defaultEvent)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StatisticMostOftenMoodItem> {
        const val TYPE = 4
        override fun createFromParcel(parcel: Parcel): StatisticMostOftenMoodItem {
            return StatisticMostOftenMoodItem(parcel)
        }

        override fun newArray(size: Int): Array<StatisticMostOftenMoodItem?> {
            return arrayOfNulls(size)
        }
    }
}