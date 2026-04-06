package com.example.allot.view.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Lets the user crop a profile photo before upload.
 */
public class ProfilePhotoCropActivity extends AppCompatActivity {
    public static final String EXTRA_INPUT_URI = "input_uri";
    public static final String EXTRA_OUTPUT_URI = "output_uri";

    private static final int OUTPUT_SIZE_PX = 1024;

    private ProfilePhotoCropView cropView;
    private TextView cancelButton;
    private TextView usePhotoButton;
    private Uri inputUri;

    /**
     * Handles the create callback.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_photo_crop);

        cropView = findViewById(R.id.profilePhotoCropView);
        cancelButton = findViewById(R.id.cancelCropButton);
        usePhotoButton = findViewById(R.id.useCroppedPhotoButton);

        String inputUriValue = getIntent().getStringExtra(EXTRA_INPUT_URI);
        inputUri = inputUriValue == null ? null : Uri.parse(inputUriValue);
        if (inputUri == null) {
            Toast.makeText(this, R.string.profile_photo_crop_load_failure, Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        cancelButton.setOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        usePhotoButton.setOnClickListener(view -> exportCroppedPhoto());

        loadCropImage();
    }

    /**
     * Performs load crop image.
     */
    private void loadCropImage() {
        usePhotoButton.setEnabled(false);
        new Thread(() -> {
            Bitmap bitmap = null;
            try {
                bitmap = loadBitmap(inputUri);
            } catch (IOException ignored) {
            }

            Bitmap finalBitmap = bitmap;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (finalBitmap == null) {
                    Toast.makeText(this, R.string.profile_photo_crop_load_failure, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                    return;
                }

                cropView.setCropBitmap(finalBitmap);
                usePhotoButton.setEnabled(true);
            });
        }).start();
    }

    /**
     * Performs export cropped photo.
     */
    private void exportCroppedPhoto() {
        usePhotoButton.setEnabled(false);
        new Thread(() -> {
            Bitmap croppedBitmap = cropView.buildCroppedBitmap(OUTPUT_SIZE_PX);
            Uri outputUri = null;
            if (croppedBitmap != null) {
                outputUri = writeBitmapToCache(croppedBitmap);
            }

            Uri finalOutputUri = outputUri;
            runOnUiThread(() -> {
                if (finalOutputUri == null) {
                    usePhotoButton.setEnabled(true);
                    Toast.makeText(this, R.string.profile_photo_crop_save_failure, Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent result = new Intent();
                result.putExtra(EXTRA_OUTPUT_URI, finalOutputUri.toString());
                setResult(RESULT_OK, result);
                finish();
            });
        }).start();
    }

    /**
     * Returns the result of write bitmap to cache.
     *
     * @param bitmap the bitmap
     * @return the result of this call
     */
    @Nullable
    private Uri writeBitmapToCache(Bitmap bitmap) {
        File outputFile = new File(getCacheDir(), "profile-photo-crop.jpg");
        try (FileOutputStream stream = new FileOutputStream(outputFile, false)) {
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 92, stream)) {
                return null;
            }
            return Uri.fromFile(outputFile);
        } catch (IOException exception) {
            return null;
        }
    }

    /**
     * Returns the result of load bitmap.
     *
     * @param imageUri the image uri
     * @return the result of this call
     */
    @Nullable
    private Bitmap loadBitmap(Uri imageUri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
            return ImageDecoder.decodeBitmap(source, (decoder, info, src) -> {
                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                decoder.setTargetSampleSize(calculateSampleSize(info.getSize().getWidth(), info.getSize().getHeight()));
            });
        }

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        if (bitmap == null) {
            return null;
        }
        int maxDimension = Math.max(bitmap.getWidth(), bitmap.getHeight());
        if (maxDimension <= 2048) {
            return bitmap;
        }

        float scale = 2048f / maxDimension;
        return Bitmap.createScaledBitmap(
                bitmap,
                Math.max(1, Math.round(bitmap.getWidth() * scale)),
                Math.max(1, Math.round(bitmap.getHeight() * scale)),
                true
        );
    }

    /**
     * Returns the result of calculate sample size.
     *
     * @param width the width
     * @param height the height
     * @return the result of this call
     */
    private int calculateSampleSize(int width, int height) {
        int largestDimension = Math.max(width, height);
        int sampleSize = 1;
        while (largestDimension / sampleSize > 2048) {
            sampleSize *= 2;
        }
        return sampleSize;
    }
}
