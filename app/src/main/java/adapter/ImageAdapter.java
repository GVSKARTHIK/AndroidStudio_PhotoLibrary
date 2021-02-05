package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.androidphotos42.MainActivity;
import com.example.androidphotos42.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import Model.Photo;

public class ImageAdapter  extends BaseAdapter {

    private Context context;
    public ArrayList<Photo> allPhotos = MainActivity.driver.getCurrentAlbum().allPhotos;

    public ImageAdapter(Context context, ArrayList<Photo>allPhotos){
        this.context = context;
        this.allPhotos = allPhotos;
    }

    @Override
    public int getCount() {

        return allPhotos.size();
    }

    @Override
    public Object getItem(int position) {
        return allPhotos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Photo photo = allPhotos.get(position);
        System.out.println(photo.photoFilePath+" "+photo.photoname);
        Bitmap bitmap = loadImageFromStorage(photo.photoFilePath,photo.photoname);
        if (convertView == null) {
            convertView = (View) inflater.inflate(R.layout.content_album, parent,false);

        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageview);
        imageView.setImageBitmap(bitmap);
        return convertView;

    }

    private Bitmap loadImageFromStorage(String path,String filename)
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

}
