package alexejantonov.com.musixmatch_lyrics_api_app.ui.artists_and_tracks;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.List;

import alexejantonov.com.musixmatch_lyrics_api_app.MyApplication;
import alexejantonov.com.musixmatch_lyrics_api_app.R;
import alexejantonov.com.musixmatch_lyrics_api_app.api.MusixMatchService;
import alexejantonov.com.musixmatch_lyrics_api_app.api.entities.track.Track;
import alexejantonov.com.musixmatch_lyrics_api_app.db.DataBase;
import alexejantonov.com.musixmatch_lyrics_api_app.ui.track_details.TrackDetailsActivity;

public class ArtistsAndTracksListFragment extends Fragment implements ArtistsAndTracksScreenContract.View {

	private static final String BUNDLE_COUNTRY = ArtistsAndTracksListFragment.class.getSimpleName() + ".country";

	private ArtistsAndTracksPresenter presenter = new ArtistsAndTracksPresenter();
	private RecyclerView recyclerView;
	private RequestManager imageRequestManager;
	private DataBase dataBase;
	private MusixMatchService musixMatchService;

	private ProgressBar progressBar;
	private SwipeRefreshLayout swipeRefreshLayout;

	public ArtistsAndTracksListFragment() {
	}

	public static ArtistsAndTracksListFragment newInstance(String country) {
		Bundle args = new Bundle();
		args.putString(BUNDLE_COUNTRY, country);
		ArtistsAndTracksListFragment artistsAndTracksListFragment = new ArtistsAndTracksListFragment();
		artistsAndTracksListFragment.setArguments(args);
		return artistsAndTracksListFragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return LayoutInflater.from(getContext()).inflate(R.layout.fragment_artists, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		progressBar = view.findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);

		swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
		swipeRefreshLayout.setOnRefreshListener(() -> presenter.loadArtists());

		recyclerView = view.findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

		dataBase = MyApplication.getDataBase();
		musixMatchService = MyApplication.getRetrofit().create(MusixMatchService.class);
		String country = getArguments().getString(BUNDLE_COUNTRY);

		presenter.onAttach(dataBase, musixMatchService, this, country);
		Log.d("Presenter", "onAttach()");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		presenter.onDetach();
		Log.d("Presenter", "onDetach()");
	}

	@Override
	public void showData(List<BaseData> data) {
		progressBar.setVisibility(View.INVISIBLE);
		swipeRefreshLayout.setRefreshing(false);

		if (getContext() != null) {
			imageRequestManager = Glide.with(getContext());
			recyclerView.setAdapter(new DataAdapter(
					data,
					this::launchTrackDetailsActivity,
					this::launchTwitter,
					imageRequestManager)
			);
		}
	}

	private void launchTrackDetailsActivity(Track track) {
		Intent i = TrackDetailsActivity.newIntent(getContext(), track);
		startActivity(i);
	}

	private void launchTwitter(String twitterUrl) {
		if (twitterUrl.length() > 0) {
			Log.d("twitter", "twitter://user?screen_name=" + twitterUrl.substring(20));
			try {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + twitterUrl.substring(20))));
			} catch (Exception e) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(twitterUrl)));
			}
		} else {
			Snackbar.make(getView(), "Artist have no Twitter", Snackbar.LENGTH_LONG).show();
		}
	}
}
