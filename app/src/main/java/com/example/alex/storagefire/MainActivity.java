package com.example.alex.storagefire;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int CHOOSER_IMAGES = 1 ;
    private static final String TAG ="mainActivity" ;
    private Button btnDownload,upLoad;
    private ImageView imageView;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storageReference = FirebaseStorage.getInstance().getReference();

        btnDownload     =findViewById(R.id.btnDownload);
        upLoad          =findViewById(R.id.btnUpload);
        imageView       =findViewById(R.id.imv);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Selecciona una imagen"),CHOOSER_IMAGES);
            }
        });


        upLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageReference misImagenes = storageReference.child("lala.png") ;
                imageView.setDrawingCacheEnabled(true);
                imageView.buildDrawingCache();
                Bitmap bitmap = imageView.getDrawingCache();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);

                byte[] misImagenesBite = baos.toByteArray();
                UploadTask uploadTask = misImagenes.putBytes(misImagenesBite);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"ocurrio un error");
                        e.printStackTrace();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(MainActivity.this, "Subida con Exito", Toast.LENGTH_SHORT).show();
                        String downloadUri = taskSnapshot.getDownloadUrl().getPath();
                        Log.w(TAG,"image Url: "+downloadUri);

                    }
                });


            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final File file;
                try{
                    file = File.createTempFile("lala","png");
                    storageReference.child("lala.png").getFile(file)
                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                    imageView.setImageBitmap(bitmap);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG,"Ocurrio Un Error al Mostrar Imagen");
                            e.printStackTrace();
                        }
                    });
                }catch (Exception e){
                    Log.e(TAG,"Ocurrio un error En la Descarga de imagenes");
                            e.printStackTrace();
                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CHOOSER_IMAGES){
            Uri imageuri = data.getData();
            if(imageuri != null){
                imageView.setImageURI(imageuri);
            }
        }
    }
}
