package com.codepath.parsetagram;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;

    private ImageView ivPostImage;
    private TextInputLayout etDescription;
    private Button btnCaptureImage;
    private Button btnSubmit;
    private Button btnLogout;
    ProgressBar progressBar;
    private File photoFile;
    public String photoFileName = "photo.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivPostImage = findViewById(R.id.ivPostImage);
        etDescription = findViewById(R.id.etDescription);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);

        Objects.requireNonNull(etDescription.getEditText()).addTextChangedListener(createTextWatcher(etDescription));
        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String description = Objects.requireNonNull(etDescription.getEditText()).getText().toString();
                if (validateDescriptionInput(description)) {
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    savePost(description, currentUser, photoFile);
                    showProgressBar();
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOut();
                goLoginActivity();
            }
        });

//        queryPosts();
    }

    private void goLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    private void showProgressBar() {
        etDescription.setVisibility(View.INVISIBLE);
        btnSubmit.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);

        Uri fileProvider = FileProvider.getUriForFile(MainActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                ivPostImage.setImageBitmap(takenImage);
                etDescription.setVisibility(View.VISIBLE);
                btnCaptureImage.setVisibility(View.INVISIBLE);
                btnSubmit.setVisibility(View.VISIBLE);
                btnLogout.setVisibility(View.INVISIBLE);
            } else {
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public File getPhotoFileUri(String fileName) {
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    private void savePost(String description, ParseUser currentUser, File photoFile) {
        Post post = new Post();
        post.setDescription(description);
        post.setImage(new ParseFile(photoFile));
        post.setUser(currentUser);
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    // TODO: add dialog box
                }
                Log.i(TAG, "Post success");
                Objects.requireNonNull(etDescription.getEditText()).setText("");
                ivPostImage.setImageResource(0);
                progressBar.setVisibility(View.INVISIBLE);
                etDescription.setVisibility(View.INVISIBLE);
                btnSubmit.setVisibility(View.INVISIBLE);
                btnCaptureImage.setVisibility(View.VISIBLE);
                btnLogout.setVisibility(View.VISIBLE);
            }
        });
    }

    private TextWatcher createTextWatcher(final TextInputLayout text) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // not needed
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                text.setError(null);
                btnSubmit.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // not needed
            }
        };
    }
    private boolean validateDescriptionInput(String description) {
        if (description.isEmpty()) {
            etDescription.setError("Description cannot be empty");
            btnSubmit.setEnabled(false);
            return false;
        }
        return true;
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                for (Post post : posts) {
                    Log.i(TAG, "Post by " + post.getUser().getUsername() + ": " + post.getDescription());
                }
            }
        });
    }
}

// TODO: add cancel button next to submit?
