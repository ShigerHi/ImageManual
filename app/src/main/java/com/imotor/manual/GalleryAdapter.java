package com.imotor.manual;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class GalleryAdapter extends BaseAdapter {

    private static final String TAG = "GalleryAdapter";
    Context mContext;
    private LayoutInflater mInflater;
    private List<Uri> mImageUris;
    private LruCache<Uri, Bitmap> mBitmapLruCache;

    public GalleryAdapter(Context context, List<Uri> uris) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mImageUris = uris;
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        Log.w(TAG, "Max memory is " + maxMemory + "KB");
        int cacheSize = maxMemory / 8;
        mBitmapLruCache = new LruCache<Uri, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Uri key, Bitmap value) {
                Log.w(TAG,"sizeOf-Gallery--"+String.valueOf(value.getByteCount() / 1024));
                return value.getByteCount() / 1024;
            }
        };
    }

    public class ViewHolder {
        public ImageView imageView;
    }

    public int getCount() {
        return mImageUris.size();
    }

    public Object getItem(int position) {
        Log.d(TAG, "getItem--position" + position);
        return (Object) position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getDropDownView--position=" + position + "  parent.count-" + parent.getChildCount());
        return super.getDropDownView(position, convertView, parent);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        Log.d(TAG, "getView--position=" + position);
        Log.d(TAG, "getView-- parent.count=" + parent.getChildCount());

        ViewHolder viewHolder = null;

        if (convertView == null || convertView.getTag() == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.custom_list_view, null);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image);

//        Bitmap bitmap = miniImageUri(mImageUris.get(position), 60);
//        viewHolder.imageView.setImageBitmap(bitmap);

        Uri uri = mImageUris.get(position);
        Bitmap bitmap = mBitmapLruCache.get(uri);
        if ( bitmap == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            bitmap = BitmapFactory.decodeFile(uri.getPath(), options);
            bitmap = compressImage(bitmap,2);
            mBitmapLruCache.put(uri, bitmap);
        }

        viewHolder.imageView.setImageBitmap(bitmap);

        return convertView;
    }

    private Bitmap miniImageUri(Uri uri, int targetHeight) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
//            Bitmap bitmap =getBitmapFormUri(mContext,uri);
            if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                return null;
            }
            bitmap = miniSizeImageView(targetHeight, bitmap);//scale zip
            return compressImage(bitmap, 10);//compress zip
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap miniSizeImageView(int targetHeight, Bitmap bitmap) {

        Matrix matrix = new Matrix();
        Log.d(TAG, "input--w-" + bitmap.getWidth() + "-h-" + bitmap.getHeight());
        float rateY = (float) targetHeight / bitmap.getHeight();
        float scale = rateY;//1.5　－－－screen dentisity
        Log.d(TAG, "scale==" + scale);
        matrix.setScale(scale, scale); //
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        Log.w(TAG, "output-w--" + bitmap.getWidth() + "-h-" + bitmap.getHeight());
        return bitmap;
    }

    public Bitmap compressImage(Bitmap image, int size) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, size, baos);//
        int options = 10;
        while (baos.toByteArray().length / 1024 > size) {  //
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//

            options -= 10;//
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//\

        return bitmap;
    }

}