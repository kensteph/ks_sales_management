package com.snack_bar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.machinezoo.sourceafis.FingerprintTemplate;
import com.snack_bar.database.BackupData;
import com.snack_bar.network.ApiClient;
import com.snack_bar.network.ApiInterface;
import com.snack_bar.network.ServerResponse;
import com.snack_bar.util.Helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadImageToServer extends AppCompatActivity implements BackupData.OnBackupListener {

    private TextView result;
    private Button btnCompareImage, uploadImage,btnBackUp,btnRestoreDB;
    private ImageView imageSelected,imageSelected1;
    private int PICK_PROFILE_IMAGE_REQUEST = 1;
    private Bitmap bitmap;
    private Helper helper ;
    private String postPath;
    private String postPath1;
    private String mediaPath;
    private String imageViewClicked;
    ProgressDialog pDialog;
    private BackupData backupData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image_to_server);
        result = (TextView) findViewById(R.id.result);
        imageSelected = (ImageView) findViewById(R.id.imageSelected);
        imageSelected1 = (ImageView) findViewById(R.id.imageSelected1);
        btnCompareImage = (Button) findViewById(R.id.btnCompareImage);
        btnBackUp = (Button) findViewById(R.id.btnBackupDB);
        btnRestoreDB = (Button) findViewById(R.id.btnRestoreDB);
        helper = new Helper();
        //BACK UP DATA
        backupData = new BackupData(this);
        backupData.setOnBackupListener(this);
        initDialog();
        imageSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.setText("");
                imageViewClicked = "LI";
                showFileChooser(PICK_PROFILE_IMAGE_REQUEST);
            }
        });
        imageSelected1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.setText("");
                imageViewClicked = "RI";
                //showFileChooser(PICK_PROFILE_IMAGE_REQUEST);
                PICK_PROFILE_IMAGE_REQUEST=0;//ALL FILES
                showFileChooser();
            }
        });
        btnCompareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (postPath != null && postPath1 != null) {
                    result.setText("Recherche en cours...");
                    File file = new File(postPath);
                    File file1 = new File(postPath1);
                    byte[] probeImage = null;
                    byte[] candidateImage = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        try {
                            probeImage = Files.readAllBytes(file.toPath());
                            candidateImage = Files.readAllBytes(file1.toPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    //CREATE TEMPLATE FOR THEM
                    FingerprintTemplate probe = helper.createTemplate(probeImage);
                    FingerprintTemplate candidate = helper.createTemplate(candidateImage);
                    if (helper.verifySingleFingerPrint(probe, candidate)) {
                        Toast.makeText(getApplicationContext(), "MATCH!!", Toast.LENGTH_LONG).show();
                        result.setText("MATCH!!");
                    } else {
                        Toast.makeText(getApplicationContext(), "NO MATCH FOUND!!", Toast.LENGTH_LONG).show();
                        result.setText("NO MATCH FOUND!!");
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "PLEASE SELECT THE IMAGES...", Toast.LENGTH_LONG).show();
                }
            }
        });
        uploadImage = (Button) findViewById(R.id.btnUpload);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile(postPath);
            }
        });
        btnBackUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backupData.exportToSD();
            }
        });
        btnRestoreDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backupData.importFromSD();
            }
        });
    }

    //The Chooser Intent to select Image via the gallery
    private void showFileChooser(int number) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        switch (number) {
            case 1:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_PROFILE_IMAGE_REQUEST);
                break;
        }
    }
    //ALL FILES
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    PICK_PROFILE_IMAGE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    protected void initDialog() {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading....");
        pDialog.setCancelable(true);
    }
    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }
    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("FILES","DATA : "+data);
        if (resultCode == RESULT_OK) {

                if (data != null) {
                    // Get the Image from data
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    assert cursor != null;
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    mediaPath = cursor.getString(columnIndex);
                    if (requestCode == 1) {
                        // Set the Image in ImageView for Previewing the Media
                        if (imageViewClicked == "LI") {
                            postPath = mediaPath;
                            imageSelected.setImageBitmap(BitmapFactory.decodeFile(mediaPath));
                        } else {
                            imageSelected1.setImageBitmap(BitmapFactory.decodeFile(mediaPath));
                            postPath1 = mediaPath;
                        }
                    }else{
                        // Get the Uri of the selected file
                        Uri uri = data.getData();
                        Log.d("FILES", "File Uri: " + uri.toString());
                        // Get the path
                        String path = null;
                        try {
                            path = getPath(this, uri);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        Log.d("FILES", "File Path: " + path);
                        // Get the file instance
                        // File file = new File(path);
                        // Initiate the upload
                        postPath = path;
                    }
                    cursor.close();
                }
        }
        else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show();
        }
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    public byte[] getBytesImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte[] imageBytes = baos.toByteArray();
        return imageBytes;
    }

    // Uploading Image/Video
    private void uploadFile(String postPath) {

        if (postPath == null || postPath.equals("")) {
            Toast.makeText(this, "please select an image ", Toast.LENGTH_LONG).show();
            return;
        } else {
            showpDialog();

            // Map is used to multipart the file using okhttp3.RequestBody
            Map<String, RequestBody> map = new HashMap<>();
            File file = new File(postPath);

            // Parsing any Media type file
            RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
            map.put("file\"; filename=\"" + file.getName() + "\"", requestBody);
            ApiInterface getResponse = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponse> call = getResponse.upload("token", map);
            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    if (response.isSuccessful()){
                        if (response.body() != null){
                            hidepDialog();
                            ServerResponse serverResponse = response.body();
                            Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }else {
                        hidepDialog();
                        Toast.makeText(getApplicationContext(), "problem uploading image", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    hidepDialog();
                    Log.d("Response gotten is", t.getMessage());
                }
            });
        }
    }

    @Override
    public void onFinishExport(String error) {
        if(error !=null) {
            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "BACKUP DONE....", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onFinishImport(String error) {
        if(error !=null) {
            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "BACKUP DONE....", Toast.LENGTH_SHORT).show();
        }
    }
}