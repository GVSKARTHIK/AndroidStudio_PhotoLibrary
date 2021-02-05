package com.example.androidphotos42;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

import Model.Album;
import Model.Photo;
import Model.Tag;
import adapter.ImageAdapter;

public class SearchPhoto extends AppCompatActivity {

    private Button searchTag;
    private EditText tagValue;
    private GridView gridViewS;


    public ArrayList<Photo> allPhotos = new ArrayList<Photo>();
    public ArrayList<Tag> allTags = new ArrayList<Tag>();
    public ImageAdapter imageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_photo);

        gridViewS = (GridView) findViewById(R.id.gridViewS);
        imageAdapter = new ImageAdapter(this,allPhotos);
        gridViewS.setAdapter(imageAdapter);


        searchTag = (Button) findViewById(R.id.searchTag);
        tagValue = (EditText) findViewById(R.id.tagValue);
        searchTag.setVisibility(View.VISIBLE);
        for(Album a: MainActivity.driver.getList()){
            for(Photo p: a.getPhotos()){
                for(Tag t: p.getTags()){
                    allTags.add(t);
                }
            }
        }
        searchTag.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                searchPhotoTags(allTags);
            }
        });


    }

    private void searchPhotoTags(ArrayList<Tag> tagSearch){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        if (tagSearch.size() == 0){
            new AlertDialog.Builder(dialogBuilder.getContext())
                    .setMessage("Tag List is empty.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        //HashSet to avoid duplicating Photos.
        HashSet<Photo> photoSet = new HashSet<Photo>();

        for (Album album : MainActivity.driver.getList()) {
            for (Photo photo : album.getPhotos()) {
                for(Tag t: photo.getTags()){
                    if(t.getValue().toLowerCase().contains(tagValue.getText().toString().toLowerCase())){
                        photoSet.add(photo);
                    }
                }
            }
        }
        allPhotos.addAll(photoSet);

        gridViewS = (GridView) findViewById(R.id.gridViewS);

        imageAdapter.allPhotos = allPhotos;
        imageAdapter.notifyDataSetChanged();
        gridViewS.setAdapter(imageAdapter);
    }



}