package alexejantonov.com.musixmatch_lyrics_api_app.ui.artists_and_tracks;

import android.util.Log;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import java.util.ArrayList;
import java.util.List;

import alexejantonov.com.musixmatch_lyrics_api_app.MyApplication;
import alexejantonov.com.musixmatch_lyrics_api_app.api.MusixMatchService;
import alexejantonov.com.musixmatch_lyrics_api_app.api.config.Constants;
import alexejantonov.com.musixmatch_lyrics_api_app.api.entities.artist.Artist;
import alexejantonov.com.musixmatch_lyrics_api_app.api.entities.artist.ArtistResponse;
import alexejantonov.com.musixmatch_lyrics_api_app.api.entities.track.Track;
import alexejantonov.com.musixmatch_lyrics_api_app.api.entities.track.TrackResponse;
import alexejantonov.com.musixmatch_lyrics_api_app.db.DataBase;
import alexejantonov.com.musixmatch_lyrics_api_app.ui.Base.QueryType;
import alexejantonov.com.musixmatch_lyrics_api_app.utils.DataContainersUtil;
import alexejantonov.com.musixmatch_lyrics_api_app.utils.DataMergeUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@InjectViewState
public class ArtistsAndTracksPresenter extends MvpPresenter<ArtistsAndTracksListView> {

	private List<Artist> artists = new ArrayList<>();
	private List<Track> tracks = new ArrayList<>();
	private MusixMatchService musixMatchService = MyApplication.getService();
	private String country;
	private DataBase dataBase = MyApplication.getDataBase();

	@Override
	protected void onFirstViewAttach() {
		super.onFirstViewAttach();
		loadData();
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void loadData() {
		if (country == null) {
			country = QueryType.ru.name();
		}
		if (dataBase.getArtists(country).size() > 0 && dataBase.getTracks().size() > 0) {
			//Тащим с БД если не пусто
			getViewState().showData(DataMergeUtil.listsMerge(dataBase.getArtists(country), dataBase.getTracks()));
		} else {
			loadArtists();
		}
	}

	public void loadArtists() {
		Log.d("Loading", country + " top chart artists from Server");
		musixMatchService.getArtists(Constants.API_KEY_VALUE, country, "1", "100").enqueue(new Callback<ArtistResponse>() {
			@Override
			public void onResponse(Call<ArtistResponse> call, Response<ArtistResponse> response) {
				if (response.isSuccessful()) {
					artists = DataContainersUtil.artistContainersToArtists(
							response.body()
									.getMessage()
									.getBody()
									.getArtistContainers(),
							country
					);

					//Только если загрузились исполнители, начинаем загружать треки
					Log.d("Loading", "Tracks");
					loadTracks();
				}
			}

			@Override
			public void onFailure(Call<ArtistResponse> call, Throwable t) {
				Log.d("Artists loading failed", t.getMessage());
			}
		});
	}

	public void loadTracks() {

		musixMatchService.getTracks(Constants.API_KEY_VALUE, country, "1", "100").enqueue(new Callback<TrackResponse>() {
			@Override
			public void onResponse(Call<TrackResponse> call, Response<TrackResponse> response) {
				if (response.isSuccessful()) {
					tracks = DataContainersUtil.trackContainersToTracks(
							response.body()
									.getMessage()
									.getBody()
									.getTrackContainers()
					);
					//Если и треки успешно загрузились, обновляем данные в БД и мержим списки для адаптера
					dataBase.insertArtists(artists);
					dataBase.insertTracks(tracks);
					getViewState().showData(DataMergeUtil.listsMerge(artists, tracks));
				}
			}

			@Override
			public void onFailure(Call<TrackResponse> call, Throwable t) {
				Log.d("Tracks loading failed", t.getMessage());
			}
		});
	}
}
