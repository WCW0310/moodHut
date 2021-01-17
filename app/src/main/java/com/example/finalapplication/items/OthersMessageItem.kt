package com.example.finalapplication.items

import android.os.Parcel
import android.os.Parcelable
import com.example.finalapplication.utils.IType

class OthersMessageItem(
    val messageId: Int,
    val name: String?,
    val text: String?,
    val sendTime: String?
) : IType, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun getType(): Int {
        return TYPE
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(messageId)
        parcel.writeString(name)
        parcel.writeString(text)
        parcel.writeString(sendTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyMessageItem> {
        const val TYPE = 8
        override fun createFromParcel(parcel: Parcel): MyMessageItem {
            return MyMessageItem(parcel)
        }

        override fun newArray(size: Int): Array<MyMessageItem?> {
            return arrayOfNulls(size)
        }
    }


}