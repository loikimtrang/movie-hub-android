package com.movie_hub.android.ui.main.movie.detail.review.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.request.review.CreateReviewRequest;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.LayoutDialogReviewBinding;
import com.movie_hub.android.ui.main.movie.detail.review.adapter.ReviewTypeItemAdapter;
import com.movie_hub.android.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

public class ReviewDialogFragment extends DialogFragment implements ReviewTypeItemAdapter.RateClickListener {

    private LayoutDialogReviewBinding binding;
    @Setter
    private ReviewDialogCallback callback;
    private MovieResponse movieResponse;
    private ReviewTypeItemAdapter reviewTypeItemAdapter;
    private final CreateReviewRequest reviewRequest = new CreateReviewRequest();

    public interface ReviewDialogCallback {
        void createReviewCallback(CreateReviewRequest request);
    }

    public static ReviewDialogFragment newInstance(String movieJson) {
        ReviewDialogFragment fragment = new ReviewDialogFragment();
        Bundle args = new Bundle();
        args.putString("movie_json", movieJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutDialogReviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();

            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Dim background
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.4f);

            // Auto adjust keyboard
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            String movieJson = getArguments().getString("movie_json");
            movieResponse = GsonUtils.fromJson(movieJson, MovieResponse.class);
        }

        if (movieResponse != null) {
            binding.tvName.setText(movieResponse.getTitle());
            reviewRequest.setMovieId(movieResponse.getId());

            if (movieResponse.getReviewCount() != null && movieResponse.getReviewCount() > 0L) {
                binding.layoutCountRv.setVisibility(View.VISIBLE);
                binding.tvAvgRv.setText(movieResponse.getAverageRating().toString());
                binding.tvCountRv.setText(movieResponse.getReviewCount().toString() + " " + getString(R.string.review));
            } else {
                binding.layoutCountRv.setVisibility(View.GONE);
            }
        }

        reviewRequest.setRate(-1);

        setupListeners();
        setUpAdapter();
    }


    private void setUpAdapter() {
        reviewTypeItemAdapter = new ReviewTypeItemAdapter(this, requireContext());
        binding.rvReview.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvReview.setAdapter(reviewTypeItemAdapter);

        List<CreateReviewRequest> createReviewRequestList = new ArrayList<>();
        createReviewRequestList.add(new CreateReviewRequest(Constants.TYPE_RATING_5, true));
        createReviewRequestList.add(new CreateReviewRequest(Constants.TYPE_RATING_4, false));
        createReviewRequestList.add(new CreateReviewRequest(Constants.TYPE_RATING_3, false));
        createReviewRequestList.add(new CreateReviewRequest(Constants.TYPE_RATING_2, false));
        createReviewRequestList.add(new CreateReviewRequest(Constants.TYPE_RATING_1, false));

        reviewRequest.setRate(Constants.TYPE_RATING_5);
        reviewTypeItemAdapter.setData(createReviewRequestList);
    }

    @Override
    public void onRateSelect(CreateReviewRequest request) {
        reviewRequest.setRate(request.getRate());
    }

    private void setupListeners() {
        binding.btnCreate.setOnClickListener(v -> {
            String review = binding.edtReview.getText().toString().trim();

            if (!review.isEmpty() && reviewRequest.getRate() != -1) {
                reviewRequest.setContent(review);
                callback.createReviewCallback(reviewRequest);
                dismiss();
            } else {
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.please_write_review)).showMessage(getContext());
            }
        });

        binding.btnCancelCreate.setOnClickListener(v -> dismiss());

        binding.edtReview.setOnFocusChangeListener((v, hasFocus) -> {
            binding.lCountChar.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
        });

        binding.edtReview.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tvCountChar.setText(String.valueOf(s.length()));
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}



