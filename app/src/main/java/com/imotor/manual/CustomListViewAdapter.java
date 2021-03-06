package com.imotor.manual;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * An array adapter that knows how to render views when given CustomData classes
 */
public class CustomListViewAdapter extends BaseAdapter {

    private String TAG = "CustomListViewAdapter";

    private LayoutInflater mInflater;
    private List<Uri> mUris;
    private Context mContext;
    private OnItemClickLitener mOnItemClickLitener;
    private int mSelectionPosion = -1;
    private int mNormalBg;
    private int mSelectedBg;
//    private int mNormalBg;

    public CustomListViewAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//LayoutInflater.from(mContext);
    }

    public CustomListViewAdapter(Context context, List<Uri> uris) {
        Log.d(TAG, "CustomListViewAdapter--");
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//LayoutInflater.from(mContext);
        mUris = uris;
        mContext = context;
        Resources resources = mContext.getResources();
//        mNormalBg = resources.getDrawable(R.drawable.bg);//
//        mSelectedBg = resources.getDrawable(R.drawable.bg2);
        mSelectedBg = resources.getColor(R.color.colorPrimaryDark);
        mSelectedBg = resources.getColor(R.color.colorAccent);//
    }

    public interface OnItemClickLitener {
        void onItemClick(int position);
    }

    public int getCount() {
        Log.d(TAG, "getCount--" + mUris.size());
        return mUris.size();
    }

    public Object getItem(int position) {
        Log.d(TAG, "getItem--" + position);
        return mUris.get(position);
    }

    public long getItemId(int position) {
        Log.d(TAG, "getItemId--" + position);
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView--" + position);
        ViewHolder viewHolder = null;

        if (convertView == null || convertView.getTag() == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.custom_list_view, null);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Log.w(TAG, "getView--mSelectionPosion--" + mSelectionPosion);
        viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image);

 /*       if (position == mSelectionPosion) {
//                viewHolder.imageView.setBackgroundColor(mSelectedBg);
            convertView.setBackgroundColor(mSelectedBg);
        } else {
//                viewHolder.imageView.setBackgroundColor(mNormalBg);
            convertView.setBackgroundColor(mNormalBg);
        }*/
        Bitmap bitmap = miniImageUri(mUris.get(position), 60);
        viewHolder.imageView.setImageBitmap(bitmap);

        return convertView;
    }

    /**
     * View holder for the views we need access to
     */
    public class ViewHolder {
        public ImageView imageView;
    }

    private Bitmap miniImageUri(Uri uri, int targetHeight) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
//            Bitmap bitmap =getBitmapFormUri(mContext,uri);
            if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                return null;
            }
            bitmap = miniSizeImageView(targetHeight, bitmap);//scale zip
            return compressImage(bitmap,100);//compress zip
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public  Bitmap getBitmapFormUri(Context context, Uri uri) throws FileNotFoundException, IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        int scaled = 1;
        //
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = scaled;//
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return bitmap;//
    }

    public  Bitmap compressImage(Bitmap image ,int size) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, size, baos);//
        int options = 100;
        while (baos.toByteArray().length / 1024 > size) {  //
            baos.reset();//重置baos即清空baos

            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//
            options -= 10;//
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//
        return bitmap;
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

    /**
     * @param position
     */
    public void setSelectPosition(int position) {
        Log.d(TAG, "setSelectPosition--" + position);
        if (!(position < 0 || position > mUris.size())) {
            mSelectionPosion = position;
//            notifyDataSetChanged();

        }
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

}
