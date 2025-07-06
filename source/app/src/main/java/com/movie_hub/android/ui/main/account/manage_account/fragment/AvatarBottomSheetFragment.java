package com.movie_hub.android.ui.main.account.manage_account.fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.api.response.user.UserUploadImageResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.FragmentBottomSheetAvatarBinding;
import com.movie_hub.android.helper.BlurEffectManager;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.manage_account.ManageAccountActivity;
import com.movie_hub.android.ui.main.account.manage_account.fragment.adapter.AvatarMenuAdapter;
import com.movie_hub.android.ui.main.account.manage_account.model.ManageAccountItemModel;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import android.Manifest;

public class AvatarBottomSheetFragment extends BottomSheetDialogFragment implements AvatarMenuAdapter.OnItemClickListener {

    private FragmentBottomSheetAvatarBinding binding;
    private static final int SDK_TIRAMISU = 33;
    private static final String READ_MEDIA_IMAGES = "android.permission.READ_MEDIA_IMAGES";
    private AvatarMenuAdapter menu1;
    private AvatarMenuAdapter menu2;
    private Uri tempCameraUri;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentBottomSheetAvatarBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        BlurEffectManager.addBlurEffect(requireActivity());

        setUpMenu();
        return view;
    }
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    startCrop(uri);
                }
            });

    private final ActivityResultLauncher<Intent> cropLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri croppedUri = UCrop.getOutput(result.getData());
                    if (croppedUri != null) {
                        uploadAvatar(croppedUri);
                    }
                }
            });
    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            isSuccess -> {
                if (isSuccess && tempCameraUri != null) {
                    startCrop(tempCameraUri);
                }
            });

    private void startCrop(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(requireContext().getCacheDir(), "cropped_avatar.jpg"));

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(90);
        options.setToolbarTitle("Crop Image");

        Intent intent = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(512, 512)
                .withOptions(options)
                .getIntent(requireContext());

        cropLauncher.launch(intent);
    }
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCameraInternal();
                } else {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.permission_camera_required), Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> storagePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openGalleryInternal();
                } else {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.permission_storage_required), Toast.LENGTH_SHORT).show();
                }
            });
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCameraInternal();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
    private void openCameraInternal() {
        File photoFile = new File(requireContext().getCacheDir(), "avatar_temp_camera.jpg");
        tempCameraUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
                photoFile
        );
        cameraLauncher.launch(tempCameraUri);
    }
    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openGalleryInternal();
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openGalleryInternal();
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

    }
    private void openGalleryInternal() {
        galleryLauncher.launch("image/*");
    }
    public void uploadAvatar(Uri croppedUri) {
        ((ManageAccountActivity) requireActivity()).uploadAvatar(croppedUri, new MainCallback<UserUploadImageResponse>() {

            @Override
            public void doError(Throwable throwable) {
                if (throwable instanceof UnknownHostException || throwable instanceof SocketTimeoutException) {
                    showUpdateError(getString(R.string.network_error_please_check_your_internet_connection));
                } else if (throwable instanceof ConnectException) {
                    showUpdateError(getString(R.string.cannot_connect_to_the_server_please_try_again));
                } else {
                    showUpdateError(getString(R.string.mgs_update_failed));
                }
            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doFail() {
                showUpdateError(getString(R.string.mgs_update_failed));
            }

            @Override
            public void doSuccess(UserUploadImageResponse object) {
                userChangeAvatar(object.getFilePath());
            }
        });
    }

    public void userChangeAvatar(String filePath) {
        ((ManageAccountActivity) requireActivity()).userChangeAvatar(filePath, new MainCallback<ResponseWrapper>() {
            @Override
            public void doError(Throwable throwable) {
                if (throwable instanceof UnknownHostException || throwable instanceof SocketTimeoutException) {
                    showUpdateError(getString(R.string.network_error_please_check_your_internet_connection));
                } else if (throwable instanceof ConnectException) {
                    showUpdateError(getString(R.string.cannot_connect_to_the_server_please_try_again));
                } else {
                    showUpdateError(getString(R.string.mgs_update_failed));
                }
            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doFail() {
                showUpdateError(getString(R.string.mgs_update_failed));
            }

            @Override
            public void doSuccess(ResponseWrapper object) {
                dismiss();
            }
        });
    }

    private void showUpdateError(String message) {
        new ToastMessage(ToastMessage.TYPE_WARNING, message).showMessage(getContext());
    }
    public void setUpMenu() {
        List<ManageAccountItemModel> menuItems1 = Arrays.asList(
                new ManageAccountItemModel(R.drawable.ic_user_picture, R.string.view_avatar),
                new ManageAccountItemModel(R.drawable.ic_picture_1, R.string.choose_avatar)
        );

        List<ManageAccountItemModel> menuItems2 = Arrays.asList(
                new ManageAccountItemModel(R.drawable.ic_camera, R.string.take_photo),
                new ManageAccountItemModel(R.drawable.ic_picture_2, R.string.choose_from_library),
                new ManageAccountItemModel(R.drawable.ic_picture_3, R.string.choose_existing_photo)
        );

        menu1 = new AvatarMenuAdapter(menuItems1, this);
        binding.recyclerViewMenu1.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewMenu1.setAdapter(menu1);

        menu2 = new AvatarMenuAdapter(menuItems2, this);
        binding.recyclerViewMenu2.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewMenu2.setAdapter(menu2);
    }

    @Override
    public void onItemClick(ManageAccountItemModel item) {
        switch (item.idTitle) {
            case R.string.choose_avatar:
                showMenu2();
                break;
            case R.string.choose_from_library:
                openGallery();
                break;
            case R.string.take_photo:
                openCamera();
                break;
        }
    }

    private void showMenu2() {
        binding.recyclerViewMenu2.setVisibility(View.VISIBLE);

        int width = binding.recyclerViewMenu1.getWidth();

        binding.recyclerViewMenu1.animate()
                .translationX(-width)
                .setDuration(300)
                .start();

        binding.recyclerViewMenu2.setTranslationX(width);
        binding.recyclerViewMenu2.animate()
                .translationX(0)
                .setDuration(300)
                .start();
    }
    @Override
    public void onStart() {
        super.onStart();

        View dialogView = getView();
        if (dialogView != null) {
            View parent = (View) dialogView.getParent();
            BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(parent);

            // Convert dp to pixel
            int desiredHeightDp = 300;
            float density = getResources().getDisplayMetrics().density;
            int desiredHeightPx = (int) (desiredHeightDp * density);

            // Set height
            parent.getLayoutParams().height = desiredHeightPx;
            parent.requestLayout();

            // Set expanded state
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setPeekHeight(desiredHeightPx);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            View touchOutsideView = dialog.findViewById(R.id.touch_outside);
            if (touchOutsideView != null) {
                touchOutsideView.setOnClickListener(v -> {
                    dismiss();
                });
            }
        });

        return dialog;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
