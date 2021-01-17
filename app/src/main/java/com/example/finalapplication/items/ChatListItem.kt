package com.example.finalapplication.items

import android.os.Parcel
import android.os.Parcelable
import com.example.finalapplication.utils.IType

class ChatListItem(
    val chatroomId: Int,
    val myName: String?,
    val othersName: String?,
    val latestMsg: String?,
    val latestTime: String?
) : IType, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {

    }

    override fun getType(): Int {
        return TYPE
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(chatroomId)
        parcel.writeString(myName)
        parcel.writeString(othersName)
        parcel.writeString(latestMsg)
        parcel.writeString(latestTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatListItem> {
        const val TYPE = 6
        override fun createFromParcel(parcel: Parcel): ChatListItem {
            return ChatListItem(parcel)
        }

        override fun newArray(size: Int): Array<ChatListItem?> {
            return arrayOfNulls(size)
        }
    }
}