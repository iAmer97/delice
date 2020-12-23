package android.example.instagramclone;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

public class ImageAdapter extends PagerAdapter {
    public static int number;
    private Context mContext;
    public ArrayList<Uri> imageuris = Posting.imageuris;

    public static int getNumber() {
        return number;
    }

    public static void setNumber(int number) {
        ImageAdapter.number = number;
    }

    ImageAdapter(Context context){
        mContext = context;
    }


    @Override
    public int getCount() {
        return getNumber();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageview = new ImageView(mContext);
        imageview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageview.setImageURI(imageuris.get(position));
        container.addView(imageview , 0);
        return imageview;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
    }
}
