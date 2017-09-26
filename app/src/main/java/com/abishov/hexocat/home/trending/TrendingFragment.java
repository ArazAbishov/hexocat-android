package com.abishov.hexocat.home.trending;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.abishov.hexocat.Hexocat;
import com.abishov.hexocat.R;
import com.abishov.hexocat.commons.views.BaseFragment;
import com.abishov.hexocat.commons.views.DividerItemDecoration;
import com.abishov.hexocat.home.repository.RepositoryAdapter;
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding2.view.RxView;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.BindDimen;
import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public final class TrendingFragment extends BaseFragment implements TrendingView {
    public static final String TAG = TrendingFragment.class.getSimpleName();

    private static final String ARG_DAYS = "argument:daysBefore";
    private static final String STATE_VIEW = "state:trendingViewState";
    private static final String STATE_RECYCLER_VIEW = "state:trendingRecyclerViewState";

    @BindView(R.id.swipe_refresh_layout_trending)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.recyclerview_trending)
    RecyclerView recyclerViewTrending;

    @BindView(R.id.button_retry)
    Button buttonRetry;

    @BindDimen(R.dimen.trending_divider_padding_start)
    float dividerPaddingStart;

    @Inject
    TrendingPresenter trendingPresenter;

    @Inject
    Picasso picasso;

    private RepositoryAdapter repositoryAdapter;
    private TrendingViewState viewState;
    private Parcelable recyclerViewState;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;


    public static TrendingFragment create(int daysBefore) {
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_DAYS, daysBefore);

        TrendingFragment trendingFragment = new TrendingFragment();
        trendingFragment.setArguments(arguments);
        return trendingFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        int daysBefpre = getArguments().getInt(ARG_DAYS);
        ((Hexocat) context.getApplicationContext()).networkComponent()
                .plus(new TrendingModule(daysBefpre))
                .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trending, container, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        bind(this, view);

        setupRecyclerView(savedInstanceState);
        buttonRetry.setVisibility(View.GONE);

        if (savedInstanceState != null) {
            viewState = savedInstanceState.getParcelable(STATE_VIEW);
        }

        trendingPresenter.onAttach(this, viewState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (viewState != null) {
            outState.putParcelable(STATE_VIEW, viewState);
            outState.putParcelable(STATE_RECYCLER_VIEW,
                    recyclerViewLayoutManager.onSaveInstanceState());
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        trendingPresenter.onDetach();
    }

    @Override
    public Observable<Object> retryActions() {
        return Observable.merge(RxSwipeRefreshLayout.refreshes(swipeRefreshLayout), RxView.clicks(buttonRetry));
    }

    @Override
    public Consumer<TrendingViewState> renderRepositories() {
        return state -> {
            viewState = state;

            recyclerViewTrending.setVisibility(state.isSuccess() ? View.VISIBLE : View.GONE);
            swipeRefreshLayout.setRefreshing(state.isInProgress());
            buttonRetry.setVisibility(state.isFailure() ? View.VISIBLE : View.GONE);

            if (state.isSuccess()) {
                repositoryAdapter.accept(state.items());
            } else if (state.isFailure()) {
                Toast.makeText(getActivity(),
                        state.error(), Toast.LENGTH_SHORT).show();
            }

            if (recyclerViewState != null) {
                recyclerViewLayoutManager.onRestoreInstanceState(recyclerViewState);
                recyclerViewState = null;
            }
        };
    }

    private void setupRecyclerView(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            recyclerViewState = savedInstanceState.getParcelable(STATE_RECYCLER_VIEW);
        }

        repositoryAdapter = new RepositoryAdapter(LayoutInflater.from(getActivity()), picasso,
                viewModel -> Toast.makeText(getActivity(), viewModel.name(), Toast.LENGTH_SHORT).show());
        recyclerViewLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewTrending.setLayoutManager(recyclerViewLayoutManager);
        recyclerViewTrending.setAdapter(repositoryAdapter);
        recyclerViewTrending.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL, dividerPaddingStart, isRtl(recyclerViewTrending)));
    }

    private boolean isRtl(View view) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }
}
