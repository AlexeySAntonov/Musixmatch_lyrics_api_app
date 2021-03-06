package com.aleksejantonov.lyricseverywhere.api.entities.track

import android.os.Build.VERSION_CODES
import android.support.annotation.RequiresApi
import com.aleksejantonov.lyricseverywhere.ui.base.BaseData
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Objects

data class Track(
    @SerializedName("track_id") val trackId: Long,
    @SerializedName("track_name") val trackName: String,
    @SerializedName("album_name") val albumName: String,
    @SerializedName("artist_id") override var artistId: Long
) : BaseData, Serializable {

  override fun toString(): String {
    return "Track{" +
        "trackId=" + trackId +
        ", trackName='" + trackName + '\''.toString() +
        ", albumName='" + albumName + '\''.toString() +
        ", artistId=" + artistId +
        '}'.toString()
  }

  override fun equals(other: Any?): Boolean {
    if (other == null || other !is Track) return false
    return artistId == other.artistId
  }

  @RequiresApi(VERSION_CODES.KITKAT)
  override fun hashCode() = Objects.hash(trackId, trackName, albumName, artistId)
}
