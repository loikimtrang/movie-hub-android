package com.movie_hub.android.ui.main.person.fragment;

import android.content.Context;

import androidx.lifecycle.ViewModelProvider;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
import com.movie_hub.android.databinding.FragmentInformationBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.person.PersonDetailViewModel;
import com.movie_hub.android.utils.DisplayUtils;
import com.movie_hub.android.utils.HtmlUtils;

public class InformationFragment extends BaseFragment<FragmentInformationBinding, InformationFragmentViewModel> {
    private PersonDetailViewModel sharedViewModel;

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(PersonDetailViewModel.class);
        setUpView();
    }

    public static InformationFragment newInstance() {
        InformationFragment fragment = new InformationFragment();
        return fragment;
    }

    public void setUpView() {
        PersonResponse personResponse = sharedViewModel.person;

        Context context = getContext();
        String updating = context.getString(R.string.updating);

        binding.otherName.setText(getOrUpdating(personResponse.getOtherName(), updating));
        binding.description.setText(HtmlUtils.convertPtoStrong(getOrUpdating(personResponse.getBio(), updating)));
        binding.country.setText(getOrUpdating(personResponse.getCountry(), updating));
        binding.dob.setText(
                personResponse.getDateOfBirth() != null
                        ? DisplayUtils.displayShortDate(personResponse.getDateOfBirth())
                        : updating
        );
        binding.gender.setText(
                personResponse.getGender() != null
                        ? DisplayUtils.displayGender(context, personResponse.getGender())
                        : updating
        );
    }

    private String getOrUpdating(String value, String updating) {
        return (value != null && !value.trim().isEmpty()) ? value : updating;
    }


    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_information;
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }
}
