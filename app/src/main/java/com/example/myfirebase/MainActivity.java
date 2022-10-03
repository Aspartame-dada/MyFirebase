package com.example.myfirebase;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myfirebase.MyUtils.ImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button chooseImg ,chooseVedio;
    ImageView imgView;
    private static  int TAKE_PICTURE = 112;
    private static  int TAKE_VEDIO = 113;

    private static int REQUEST_PERMISSION_CODE = 222;
    Uri filePath;
    ProgressDialog pd;
    private LocationManager lm;
    private String loc_msg="Location 1160";
    String currentPhotoPath,imgplace;
    private ImageLoader imageLoader=new ImageLoader(MainActivity.this);

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
    };
    //creating reference to firebase storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://my-firebase-d087a.appspot.com");    //change the url according to your firebase app


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ask permission
        verifyStoragePermissions(MainActivity.this);
        try {
            initView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void initView() throws IOException {

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationUpdate();
        verifyStoragePermissions(MainActivity.this);
        chooseImg = (Button) findViewById(R.id.chooseImg);
        imgView = (ImageView) findViewById(R.id.imgView);
        chooseVedio=findViewById(R.id.chooseVedio);

        pd = new ProgressDialog(this);
        pd.setMessage("Uploading....");


        chooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();            }
        });
        chooseVedio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TAG", "onClick: ----------is click");
                dispatchTakeVedioIntent();
            }
        });

    }
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.CALL_PHONE) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }

    }}
    public void upCloudPhoto(){
        if (filePath != null) {
            pd.show();

            StorageReference childRef = storageRef.child(currentPhotoPath);
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .setCustomMetadata("location", loc_msg)
                    .build();

            //uploading the image
            UploadTask uploadTask = childRef.putFile(filePath, metadata);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    pd.dismiss();
                    Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(MainActivity.this, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Select an image", Toast.LENGTH_SHORT).show();
        }
    }
    public void upCloudVedio(){
        Log.i("TAG", "upCloudVedio: ------------------"+filePath);
        if (filePath != null) {
            pd.show();

            StorageReference childRef = storageRef.child(currentPhotoPath);
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("videos/mp4")
                    .setCustomMetadata("location", loc_msg)
                    .build();

            //uploading the image
            UploadTask uploadTask = childRef.putFile(filePath, metadata);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    pd.dismiss();
                    Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(MainActivity.this, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Select an image", Toast.LENGTH_SHORT).show();
        }
    }



    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            photoFile = createImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    /*7.0以上要通过FileProvider将File转化为Uri*/
                    photoURI = FileProvider.getUriForFile(this, "com.example.myfirebase.fileprovider", photoFile);
                } else {
                    /*7.0以下则直接使用Uri的fromFile方法将File转化为Uri*/
                    photoURI = Uri.fromFile(photoFile);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_PICTURE);
            }
        }
    }
    private void dispatchTakeVedioIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            photoFile =createVedioFile();
            Log.i("TAG", "createVedioFile: ---------3");

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    /*7.0以上要通过FileProvider将File转化为Uri*/
                    photoURI = FileProvider.getUriForFile(this, "com.example.myfirebase.fileprovider", photoFile);
                } else {
                    /*7.0以下则直接使用Uri的fromFile方法将File转化为Uri*/
                    photoURI = Uri.fromFile(photoFile);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_VEDIO);
                Log.i("TAG", "createVedioFile: ---------4");

            }
        }
    }

    private File createImageFile() {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = new File(Environment.getExternalStorageDirectory(), "AAAAA/"+loc_msg);
        if (!storageDir.exists())
            storageDir.mkdirs();
        File currentImageFile = new File(storageDir, imageFileName+imgplace+ ".jpg");
        if (!currentImageFile.exists()) {
            try {
                currentImageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = currentImageFile.getAbsolutePath();
        return currentImageFile;
    }
    private File createVedioFile() {
        Log.i("TAG", "createVedioFile: ---------1");
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String imageFileName = "VEDIO_" + timeStamp;
        File storageDir = new File(Environment.getExternalStorageDirectory(), "AAAAA/"+loc_msg);
        if (!storageDir.exists())
            storageDir.mkdirs();
        File currentImageFile = new File(storageDir, imageFileName+imgplace+ ".mp4");
        if (!currentImageFile.exists()) {
            try {
                currentImageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = currentImageFile.getAbsolutePath();
        filePath=Uri.fromFile(currentImageFile);
        Log.i("TAG", "createVedioFile: ---------2");

        return currentImageFile;
    }

    //add album
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("TAG", "onActivityResult: -------------------"+filePath);
        Log.i("TAG", "createVedioFile: ---------5");
        Log.i("TAG", "onActivityResult: -------------------"+resultCode+" "+requestCode);


        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PICTURE|| resultCode == RESULT_OK) {
            galleryAddPic();
            //如果写入本地//takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);data就会变成null
            if (data != null) {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
            } else {
//                Toast.makeText(this, "data is null", Toast.LENGTH_LONG).show();
                File file = new File(currentPhotoPath);
                filePath= Uri.fromFile(file);
                Log.i("TAG", "onActivityResult: ");
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(filePath));
                    Log.i("TAG", "onActivityResult: ----------------------"+currentPhotoPath);
                    imageLoader.displayImage(bitmap,imgView);
                    upCloudPhoto();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }
       if(requestCode == TAKE_VEDIO && resultCode == RESULT_OK){
            Log.i("TAG", "createVedioFile: ---------6");

            galleryAddPic();
            //如果写入本地//takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);data就会变成null
//                Toast.makeText(this, "data is null", Toast.LENGTH_LONG).show();
                File file = new File(currentPhotoPath);
                filePath= Uri.fromFile(file);
                Log.i("TAG", "onActivityResult: ");
                upCloudVedio();

        }

    }

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0x001) {

                Log.i("TAG", "handleMessage: ------------------"+loc_msg);


            }

            return false;
        }
    });

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // 当GPS定位信息发生改变时，更新定位
            try {
                updateShow(location);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

            // 如果没权限，打开设置页面让用户自己设置
            if (checkCallingOrSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(MainActivity.this, "请打开GPS~", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 0);
                return;
            }

            // 当GPS LocationProvider可用时，更新定位
            try {
                updateShow(lm.getLastKnownLocation(provider));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            try {
                updateShow(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    //定义一个更新显示的方法
    private void updateShow(Location location) throws IOException {
        if (location != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Location"+(int)location.getLongitude()+" ");
            sb.append(+(int)location.getLatitude());


            imgplace = sb.toString();
            Geocoder gc = new Geocoder(this, Locale.getDefault());
            String str  = String.valueOf(gc.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1));

            loc_msg =str.substring(str.indexOf("admin="),str.indexOf(",sub"));
//            loc_msg=s.substring(s.indexOf(4),s.indexOf());

        } else loc_msg = "";

        handler.sendEmptyMessage(0x001);
    }

    public void locationUpdate() throws IOException {

        // 如果没权限，打开设置页面让用户自己设置
        if ( checkCallingOrSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(MainActivity.this, "请打开GPS~", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }

        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        updateShow(location);

        // 设置间隔两秒获得一次 GPS 定位信息
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 8,mLocationListener);
    }


}