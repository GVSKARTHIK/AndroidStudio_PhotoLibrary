package com.example.androidphotos42;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import Model.*;
import adapter.ImageAdapter;

public class AlbumActivity extends AppCompatActivity {

    private Button deletePhotoButton, movePhotoButton, openPhotoButton;
    private FloatingActionButton addPhoto;

    public  ArrayList<Photo> allPhotos = new ArrayList<Photo>();
    public GridView gridView;
    public ImageAdapter imageAdapter;
    public final int REQUEST_CODE = 1;
    public AlbumList albumList= MainActivity.driver;
    public Album album;
    static AlbumList driver;
    public Photo file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        gridView = (GridView) findViewById(R.id.gridView);
        imageAdapter = new ImageAdapter(this, albumList.getCurrentAlbum().allPhotos);
        gridView.setAdapter(imageAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Photo p = MainActivity.driver.getCurrentAlbum().allPhotos.get(position);
                MainActivity.driver.getCurrentAlbum().setCurrentPhoto(p);
            }
        });

        deletePhotoButton = (Button) findViewById(R.id.deletePhotoButton);
        movePhotoButton = (Button) findViewById(R.id.movePhotoButton);
        openPhotoButton = (Button) findViewById(R.id.openPhotoButton);

        populate();
          deletePhotoButton.setVisibility(View.INVISIBLE);
          movePhotoButton.setVisibility(View.INVISIBLE);
         openPhotoButton.setVisibility(View.INVISIBLE);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                  deletePhotoButton.setVisibility(View.VISIBLE);
                  movePhotoButton.setVisibility(View.VISIBLE);
                 openPhotoButton.setVisibility(View.VISIBLE);

                deletePhotoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deletePhoto(position);
                    }
                });

                movePhotoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        movePhoto(position);
                    }
                });

                openPhotoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openPhoto(position);
                    }
                });
            }
        });

        FloatingActionButton add = findViewById(R.id.addPhoto);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPhoto();
            }
            public void addPhoto() {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to add \""  + "\"?");
        builder.setPositiveButton("Add",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if(requestCode == REQUEST_CODE ){

                            Uri photoPath = null;

                            if(data != null){
                                photoPath = data.getData();
                            }

                            ImageView iv = new ImageView(AlbumActivity.this);
                            iv.setImageURI(photoPath);

                            BitmapDrawable drawable = (BitmapDrawable) iv.getDrawable();
                            Bitmap selectedImageGal = drawable.getBitmap();

                            Photo photo = new Photo(photoPath.toString());
                            photo.setPhoto(selectedImageGal);

                            File f = new File(photoPath.getPath());
                            String pathID = f.getAbsolutePath();
                            String filename = fileNamePath(photoPath);


                            photo.setName(filename);

                            String path = saveToInternalStorage(selectedImageGal,filename);
                            photo.photoFilePath = path;


                           MainActivity.driver.getCurrentAlbum().addPhoto(photo);


                            try{

                                AlbumList.save(MainActivity.driver);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            gridView =(GridView) findViewById(R.id.gridView);
                            populate();
                            imageAdapter.allPhotos = MainActivity.driver.getCurrentAlbum().allPhotos;
                            imageAdapter.notifyDataSetChanged();

                            gridView.setAdapter(imageAdapter);

                        }
                    }
                });
        builder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder.show();

    }

    private String fileNamePath(Uri selectedImage){

        String filename = "not found";
        String[] column = {MediaStore.MediaColumns.DISPLAY_NAME};
        ContentResolver cr = getApplicationContext().getContentResolver();
        Cursor cursor = cr.query(selectedImage, column,
                null, null, null);
        if(cursor != null) {
            try {
                if (cursor.moveToFirst()){
                    filename = cursor.getString(0);
                }
            } catch (Exception e){

            }
        }

        return filename;
    }

    private String saveToInternalStorage(Bitmap bitmapImage,String filename){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath=new File(directory,filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);

            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }


    public void deletePhoto(int index){
        imageAdapter = (ImageAdapter)gridView.getAdapter();
        if (imageAdapter.getCount() == 0) {
            new AlertDialog.Builder(this)
                    .setMessage("There are no photos to delete.")
                    .setPositiveButton("OK", null)
                    .show();

            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        // int index = gridView.getCheckedItemPosition();
        Photo currentPhoto = (Photo) imageAdapter.getItem(index);
        dialogBuilder.setMessage("Are you sure you want to delete?");
        dialogBuilder.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.driver.getCurrentAlbum().remove(index);
                        allPhotos.remove(currentPhoto);
                        imageAdapter.allPhotos.remove(currentPhoto);
                        imageAdapter.notifyDataSetChanged();
                        gridView.setItemChecked(index, true);

                        try{

                            AlbumList.save(MainActivity.driver);

                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });
        dialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });


        try{

            AlbumList.save(MainActivity.driver);
        }catch(Exception e){
            e.printStackTrace();
        }
        populate();
        imageAdapter.notifyDataSetChanged();
        gridView.setAdapter(imageAdapter);
        // gridView.setAdapter(imageAdapter);
        dialogBuilder.show();
        deletePhotoButton.setVisibility(View.INVISIBLE);
        movePhotoButton.setVisibility(View.INVISIBLE);
        openPhotoButton.setVisibility(View.INVISIBLE);

    }


    public void movePhoto(int index){
        imageAdapter = (ImageAdapter)gridView.getAdapter();
        if (imageAdapter.getCount() == 0) {
            new AlertDialog.Builder(this)
                    .setMessage("There are no albums.")
                    .setPositiveButton("OK", null)
                    .show();

            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        // int index = gridView.getCheckedItemPosition();
        Photo currentPhoto = (Photo) imageAdapter.getItem(index);
        dialogBuilder.setMessage("Enter the Name of the Album to move?");
        final EditText input = new EditText(this);
        dialogBuilder.setView(input);

        dialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();

            }
        });

        dialogBuilder.setPositiveButton("Move", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String albumNameToMove = input.getText().toString().trim();
                boolean albumCheck = false;

                if(albumNameToMove.isEmpty() || albumNameToMove == null) {
                    new AlertDialog.Builder(dialogBuilder.getContext()).setMessage("Album name cannot be empty").setPositiveButton("OK", null).show();
                    return;
                }
                int idx =0;
                for(int i = 0; i < MainActivity.driver.allAlbums.size(); i++){
                    Album album = MainActivity.driver.allAlbums.get(i);
                    if(album.getName().equals(albumNameToMove)){
                        idx = i;
                        albumCheck = !albumCheck;
                    }
                }

                if(albumCheck){
                    MainActivity.driver.allAlbums.get(idx).addPhoto(MainActivity.driver.getCurrentAlbum().allPhotos.get(index));
                    MainActivity.driver.getCurrentAlbum().remove(index);
                }
                else{
                    new AlertDialog.Builder(dialogBuilder.getContext()).setMessage("Album name is invalid").setPositiveButton("OK", null).show();
                    return;
                }

            }
        });
        try{

            AlbumList.save(MainActivity.driver);
        }catch(Exception e){
            e.printStackTrace();
        }
        gridView =(GridView) findViewById(R.id.gridView);
        populate();
        imageAdapter.notifyDataSetChanged();
        gridView.setAdapter(imageAdapter);
        dialogBuilder.show();

        deletePhotoButton.setVisibility(View.INVISIBLE);
        movePhotoButton.setVisibility(View.INVISIBLE);
        openPhotoButton.setVisibility(View.INVISIBLE);

    }

    public void openPhoto(int index){
        Photo photo = MainActivity.driver.getCurrentAlbum().getPhotos().get(index);
        MainActivity.driver.getCurrentAlbum().setCurrentPhoto(photo);
        Intent intent = new Intent(this, singlePhotoDisplay.class);
        intent.putExtra("index", index);
        startActivity(intent);
    }



    public void populate(){
        allPhotos.clear();
        allPhotos.addAll(MainActivity.driver.getCurrentAlbum().getPhotos());
    }
}