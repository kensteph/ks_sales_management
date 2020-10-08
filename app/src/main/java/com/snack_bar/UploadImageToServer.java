package com.snack_bar;

import android.app.ProgressDialog;
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
import com.snack_bar.network.ApiClient;
import com.snack_bar.network.ApiInterface;
import com.snack_bar.network.ServerResponse;
import com.snack_bar.util.Helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadImageToServer extends AppCompatActivity {

    private TextView result;
    private Button btnCompareImage, uploadImage;
    private ImageView imageSelected,imageSelected1;
    private int PICK_PROFILE_IMAGE_REQUEST = 1;
    private Bitmap bitmap;
    private Helper helper ;
    private String postPath;
    private String postPath1;
    private String mediaPath;
    private String imageViewClicked;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image_to_server);
        result = (TextView) findViewById(R.id.result);
        imageSelected = (ImageView) findViewById(R.id.imageSelected);
        imageSelected1 = (ImageView) findViewById(R.id.imageSelected1);
        btnCompareImage = (Button) findViewById(R.id.btnCompareImage);
        helper = new Helper();
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
                showFileChooser(PICK_PROFILE_IMAGE_REQUEST);
            }
        });
        btnCompareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.setText("Recherche en cours...");
//                Bitmap bitmap = ((BitmapDrawable)imageSelected.getDrawable()).getBitmap();
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                //LI
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                byte [] byte_arr = stream.toByteArray();
//                //CREATE TEMPLATE FOR THEM
//                FingerprintTemplate probe = helper.createTemplate(byte_arr);
//                //RI
//                ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
//                Bitmap bitmapR = ((BitmapDrawable)imageSelected1.getDrawable()).getBitmap();
//                bitmapR.compress(Bitmap.CompressFormat.JPEG, 100, stream1);
//                byte [] byte_arr_R = stream.toByteArray();
                File file = new File(postPath);
                File file1 = new File(postPath1);
                byte[] probeImage=null;
                byte [] candidateImage=null;
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
                if(helper.verifySingleFingerPrint(probe,candidate)){
                    Toast.makeText(getApplicationContext(),"MATCH!!",Toast.LENGTH_LONG).show();
                    result.setText("MATCH!!");
                }else{
                    Toast.makeText(getApplicationContext(),"NO MATCH FOUND!!",Toast.LENGTH_LONG).show();
                    result.setText("NO MATCH FOUND!!");
                }
            }
        });

        uploadImage = (Button) findViewById(R.id.btnUpload);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
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
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_PROFILE_IMAGE_REQUEST) {
                if (data != null) {
                    // Get the Image from data
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    assert cursor != null;
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    mediaPath = cursor.getString(columnIndex);
                    // Set the Image in ImageView for Previewing the Media
                    if(imageViewClicked =="LI") {
                        postPath = mediaPath;
                        imageSelected.setImageBitmap(BitmapFactory.decodeFile(mediaPath));
                    }else{
                        imageSelected1.setImageBitmap(BitmapFactory.decodeFile(mediaPath));
                        postPath1 = mediaPath;
                    }
                    cursor.close();
                }
            }
        }
        else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show();
        }
    }

    public byte[] getBytesImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte[] imageBytes = baos.toByteArray();
        return imageBytes;
    }

    // Uploading Image/Video
    private void uploadFile() {
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
                    Log.v("Response gotten is", t.getMessage());
                }
            });
        }
    }

}