package alexejantonov.com.musixmatch_lyrics_api_app.ui.Base

import alexejantonov.com.musixmatch_lyrics_api_app.R
import alexejantonov.com.musixmatch_lyrics_api_app.api.entities.artist.Artist
import alexejantonov.com.musixmatch_lyrics_api_app.api.entities.track.Track
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.item_artist.view.artistName
import kotlinx.android.synthetic.main.item_artist.view.twitterIcon
import kotlinx.android.synthetic.main.item_track.view.albumCover
import kotlinx.android.synthetic.main.item_track.view.trackAlbum
import kotlinx.android.synthetic.main.item_track.view.trackName

class DataAdapter(
    private val data: MutableList<BaseData>,
    private val onTrackClickListener: OnTrackClickListener,
    private val onTwitterClickListener: OnTwitterClickListener,
    private val imageRequestManager: RequestManager,
    private var query: String?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  override fun getItemViewType(position: Int): Int {
    return if (data[position] is Artist) 0
    else 1
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      0    -> ArtistViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_artist, parent, false))
      1    -> TrackViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false))
      else -> throw IllegalStateException("Unknown view type")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (this.getItemViewType(position)) {
      0 -> (holder as ArtistViewHolder).bindTo(data[position] as Artist)
      1 -> (holder as TrackViewHolder).bindTo(data[position] as Track)
    }
  }

  override fun getItemCount() = data.size

  fun updateData(data: List<BaseData>) {
    val diffResult = DiffUtil.calculateDiff(DataDiffCallback(this.data, data))

    this.data.clear()
    this.data.addAll(data)
    diffResult.dispatchUpdatesTo(this)
  }

  fun updateQueryData(data: MutableList<BaseData>, query: String) {
    this.data.clear()
    this.data.addAll(data)
    this.query = query
    notifyDataSetChanged()
  }

  private inner class ArtistViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindTo(artist: Artist) {
      itemView.apply {
        if (query != null) {
          val startIndex = indexOfSearchQuery(artist.artistName)
          if (startIndex == -1) {
            artistName.text = artist.artistName
          } else {
            val highLightedName = SpannableString(artist.artistName)
            highLightedName.setSpan(
                TextAppearanceSpan(itemView.context, R.style.searchTextHighlight),
                startIndex,
                startIndex + query!!.length,
                0)
            artistName.text = highLightedName
          }
        } else {
          artistName.text = artist.artistName
        }
        twitterIcon.setOnClickListener { onTwitterClickListener.onClick((data[adapterPosition] as Artist).twitterUrl) }
      }
    }

    private fun indexOfSearchQuery(artistName: String): Int {
      return if (!TextUtils.isEmpty(query)) {
        artistName.toLowerCase().indexOf(query!!.toLowerCase())
      } else -1
    }
  }

  private inner class TrackViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindTo(track: Track) {
      itemView.apply {
        if (query != null) {
          val startIndex = indexOfSearchQuery(track.trackName)
          if (startIndex == -1) {
            trackName.text = track.trackName
          } else {
            val highLightedName = SpannableString(track.trackName)
            highLightedName.setSpan(
                TextAppearanceSpan(itemView.context, R.style.searchTextHighlight),
                startIndex,
                startIndex + query!!.length,
                0)
            trackName.text = highLightedName
          }
        } else {
          trackName.text = track.trackName
        }
        trackAlbum.text = track.albumName
        imageRequestManager.load(track.albumCover).into(albumCover)
        setOnClickListener { onTrackClickListener.onClick(data[adapterPosition] as Track) }
      }
    }

    private fun indexOfSearchQuery(artistName: String): Int {
      return if (!TextUtils.isEmpty(query)) {
        artistName.toLowerCase().indexOf(query!!.toLowerCase())
      } else -1
    }
  }

  interface OnTrackClickListener {
    fun onClick(track: Track)
  }

  interface OnTwitterClickListener {
    fun onClick(twitterUrl: String)
  }
}
