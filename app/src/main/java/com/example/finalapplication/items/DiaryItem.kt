package com.example.finalapplication.items

import android.os.Parcel
import android.os.Parcelable
import com.example.finalapplication.utils.IType

class DiaryItem(
    val diaryId: String?,
    val recordDate: String?,
    val moodNameArray: Array<String?>,
    val moodScoreArray: Array<String?>,
    val eventArray: Array<String?>,
    val content: String?
) : IType, Parcelable {


    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArray() as Array<String?>,
        parcel.createStringArray() as Array<String?>,
        parcel.createStringArray() as Array<String?>,
        parcel.readString()
    ) {
    }

    override fun getType(): Int {
        return TYPE
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(diaryId)
        parcel.writeString(recordDate)
        parcel.writeStringArray(moodNameArray)
        parcel.writeStringArray(moodScoreArray)
        parcel.writeStringArray(eventArray)
        parcel.writeString(content)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DiaryItem> {
        const val TYPE = 3
        override fun createFromParcel(parcel: Parcel): DiaryItem {
            return DiaryItem(parcel)
        }

        override fun newArray(size: Int): Array<DiaryItem?> {
            return arrayOfNulls(size)
        }
    }


}