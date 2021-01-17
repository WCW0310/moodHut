package com.example.finalapplication.items

import android.os.Parcel
import android.os.Parcelable
import com.example.finalapplication.utils.IType

class AddDiaryActItem(
    val actFolderName: String?,
    val actArray: Array<String?>,
    val imageResourceArray: Array<String?>
) : IType,
    Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.createStringArray() as Array<String?>,
        parcel.createStringArray() as Array<String?>
    ) {
    }

    override fun getType(): Int {
        return TYPE
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(actFolderName)
        parcel.writeStringArray(actArray)
        parcel.writeStringArray(imageResourceArray)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AddDiaryActItem> {
        const val TYPE = 2
        override fun createFromParcel(parcel: Parcel): AddDiaryActItem {
            return AddDiaryActItem(parcel)
        }

        override fun newArray(size: Int): Array<AddDiaryActItem?> {
            return arrayOfNulls(size)
        }
    }

}