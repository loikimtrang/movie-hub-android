package com.movie_hub.android.ui.main.live;

import static com.movie_hub.android.ui.main.home.HomeFragment.NavigateToMovieDetails;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.request.room.RoomRequest;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.room.RoomResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.FragmentLiveBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.home.filter.model.FilterTypeModel;
import com.movie_hub.android.ui.main.live.adapter.RoomAdapter;
import com.movie_hub.android.ui.main.live.adapter.RoomFilterTypeAdapter;
import com.movie_hub.android.ui.main.live.adapter.RoomShimmerAdapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LiveFragment extends BaseFragment<FragmentLiveBinding, LiveViewModel> implements RoomAdapter.OnRoomClickListener {
    private static final int FILTER_ALL = -1;
    private static final int FILTER_MY_ROOM = 0;

    private int currentPage = 0;
    private final int pageSize = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentFilter = FILTER_ALL;

    private RoomShimmerAdapter shimmerAdapter;

    // null means "All states"
    private Integer currentStateFilter = null;

    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);

        initFilter();
        initRoomList();
        initSwipeRefresh();
        binding.option.setOnClickListener(this::showFilterMenu);

        getRooms(true);
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_live;
    }

    public RoomAdapter roomAdapter;

    public RoomFilterTypeAdapter roomFilterTypeAdapter;

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    public List<FilterTypeModel> getFilterType() {
        List<FilterTypeModel> filterTypeModels = new ArrayList<>();
        FilterTypeModel filterTypeModel1 = new FilterTypeModel(getString(R.string.all), -1, true);
        FilterTypeModel filterTypeModel2 = new FilterTypeModel(getString(R.string.my_room), 0, false);

        filterTypeModels.add(filterTypeModel1);
        filterTypeModels.add(filterTypeModel2);

        return filterTypeModels;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initFilter() {
        roomFilterTypeAdapter = new RoomFilterTypeAdapter(filterTypeModel -> {
            currentFilter = filterTypeModel.getType();
            if (!viewModel.isLogin() && currentFilter == FILTER_MY_ROOM) {
                ((MainActivity) requireActivity()).showLoginRequiredDialog();
            } else {
                List<FilterTypeModel> list = getFilterType();
                for (FilterTypeModel m : list) {
                    m.setSelect(m.getType() == currentFilter);
                }
                roomFilterTypeAdapter.setData(list);
                resetPaging();
                getRooms(true);
                updateSwipeToDelete();
            }


        }, requireContext());

        binding.rvType.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvType.setAdapter(roomFilterTypeAdapter);
        roomFilterTypeAdapter.setData(getFilterType());
    }

    private void initRoomList() {
        roomAdapter = new RoomAdapter(requireContext(), this);
        binding.rvRoom.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.rvRoom.setAdapter(roomAdapter);
        shimmerAdapter = new RoomShimmerAdapter(3);

        binding.rvRoom.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) return;

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null || isLoading || isLastPage) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                    getRooms(false);
                }
            }
        });

        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (currentFilter != FILTER_MY_ROOM) return 0;
                int pos = viewHolder.getBindingAdapterPosition();
                RoomResponse item = roomAdapter.getItem(pos);
                if (item == null) return 0;
                if (!Objects.equals(item.getState(), Constants.STATE_END)) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getBindingAdapterPosition();
                RoomResponse item = roomAdapter.getItem(pos);
                if (item == null || item.getId() == null) {
                    roomAdapter.notifyItemChanged(pos);
                    return;
                }

                viewModel.showLoading();
                viewModel.deleteRoom(new MainCallback<Void>() {
                    @Override
                    public void doSuccess() {
                        viewModel.hideLoading();
                        roomAdapter.removeAt(pos);
                        if (roomAdapter.getItemCount() == 0) {
                            binding.layoutEmpty.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void doError(Throwable error) {
                        viewModel.hideLoading();
                        roomAdapter.notifyItemChanged(pos);
                    }

                    @Override public void doSuccess(Void object) {}

                    @Override
                    public void doFail() {
                        viewModel.hideLoading();
                        roomAdapter.notifyItemChanged(pos);
                    }
                }, item.getId());
            }
        });
        updateSwipeToDelete();
    }

    private void initSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> getRooms(true));
    }

    private void resetPaging() {
        currentPage = 0;
        isLastPage = false;
        isLoading = false;
        binding.swipeRefreshLayout.setRefreshing(false);
        binding.layoutEmpty.setVisibility(View.GONE);
    }

    private void showShimmerLoading() {
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.rvRoom.setAdapter(shimmerAdapter);
    }

    private void finishLoading(boolean isRefresh) {
        isLoading = false;
        if (isRefresh) {
            binding.swipeRefreshLayout.setRefreshing(false);
            if (binding.rvRoom.getAdapter() instanceof RoomShimmerAdapter) {
                binding.rvRoom.setAdapter(roomAdapter);
            }
        }
    }

    public void getRooms(boolean isRefresh) {
        if (!viewModel.isLogin() && currentFilter == FILTER_MY_ROOM) {
            ((MainActivity) requireActivity()).showLoginRequiredDialog();
            return;
        }
        if (isLoading) return;
        isLoading = true;

        if (isRefresh) {
            currentPage = 0;
            isLastPage = false;
            binding.layoutEmpty.setVisibility(View.GONE);
            if (!binding.swipeRefreshLayout.isRefreshing()) {
                showShimmerLoading();
            }
        }

        RoomRequest request = new RoomRequest();
        request.setPage(currentPage);
        request.setSize(pageSize);
        request.setPaged(true);
        request.setState(currentStateFilter);

        MainCallback<ResponseListObj<RoomResponse>> callback = new MainCallback<ResponseListObj<RoomResponse>>() {
            @Override
            public void doSuccess(ResponseListObj<RoomResponse> response) {
                List<RoomResponse> data = response != null ? response.getContent() : null;

                if (isRefresh) {
                    if (data == null || data.isEmpty()) {
                        binding.layoutEmpty.setVisibility(View.VISIBLE);
                        roomAdapter.setData(new ArrayList<>());
                    } else {
                        binding.layoutEmpty.setVisibility(View.GONE);
                        roomAdapter.setData(data);
                    }
                } else {
                    if (data != null && !data.isEmpty()) {
                        roomAdapter.addData(data);
                    }
                }

                if (data == null || response == null || currentPage >= response.getTotalPages() || data.size() < pageSize) {
                    isLastPage = true;
                } else {
                    currentPage++;
                }

                finishLoading(isRefresh);
                updateSwipeToDelete();
            }

            @Override
            public void doError(Throwable error) {
                finishLoading(isRefresh);
            }

            @Override
            public void doSuccess() {
            }

            @Override
            public void doFail() {
                finishLoading(isRefresh);
            }
        };

        if (currentFilter == FILTER_MY_ROOM) {
            viewModel.getListMyRoom(callback, request);
        } else {
            viewModel.getListRoom(callback, request);
        }
    }

    private void updateSwipeToDelete() {
        if (itemTouchHelper == null) return;
        if (currentFilter == FILTER_MY_ROOM) {
            itemTouchHelper.attachToRecyclerView(binding.rvRoom);
        } else {
            itemTouchHelper.attachToRecyclerView(null);
        }
    }

    public void showFilterMenu(View anchor) {
        View popupView = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_popup_filter_room, null);
        int widthInPx = getResources().getDimensionPixelSize(R.dimen._160sdp);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                widthInPx,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        ImageView checkAll = popupView.findViewById(R.id.check_all);
        ImageView checkLive = popupView.findViewById(R.id.check_un_live);
        ImageView checkWaiting = popupView.findViewById(R.id.check_waiting);
        ImageView checkEnd = popupView.findViewById(R.id.check_end);

        checkAll.setVisibility(View.GONE);
        checkLive.setVisibility(View.GONE);
        checkWaiting.setVisibility(View.GONE);
        checkEnd.setVisibility(View.GONE);

        if (currentStateFilter == null) {
            checkAll.setVisibility(View.VISIBLE);
        } else if (Objects.equals(currentStateFilter, Constants.STATE_OPEN)) {
            checkLive.setVisibility(View.VISIBLE);
        } else if (Objects.equals(currentStateFilter, Constants.STATE_LOCKED)) {
            checkWaiting.setVisibility(View.VISIBLE);
        } else if (Objects.equals(currentStateFilter, Constants.STATE_END)) {
            checkEnd.setVisibility(View.VISIBLE);
        }

        popupView.findViewById(R.id.btn_all).setOnClickListener(v -> {
            currentStateFilter = null;
            resetPaging();
            getRooms(true);
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.btn_live).setOnClickListener(v -> {
            currentStateFilter = Constants.STATE_OPEN;
            resetPaging();
            getRooms(true);
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.btn_waiting).setOnClickListener(v -> {
            currentStateFilter = Constants.STATE_LOCKED;
            resetPaging();
            getRooms(true);
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.btn_end).setOnClickListener(v -> {
            currentStateFilter = Constants.STATE_END;
            resetPaging();
            getRooms(true);
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(anchor, 0, 0);
    }

    @Override
    public void onRoomClick(RoomResponse model, int position) {
        if (!viewModel.isLogin()) {
            ((MainActivity) requireActivity()).showLoginRequiredDialog();
            return;
        }
        showLoading();
        if (Objects.equals(model.getState(), Constants.ROOM_STATE_RUNNING)) {
            getMovie(model.getMovieItem().getMovie().getId(), model, true);
        } else {
            getMovie(model.getMovieItem().getMovie().getId(), model, false);
        }
    }

    public void getMovie(Long id, RoomResponse model, boolean isRoomRunning) {
        viewModel.getMovie(new MainCallback<MovieResponse>() {
            @Override
            public void doError(Throwable error) {
                hideLoading();
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred)).showMessage(getContext());
            }

            @Override
            public void doSuccess() {
                hideLoading();
            }

            @Override
            public void doFail() {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred)).showMessage(getContext());
                hideLoading();
            }

            @Override
            public void doSuccess(MovieResponse object) {
                if (isRoomRunning) {
                    joinRoom(object, model);
                } else {
                    if (viewModel.isLogin()) {
                        getListMovieTracking(object, NavigateToMovieDetails);
                    }
                }
            }
        }, id);
    }

    public void getListMovieTracking(MovieResponse movieResponse, int typeNavigate) {
        showLoading();
        viewModel.getListMovieTracking(new MainCallback<ListWatchHistoryResponse>() {

            @Override
            public void doSuccess(ListWatchHistoryResponse data) {
                if (typeNavigate == NavigateToMovieDetails) {
                    navigateToMovieDetails(movieResponse, data);
                }
            }

            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {
                hideLoading();
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, movieResponse.getId());
    }

    public void navigateToMovieDetails(MovieResponse movieResponse, ListWatchHistoryResponse listWatchHistoryResponse) {
        ((MainActivity) requireActivity()).navigateToMovieDetail(movieResponse, listWatchHistoryResponse);
    }

    public void joinRoom(MovieResponse movieResponse, RoomResponse model) {
        viewModel.joinRoom(new MainCallback<RoomResponse>() {
            @Override
            public void doError(Throwable error) {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred)).showMessage(getContext());
                hideLoading();
            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doFail() {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred)).showMessage(getContext());
                hideLoading();
            }

            @Override
            public void doSuccess(RoomResponse object) {
                createMqtt(movieResponse, object);
            }
        }, model.getId());
    }

    public void createMqtt(MovieResponse movieResponse, RoomResponse model) {
        ((MainActivity) requireActivity()).createMqtt(movieResponse, model);
    }

    public void showLoading() {
        ((MainActivity) requireActivity()).showLoading();
    }

    public void hideLoading() {
        ((MainActivity) requireActivity()).hideLoading();
    }
}
