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
import com.movie_hub.android.utils.DialogUtils;
import com.movie_hub.android.utils.DisplayUtils;
import com.movie_hub.android.utils.RoomDialogUtils;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
    private boolean isSearchMode = false;
    private boolean isSearchLoading = false;
    private boolean isClearIconVisible = false;
    private Drawable searchIcon;
    private Drawable clearIcon;

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);

        initFilter();
        initRoomList();
        initSwipeRefresh();
        initSearch();
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
                roomAdapter.setMyRoomListMode(currentFilter == FILTER_MY_ROOM);
                resetPaging();
                getRooms(true);
            }


        }, requireContext());

        binding.rvType.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvType.setAdapter(roomFilterTypeAdapter);
        roomFilterTypeAdapter.setData(getFilterType());
    }

    private void initRoomList() {
        roomAdapter = new RoomAdapter(requireContext(), this);
        roomAdapter.setMyRoomListMode(currentFilter == FILTER_MY_ROOM);
        binding.rvRoom.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.rvRoom.setAdapter(roomAdapter);
        shimmerAdapter = new RoomShimmerAdapter(3);

        binding.rvRoom.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0 || isSearchMode) return;

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
    }

    private void initSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            if (isSearchMode) {
                searchRoomByCode();
            } else {
                getRooms(true);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearch() {
        prepareSearchIcons();
        binding.edtSearchRoom.setCompoundDrawables(searchIcon, null, null, null);

        binding.btnSearch.setOnClickListener(v -> enterSearchMode());

        binding.edtSearchRoom.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchRoomByCode();
                return true;
            }
            return false;
        });

        binding.edtSearchRoom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isSearchMode) return;
                if (!isClearIconVisible) {
                    binding.edtSearchRoom.setCompoundDrawables(searchIcon, null, clearIcon, null);
                    isClearIconVisible = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.edtSearchRoom.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && isClearIconVisible) {
                Drawable endDrawable = binding.edtSearchRoom.getCompoundDrawables()[2];
                if (endDrawable != null) {
                    int drawableWidth = endDrawable.getBounds().width();
                    int touchAreaStart = binding.edtSearchRoom.getWidth()
                            - binding.edtSearchRoom.getPaddingEnd() - drawableWidth;
                    if (event.getX() >= touchAreaStart) {
                        if (!binding.edtSearchRoom.getText().toString().trim().isEmpty()) {
                            binding.edtSearchRoom.setText("");
                        } else {
                            exitSearchMode();
                        }
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private void prepareSearchIcons() {
        searchIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_search_bar);
        clearIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_clear);

        int sizeInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20, requireContext().getResources().getDisplayMetrics());

        if (searchIcon != null) searchIcon.setBounds(0, 0, sizeInPx, sizeInPx);
        if (clearIcon != null) clearIcon.setBounds(0, 0, sizeInPx, sizeInPx);
    }

    private void enterSearchMode() {
        if (isSearchMode) return;

        isSearchMode = true;
        binding.layoutHeader.setVisibility(View.GONE);
        binding.layoutSearch.setVisibility(View.VISIBLE);
        binding.rvType.setVisibility(View.GONE);
        binding.edtSearchRoom.setText("");
        binding.edtSearchRoom.setCompoundDrawables(searchIcon, null, clearIcon, null);
        isClearIconVisible = true;
        roomAdapter.setData(new ArrayList<>());
        binding.layoutEmpty.setVisibility(View.GONE);
        isLastPage = true;
        binding.edtSearchRoom.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(binding.edtSearchRoom, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void exitSearchMode() {
        if (!isSearchMode) return;

        isSearchMode = false;
        isSearchLoading = false;
        isClearIconVisible = false;
        binding.layoutSearch.setVisibility(View.GONE);
        binding.layoutHeader.setVisibility(View.VISIBLE);
        binding.rvType.setVisibility(View.VISIBLE);
        binding.edtSearchRoom.setText("");
        binding.edtSearchRoom.setCompoundDrawables(searchIcon, null, null, null);
        binding.tvEmpty.setText(getString(R.string.empty_room));
        ((MainActivity) requireActivity()).hideKeyboard();
        resetPaging();
        getRooms(true);
    }

    private void searchRoomByCode() {
        String code = binding.edtSearchRoom.getText().toString().trim();
        if (code.isEmpty()) {
            new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.error_input_room_code)).showMessage(requireContext());
            binding.swipeRefreshLayout.setRefreshing(false);
            return;
        }
        if (isSearchLoading) return;

        isSearchLoading = true;
        ((MainActivity) requireActivity()).hideKeyboard();
        if (!binding.swipeRefreshLayout.isRefreshing()) {
            showShimmerLoading();
        }

        viewModel.getRoomByCode(new MainCallback<RoomResponse>() {
            @Override
            public void doSuccess(RoomResponse response) {
                isSearchLoading = false;
                finishLoading(true);
                roomAdapter.setMyRoomListMode(false);
                if (response != null) {
                    List<RoomResponse> data = new ArrayList<>();
                    data.add(response);
                    roomAdapter.setData(data);
                    binding.layoutEmpty.setVisibility(View.GONE);
                } else {
                    roomAdapter.setData(new ArrayList<>());
                    binding.tvEmpty.setText(getString(R.string.empty_room_search));
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void doError(Throwable error) {
                isSearchLoading = false;
                finishLoading(true);
                roomAdapter.setData(new ArrayList<>());
                binding.tvEmpty.setText(getString(R.string.empty_room_search));
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                new ToastMessage(ToastMessage.TYPE_ERROR, getString(R.string.an_error_occurred)).showMessage(requireContext());
            }

            @Override
            public void doSuccess() {
            }

            @Override
            public void doFail() {
                isSearchLoading = false;
                finishLoading(true);
                roomAdapter.setData(new ArrayList<>());
                binding.tvEmpty.setText(getString(R.string.empty_room_search));
                binding.layoutEmpty.setVisibility(View.VISIBLE);
            }
        }, code);
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
        if (isSearchMode) return;
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
                roomAdapter.setMyRoomListMode(currentFilter == FILTER_MY_ROOM);
                List<RoomResponse> data = response != null ? response.getContent() : null;

                if (isRefresh) {
                    if (data == null || data.isEmpty()) {
                        binding.tvEmpty.setText(getString(R.string.empty_room));
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
    public void onRoomDelete(RoomResponse item, int pos) {
        if (item == null || item.getId() == null || pos == RecyclerView.NO_POSITION) return;

        // Remove immediately with smooth animation.
        roomAdapter.removeAt(pos);
        updateEmptyState();

        showLoading();
        viewModel.deleteRoom(new MainCallback<Void>() {
            @Override
            public void doSuccess() {
                hideLoading();
                updateEmptyState();
            }

            @Override
            public void doError(Throwable error) {
                hideLoading();
                // Best-effort restore if API fails.
                roomAdapter.restoreAt(pos, item);
                updateEmptyState();
            }

            @Override
            public void doFail() {
                hideLoading();
                roomAdapter.restoreAt(pos, item);
                updateEmptyState();
            }
        }, item.getId());
    }

    @Override
    public void onRoomCopyCode(RoomResponse model) {
        if (model == null) return;
        RoomDialogUtils.showRoomCodeDialog(requireContext(), model.getCode(), null);
    }

    private void updateEmptyState() {
        if (isSearchMode) return;
        binding.tvEmpty.setText(getString(R.string.empty_room));
        binding.layoutEmpty.setVisibility(roomAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRoomClick(RoomResponse model, int position) {
        handleRoomClickFromExternal(model);
    }

    public void handleRoomClickFromExternal(RoomResponse model) {
        ((MainActivity) requireActivity()).handleRoomClick(model);
    }

    private void showLoading() {
        ((MainActivity) requireActivity()).showLoading();
    }

    private void hideLoading() {
        ((MainActivity) requireActivity()).hideLoading();
    }
}
