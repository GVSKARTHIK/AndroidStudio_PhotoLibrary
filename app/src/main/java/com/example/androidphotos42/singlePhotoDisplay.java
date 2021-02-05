package com.example.androidphotos42;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import Model.AlbumList;
import Model.Photo;
import Model.Tag;

public class singlePhotoDisplay extends AppCompatActivity {
    public ImageView imageView;
    public Button  addTag, deleteTag;
    public ListView listViewT;
    public TextView filename;
    public EditText tagName, tagValue;

    //Array Lists
    public static ArrayList<Photo> allPhotos = new ArrayList<Photo>();
    public ArrayList<Tag> allTags = MainActivity.driver.getCurrentAlbum().currPhoto.getTags();
    public Photo photo;

    //Adapter
    public ArrayAdapter<Tag> adapter;
    //private ArrayAdapter<String> tagsAdapter;

    //Indices
    public int currIndex=0;
    public int photoIndex;
    public int previousIndex;
    public int nextIndex = 0;

    public AlbumList driver = MainActivity.driver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_photo_display);

        listViewT = (ListView) findViewById(R.id.listViewT);
        adapter = (ArrayAdapter<Tag>) listViewT.getAdapter();
        adapter = new ArrayAdapter<>(this, R.layout.content_single_photo_display, driver.getCurrentAlbum().getPhoto().getTags());
        listViewT.setAdapter(adapter);
        imageView = (ImageView) findViewById(R.id.imageView);


        filename = (TextView) findViewById(R.id.filename);
        filename.setText(MainActivity.driver.getCurrentAlbum().getPhoto().photoname);

        tagName =(EditText)findViewById(R.id.tagName);
        tagValue =(EditText) findViewById(R.id.tagValue);
        addTag = (Button) findViewById(R.id.addTag);
        deleteTag =(Button) findViewById(R.id.deleteTag);


        if (MainActivity.driver.getCurrentAlbum().getPhotos().size() != 0) {
            spinUpDisplay();
        }

        displayTags();

        update();


    }



    public void nextPhoto(View view) {
        populatePhotoList();
        if(currIndex + 1 > allPhotos.size()-1) {
            return;
        } else {
            currIndex++;
            Photo photo = allPhotos.get(currIndex);
            MainActivity.driver.getCurrentAlbum().setCurrentPhoto(photo);
                Photo currPhoto = MainActivity.driver.getCurrentAlbum().getPhoto();
                Bitmap bitmap = loadImageFromStorage(currPhoto.photoFilePath,currPhoto.photoname);
            filename.setText(MainActivity.driver.getCurrentAlbum().getPhoto().photoname);
                imageView.setImageBitmap(bitmap);
                MainActivity.driver.getCurrentAlbum().setCurrentPhoto(photo);
                displayTags();

        }
    }



    public void previousPhoto(View view) {
        populatePhotoList();
        if(currIndex-1 < 0) {
            return;
        }

        else {
            currIndex--;
            Photo photo = allPhotos.get(currIndex);
            MainActivity.driver.getCurrentAlbum().setCurrentPhoto(photo);
            filename.setText(MainActivity.driver.getCurrentAlbum().getPhoto().photoname);
                Photo currPhoto = MainActivity.driver.getCurrentAlbum().getPhoto();
                Bitmap bitmap = loadImageFromStorage(currPhoto.photoFilePath,currPhoto.photoname);
                imageView.setImageBitmap(bitmap);
                MainActivity.driver.getCurrentAlbum().setCurrentPhoto(photo);
                displayTags();
        }
    }



    public void displayTags() {
        photo = MainActivity.driver.getCurrentAlbum().getPhoto();
        System.out.println(photo.getTags());
        if (photo != null) {
            ArrayList<Tag> temp = MainActivity.driver.getCurrentAlbum().getPhoto().getTags();

            //TBD: Set up adapter instead
            adapter = new ArrayAdapter<>(this, R.layout.content_single_photo_display, temp);
            listViewT = findViewById(R.id.listViewT);
            listViewT.setAdapter(adapter);
        }

    }




    public void populatePhotoList(){
        allPhotos.clear();
        for (int i = 0; i < MainActivity.driver.getCurrentAlbum().getPhotos().size(); i++){
            allPhotos.add(MainActivity.driver.getCurrentAlbum().getPhotos().get(i));
        }
    }


    private Bitmap loadImageFromStorage(String path, String filename)
    {

        try {
            File f=new File(path, filename);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return  b;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public void spinUpDisplay(){
        Photo currPhoto = MainActivity.driver.getCurrentAlbum().getPhoto();
        Bitmap bitmap = loadImageFromStorage(currPhoto.photoFilePath,currPhoto.photoname);
        imageView.setImageBitmap(bitmap);
    }




    public void addTags(View view) {

        adapter = (ArrayAdapter<Tag>) listViewT.getAdapter();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);


        String tagname = tagName.getText().toString();
        String tagvalue = tagValue.getText().toString();

        Tag tag = (tagname.isEmpty() || tagvalue.isEmpty()) ? null : new Tag(tagname, tagvalue);
        if (tag == null) {
            new AlertDialog.Builder(dialogBuilder.getContext())
                    .setMessage("Make sure tagname and tagvalue are not empty.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        if (!checkTags(tag)) {
            new AlertDialog.Builder(dialogBuilder.getContext())
                    .setMessage("This tag already exists.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        //Only allows Person and Location
        if (!(tagname.equals("Person") || tagname.equals("Location"))){
            new AlertDialog.Builder(dialogBuilder.getContext())
                    .setMessage("Please enter 'Person' or 'Location' for Tag Name.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        MainActivity.driver.getCurrentAlbum().getPhoto().addNewTag(tagname,tagvalue);
       // adapter.add(tag);

        try{
            AlbumList.save(driver);

        }catch(Exception e){
            e.printStackTrace();
        }
        update();
        dialogBuilder.show();
        listViewT.setAdapter(adapter);

    }



    public void deleteTags(View view) {

        adapter = (ArrayAdapter<Tag>) listViewT.getAdapter();
        List<Tag> tags = MainActivity.driver.getCurrentAlbum().getPhoto().getTags();
        if (tags.size() <= 0) {
            new AlertDialog.Builder(this)
                    .setMessage("There are no tags.")
                    .setPositiveButton("OK", null)
                    .show();

            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int index = listViewT.getCheckedItemPosition();
        if (index == -1) {
            new AlertDialog.Builder(this)
                    .setMessage("Select a tag and try again.")
                    .setPositiveButton("OK", null)
                    .show();

            return;
        }
         Tag checkedTag = (Tag)adapter.getItem(index);

        builder.setMessage("Are you sure you want to delete ?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                adapter.remove(checkedTag);
                listViewT.setItemChecked(index, true);

                Tag tag = allTags.get(index);
                MainActivity.driver.getCurrentAlbum().getPhoto().deleteTag(tag.getType(), tag.getValue());

                try{
                    AlbumList.save(driver);

                }catch(Exception e){
                    e.printStackTrace();
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



        update();

    }




    public boolean checkTags(Tag tag){
        List<Tag> allTags = MainActivity.driver.getCurrentAlbum().getPhoto().getTags();
        for (int i = 0; i < allTags.size(); i++){
            if (tag.equals(allTags.get(i))){
                return false;
            }
        }
        return true;
    }

    public void update(){
        allTags.clear();
        for(int i = 0; i < MainActivity.driver.getCurrentAlbum().getPhoto().getTags().size(); i++){
            allTags.add(MainActivity.driver.getCurrentAlbum().getPhoto().getTags().get(i));
        }
    }

}