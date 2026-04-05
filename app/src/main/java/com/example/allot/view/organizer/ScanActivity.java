package com.example.allot.view.organizer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.example.allot.R;
import com.example.allot.controller.organizer.ScanController;
import com.example.allot.controller.organizer.ScanDecoderService;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventScanResult;
import com.example.allot.view.event.EventDetailActivity;
import com.example.allot.view.shared.AppNavigator;
import com.example.allot.view.shared.BottomNavBarView;
import com.example.allot.view.shared.EventDisplayFormatter;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hosts the scanner screen and routes scanned event QR codes to event detail.
 */
public class ScanActivity extends AppCompatActivity {
    private PreviewView cameraPreviewView;
    private BottomNavBarView bottomNavBar;
    private TextView scanErrorText;
    private TextView uploadImageButton;
    private ScanController scanController;
    private ScanDecoderService scanDecoderService;
    private ProcessCameraProvider cameraProvider;
    private ExecutorService scannerExecutor;
    private boolean hasRequestedCameraPermission;
    private boolean scanHandlingInProgress;
    private boolean cameraBound;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                hasRequestedCameraPermission = true;
                if (granted) {
                    clearInlineError();
                    startCameraPreview();
                } else {
                    showInlineError(R.string.scan_error_permission);
                }
            });

    private final ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::handlePickedImage);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        scanController = new ScanController();
        scanDecoderService = new ScanDecoderService();
        scannerExecutor = Executors.newSingleThreadExecutor();

        cameraPreviewView = findViewById(R.id.cameraPreviewView);
        scanErrorText = findViewById(R.id.scanErrorText);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        bottomNavBar = findViewById(R.id.bottomNavBar);

        uploadImageButton.setOnClickListener(view -> openImagePicker());
        setupBottomNav();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasCameraPermission()) {
            clearInlineError();
            startCameraPreview();
            return;
        }

        if (!hasRequestedCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            showInlineError(R.string.scan_error_permission);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopCameraPreview();
    }

    @Override
    public void finish() {
        stopCameraPreview();
        if (scannerExecutor != null) {
            scannerExecutor.shutdownNow();
        }
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void setupBottomNav() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.SCAN);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> AppNavigator.openExplore(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, view -> AppNavigator.openSaved(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, view -> AppNavigator.openMyEvents(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, view -> AppNavigator.openProfile(this, true));
    }

    private void openImagePicker() {
        clearInlineError();
        pickImageLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        );
    }

    private void handlePickedImage(Uri imageUri) {
        if (imageUri == null) {
            return;
        }

        clearInlineError();
        stopCameraPreview();
        scannerExecutor.execute(() -> {
            try {
                Bitmap bitmap = loadBitmap(imageUri);
                String payload = scanDecoderService.decodeBitmap(bitmap);
                runOnUiThread(() -> {
                    if (payload == null) {
                        showInlineError(R.string.scan_error_no_qr_found);
                        if (hasCameraPermission()) {
                            startCameraPreview();
                        }
                        return;
                    }
                    resolveScannedPayload(payload);
                });
            } catch (IOException | RuntimeException exception) {
                runOnUiThread(() -> {
                    showInlineError(R.string.scan_error_load);
                    if (hasCameraPermission()) {
                        startCameraPreview();
                    }
                });
            }
        });
    }

    private Bitmap loadBitmap(Uri imageUri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
            return ImageDecoder.decodeBitmap(source, (decoder, info, src) ->
                    decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE));
        }
        return MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
    }

    private void startCameraPreview() {
        if (cameraBound || scanHandlingInProgress) {
            return;
        }

        ListenableFuture<ProcessCameraProvider> providerFuture = ProcessCameraProvider.getInstance(this);
        providerFuture.addListener(() -> {
            try {
                cameraProvider = providerFuture.get();
                bindCameraUseCases();
                cameraBound = true;
            } catch (Exception exception) {
                cameraBound = false;
                showInlineError(R.string.scan_error_camera_start);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) {
            return;
        }

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(scannerExecutor, imageProxy -> {
            if (scanHandlingInProgress) {
                imageProxy.close();
                return;
            }

            String payload = scanDecoderService.decodeImageProxy(imageProxy);
            imageProxy.close();
            if (payload == null) {
                return;
            }

            runOnUiThread(() -> resolveScannedPayload(payload));
        });

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
    }

    private void stopCameraPreview() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        cameraBound = false;
    }

    private void resolveScannedPayload(String payload) {
        if (scanHandlingInProgress) {
            return;
        }

        scanHandlingInProgress = true;
        clearInlineError();
        stopCameraPreview();

        scanController.loadScannedEvent(payload, (EventScanResult result, boolean success) -> {
            if (result != null && result.shouldOpenEvent()) {
                openEventDetail(result.getEvent());
                return;
            }

            int messageResId = result != null && result.getMessageResId() != null
                    ? result.getMessageResId()
                    : R.string.scan_error_load;
            showInlineError(messageResId);
            scanHandlingInProgress = false;

            if (hasCameraPermission()) {
                startCameraPreview();
            }
        });
    }

    private void openEventDetail(Event event) {
        if (event == null) {
            showInlineError(R.string.scan_error_load);
            scanHandlingInProgress = false;
            if (hasCameraPermission()) {
                startCameraPreview();
            }
            return;
        }

        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.getEventId());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_TITLE, EventDisplayFormatter.title(event));
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_LOCATION, EventDisplayFormatter.location(event));
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DATE, EventDisplayFormatter.date(event));
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_PRICE, EventDisplayFormatter.price(event));
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DEADLINE, EventDisplayFormatter.deadline(event));
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_CATEGORY, event.getCategory());
        startActivity(intent);
        scanHandlingInProgress = false;
        overridePendingTransition(0, 0);
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void showInlineError(int messageResId) {
        scanErrorText.setVisibility(android.view.View.VISIBLE);
        scanErrorText.setText(messageResId);
    }

    private void clearInlineError() {
        scanErrorText.setText("");
        scanErrorText.setVisibility(android.view.View.GONE);
    }
}
