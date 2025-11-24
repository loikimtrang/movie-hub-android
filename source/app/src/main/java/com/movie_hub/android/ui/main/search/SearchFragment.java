package com.movie_hub.android.ui.main.search;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.inputmethod.EditorInfo;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.FragmentSearchBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.search.result.SearchResultFragment;
import com.movie_hub.android.ui.main.search.suggestion.SearchSuggestionFragment;
import com.movie_hub.android.ui.main.search.topTrending.SearchTopTrendingFragment;

import eu.davidea.flexibleadapter.databinding.BR;

public class SearchFragment extends BaseFragment<FragmentSearchBinding, SearchViewModel> implements SystemBarColorProvider {

    private FragmentManager fragmentManager;

    private SearchTopTrendingFragment trendingFragment = new SearchTopTrendingFragment();
    private SearchSuggestionFragment suggestionFragment = new SearchSuggestionFragment();
    private SearchResultFragment resultFragment;

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
        fragmentManager = getChildFragmentManager();

        prepareSearchIcons();
        binding.search.setCompoundDrawables(searchIcon, null, null, null);

        showTrending();
        setupSearchEvents();
    }

    private final Handler handler = new Handler();
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY = 500; // milliseconds

    private boolean isClearIconVisible = false;
    @SuppressLint("ClickableViewAccessibility")
    private void setupSearchEvents() {
        binding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();

                handler.removeCallbacks(searchRunnable); // luôn huỷ trước

                if (!keyword.isEmpty()) {
                    if (!isClearIconVisible) {
                        animateClearIcon(true);
                        isClearIconVisible = true;
                    }

                    showSuggestionIfNeeded(); //
                    searchRunnable = () -> suggestionFragment.setKeyWord(keyword);
                    handler.postDelayed(searchRunnable, SEARCH_DELAY);

                } else {
                    if (isClearIconVisible) {
                        animateClearIcon(false);
                        isClearIconVisible = false;
                    }
                    showTrending();
                }
            }


            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                ((MainActivity) requireActivity()).hideKeyboard();
                String keyword = binding.search.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    viewModel.insertKeyWord(keyword);
                    ((MainActivity) requireActivity()).hideLoading();
                    showResult(keyword);
                }
                return true;
            }
            return false;
        });

        binding.search.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2;
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (binding.search.getCompoundDrawables()[DRAWABLE_END] != null) {
                    int drawableWidth = binding.search.getCompoundDrawables()[DRAWABLE_END].getBounds().width();
                    int touchAreaStart = binding.search.getWidth() - binding.search.getPaddingEnd() - drawableWidth;
                    if (event.getX() >= touchAreaStart) {
                        binding.search.setText("");
                        return true;
                    }
                }
            }
            return false;
        });
    }
    private void showSuggestionIfNeeded() {
        if (!(fragmentManager.findFragmentById(R.id.contentView) instanceof SearchSuggestionFragment)) {
            fragmentManager.beginTransaction()
                    .replace(R.id.contentView, suggestionFragment)
                    .commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(searchRunnable);
    }

    private void animateClearIcon(boolean show) {
        Drawable[] drawables = binding.search.getCompoundDrawables();
        Drawable currentEnd = drawables[2]; // drawableEnd

        int duration = 150;

        if (show) {
            if (currentEnd == null) {
                clearIcon.setAlpha(0);
                binding.search.setCompoundDrawables(searchIcon, null, clearIcon, null);
            } else {
                clearIcon = currentEnd;
            }

            ValueAnimator animator = ValueAnimator.ofInt(0, 255);
            animator.setDuration(duration);
            Drawable finalClearIcon = clearIcon;
            animator.addUpdateListener(animation -> {
                finalClearIcon.setAlpha((int) animation.getAnimatedValue());
                binding.search.setCompoundDrawables(searchIcon, null, finalClearIcon, null);
            });
            animator.start();
        } else if (currentEnd != null) {
            // Fade out rồi remove drawableEnd
            ValueAnimator animator = ValueAnimator.ofInt(255, 0);
            animator.setDuration(duration);
            Drawable finalClearIcon = currentEnd;
            animator.addUpdateListener(animation -> {
                finalClearIcon.setAlpha((int) animation.getAnimatedValue());
                binding.search.setCompoundDrawables(searchIcon, null, finalClearIcon, null);
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    binding.search.setCompoundDrawables(searchIcon, null, null, null);
                }
            });
            animator.start();
        }
    }


    private void showTrending() {
        fragmentManager.beginTransaction()
                .replace(R.id.contentView, trendingFragment)
                .commit();
    }

    private void showSuggestion(String keyword) {
        suggestionFragment.setKeyWord(keyword);
        fragmentManager.beginTransaction()
                .replace(R.id.contentView, suggestionFragment)
                .commit();
    }

    public void showResult(String keyword) {
        resultFragment = new SearchResultFragment();
        Bundle bundle = new Bundle();
        bundle.putString("keyword", keyword);
        resultFragment.setArguments(bundle);

        fragmentManager.beginTransaction()
                .replace(R.id.contentView, resultFragment)
                .commit();
    }

    public void onHistoryItemClicked(String keyword) {
        binding.search.setText(keyword);
        binding.search.setSelection(keyword.length());

        animateClearIcon(true);
        isClearIconVisible = true;
        viewModel.insertKeyWord(keyword);
        showResult(keyword);

        ((MainActivity) requireActivity()).hideKeyboard();
    }
    private Drawable searchIcon;
    private Drawable clearIcon;

    private void prepareSearchIcons() {
        searchIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_search_bar);
        clearIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_clear);

        int sizeInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20, requireContext().getResources().getDisplayMetrics());

        if (searchIcon != null) searchIcon.setBounds(0, 0, sizeInPx, sizeInPx);
        if (clearIcon != null) clearIcon.setBounds(0, 0, sizeInPx, sizeInPx);
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search;
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public int getStatusBarColor() {
        return R.color.account_header;
    }
    @Override
    public int getNavigationBarColor() {
        return R.color.bg_tab_bar;
    }
}
