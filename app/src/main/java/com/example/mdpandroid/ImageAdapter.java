package com.example.mdpandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.PicHolder> {
    public static class PicHolder extends RecyclerView.ViewHolder{

        public ImageView mimage_sent;

        public PicHolder(View itemview){
            super(itemview);
            mimage_sent=(ImageView)itemview.findViewById(R.id.image_gallery);
        }
    }

    private ArrayList<Bitmap> data;
    private Context context;

    public ImageAdapter(Context mcontext, ArrayList<Bitmap> mdata){
        context=mcontext;
        data=mdata;
    }

    private Context getContext() {
        return context;
    }


    @Override
    public ImageAdapter.PicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context=parent.getContext();
        LayoutInflater inflater= LayoutInflater.from(context);

        View galleryView=inflater.inflate(R.layout.imageItem, parent, false);

        PicHolder picHolder=new PicHolder(galleryView);


        return picHolder;
    }

    @Override
    public void onBindViewHolder(PicHolder holder, int position) {

        Bitmap bitmap= data.get(position);

        ImageView imageView=holder.mimage_sent;
        imageView.setImageBitmap(bitmap);

    }


    @Override
    public int getItemCount() {
        return data.size();
    }

}
