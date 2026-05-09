package com.movie_hub.android.ui.main.home.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.collection.CollectionResponse;
import com.movie_hub.android.data.model.api.response.favourite.FavouriteResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.ItemHomeCollectionBinding;
import com.movie_hub.android.databinding.ItemHomeCollectionBinding;
import com.movie_hub.android.ui.main.home.OnMovieClickCallback;

import java.util.ArrayList;
import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.CollectionType_1_ViewHolder> {

    private final List<CollectionResponse> items = new ArrayList<>();
    private OnCollectionClickListener listener;
    private Context context;
    public OnMovieClickCallback onMovieClickCallback;
    public interface OnCollectionClickListener {
        void onMoreClick(CollectionResponse collectionResponse);
    }
    public CollectionAdapter(OnCollectionClickListener listener, OnMovieClickCallback onMovieClickCallback, Context context) {
        super();
        this.listener = listener;
        this.context = context;
        this.onMovieClickCallback =onMovieClickCallback;
    }

    @NonNull
    @Override
    public CollectionType_1_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemHomeCollectionBinding binding = ItemHomeCollectionBinding.inflate(inflater, parent, false);
        return new CollectionType_1_ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionType_1_ViewHolder holder, int position) {
        CollectionResponse item = items.get(position);
        holder.binding.tvTitle.setSelected(true);
        holder.binding.tvTitle.setText(item.getName());

        resetViews(holder);

        switch (item.getStyleType()) {
            case Constants.TYPE_COLLECTION_1:
                setupRecycler(holder, new CollectionType_1_Adapter(onMovieClickCallback, context), item.getMovies());
                break;

            case Constants.TYPE_COLLECTION_3:
                setupRecycler(holder, new CollectionType_3_Adapter(onMovieClickCallback, context), item.getMovies());
                break;

            case Constants.TYPE_COLLECTION_4:
                showMoreButton(holder, item);
                setupViewPager(holder, item.getMovies());
                break;

            default:
                showMoreButton(holder, item);
                setupRecycler(holder, new CollectionType_2_Adapter(onMovieClickCallback, context), item.getMovies());
                break;

        }
    }

    private void resetViews(CollectionType_1_ViewHolder holder) {
        holder.binding.rvCollection.setVisibility(View.GONE);
        holder.binding.viewPager.setVisibility(View.GONE);
        holder.binding.btnMore.setVisibility(View.INVISIBLE);
        holder.binding.layoutBtn.setVisibility(View.GONE);
    }

    private void showMoreButton(CollectionType_1_ViewHolder holder, CollectionResponse item) {
        holder.binding.btnMore.setVisibility(View.VISIBLE);
        holder.binding.btnMore.setOnClickListener(v -> listener.onMoreClick(item));
    }

    private void setupViewPager(CollectionType_1_ViewHolder holder, List<MovieResponse> data) {
        holder.binding.layoutBtn.setVisibility(View.VISIBLE);
        holder.binding.btnMore.setVisibility(View.VISIBLE);
        holder.binding.viewPager.setVisibility(View.VISIBLE);

        ViewPager2 viewPager = holder.binding.viewPager;

        RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
        recyclerView.setClipToPadding(false);
        recyclerView.setClipChildren(false);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        if (viewPager.getAdapter() == null || !(viewPager.getAdapter() instanceof CollectionType_4_Adapter)) {
            CollectionType_4_Adapter adapter = new CollectionType_4_Adapter(onMovieClickCallback, context);
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(3);
            viewPager.setPageTransformer(new RevealPageTransformer());
        }

        final MovieResponse[] currentMovie = {new MovieResponse()};
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                CollectionType_4_Adapter adapter = (CollectionType_4_Adapter) viewPager.getAdapter();
                if (adapter != null && position < adapter.getItemCount()) {
                    currentMovie[0] = adapter.getItemAt(position);
                }
            }
        });

        holder.binding.btnInf.setOnClickListener(v -> {
            int currentPosition = viewPager.getCurrentItem();
            CollectionType_4_Adapter adapter = (CollectionType_4_Adapter) viewPager.getAdapter();
            if (adapter != null && currentPosition < adapter.getItemCount()) {
                MovieResponse movie = adapter.getItemAt(currentPosition);
                onMovieClickCallback.onMovieClick(movie);
            }
        });

        holder.binding.btnWatchNow.setOnClickListener(v -> {
            int currentPosition = viewPager.getCurrentItem();
            CollectionType_4_Adapter adapter = (CollectionType_4_Adapter) viewPager.getAdapter();
            if (adapter != null && currentPosition < adapter.getItemCount()) {
                MovieResponse movie = adapter.getItemAt(currentPosition);
                onMovieClickCallback.onWatchMovieClick(movie);
            }
        });

        CollectionType_4_Adapter adapter = (CollectionType_4_Adapter) viewPager.getAdapter();
        adapter.setData(data);
    }

    private void setupRecycler(CollectionType_1_ViewHolder holder, RecyclerView.Adapter<?> adapterTemplate, List<MovieResponse> data) {
        holder.binding.rvCollection.setVisibility(View.VISIBLE);

        RecyclerView rv = holder.binding.rvCollection;

        if (rv.getLayoutManager() == null) {
            rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        }

        RecyclerView.Adapter<?> currentAdapter = rv.getAdapter();

        if (currentAdapter == null || !currentAdapter.getClass().equals(adapterTemplate.getClass())) {
            rv.setAdapter(adapterTemplate);
            currentAdapter = adapterTemplate;
        }

        if (currentAdapter instanceof CollectionType_1_Adapter) {
            ((CollectionType_1_Adapter) currentAdapter).setData(data);
        } else if (currentAdapter instanceof CollectionType_2_Adapter) {
            ((CollectionType_2_Adapter) currentAdapter).setData(data);
        } else if (currentAdapter instanceof CollectionType_3_Adapter) {
            ((CollectionType_3_Adapter) currentAdapter).setData(data);
        }
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);

            notifyItemRangeChanged(position, items.size());
        }
    }

    @SuppressLint("NewApi")
    public void setData(List<CollectionResponse> newData) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return items.size();
            }

            @Override
            public int getNewListSize() {
                return newData != null ? newData.size() : 0;
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return items.get(oldItemPosition).getId().equals(newData.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                CollectionResponse oldItem = items.get(oldItemPosition);
                CollectionResponse newItem = newData.get(newItemPosition);
                return oldItem.getName().equals(newItem.getName())
                        && oldItem.getMovies().size() == newItem.getMovies().size();
            }
        });

        items.clear();
        if (newData != null) {
            items.addAll(newData);
        }
        diffResult.dispatchUpdatesTo(this);
    }

    public void addData(List<CollectionResponse> moreItems) {
        int startPos = items.size();
        items.addAll(moreItems);
        notifyItemRangeInserted(startPos, moreItems.size());
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CollectionType_1_ViewHolder extends RecyclerView.ViewHolder {
        private final ItemHomeCollectionBinding binding;

        public CollectionType_1_ViewHolder(@NonNull ItemHomeCollectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}