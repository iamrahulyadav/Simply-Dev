package com.builder.ibalance.adapters;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.builder.ibalance.R;
import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.util.MyApplication;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.util.List;
 
 
public class CustomContactsAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<ContactDetailModel> contactItems;
    Typeface tf;
    CircleTransform mCircleTransform = new CircleTransform(MyApplication.context);
    public CustomContactsAdapter(List<ContactDetailModel> contactItems) {
    	  inflater = (LayoutInflater)MyApplication.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	tf = Typeface.createFromAsset(MyApplication.context.getResources().getAssets(), "Roboto-Regular.ttf");
        this.contactItems = contactItems;
      
    }
 
    @Override
    public int getCount() {
        return contactItems.size();
    }
 
    @Override
    public Object getItem(int location) {
        return contactItems.get(location);
        
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
 //TODO: use View holder pattern
          
        if (convertView == null)
            convertView = inflater.inflate(R.layout.contact_list_item, null);
        
        
        ImageView contactPicture = (ImageView) convertView
                .findViewById(R.id.contact_picture);
        TextView contact_name = (TextView) convertView.findViewById(R.id.contact_name);
        contact_name.setTypeface(tf);
        TextView contact_number = (TextView) convertView.findViewById(R.id.contact_number);
        contact_name.setTypeface(tf);
        TextView circle_state = (TextView) convertView.findViewById(R.id.circle);
        circle_state.setTypeface(tf);
        TextView total_duration = (TextView) convertView.findViewById(R.id.toatal_duration);
        total_duration.setTypeface(tf);
        TextView cost = (TextView) convertView.findViewById(R.id.cost);
        cost.setTypeface(tf);
        // getting movie data for the row
        ContactDetailModel m = contactItems.get(position);
        convertView.setTag(m);
        // thumbnail image
        //thumbNail.setImageUrl(m.getThumbnailUrl(), imageLoader);
        String image_uri = m.image_uri;

        if (image_uri!=null) {
        	//Log.d("PAI", image_uri);
        	Glide.with(convertView.getContext()).load(image_uri).transform(mCircleTransform).into(contactPicture);
        }
        else
            Glide.with(convertView.getContext()).load(R.drawable.default_contact_picture).transform(mCircleTransform).into(contactPicture);
       
        
        // Name
        contact_name.setText(m.name);
         
        // contact_number
        contact_number.setText(m.number);
         
        // circle and State
        circle_state.setText(m.carrier+","+m.circle);
         
        // total_duration
        total_duration.setText(getTotalDurationFormatted(m.out_duration));
        
        cost.setText("Rs. "+String.format("%.2f", m.total_cost));
 
        return convertView;
    }
    static class CircleTransform extends BitmapTransformation
    {
        public CircleTransform(Context context) {
            super(context);
        }

        @Override protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            return circleCrop(pool, toTransform);
        }

        private static Bitmap circleCrop(BitmapPool pool, Bitmap source) {
            if (source == null) return null;

            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            // TODO this could be acquired from the pool too
            Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

            Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;
        }

        @Override public String getId() {
            return getClass().getName();
        }
    }
    private String getTotalDurationFormatted(int totalSecs) {
        String min, sec, hr;
        Integer hrs, mins, secs;
        secs = totalSecs % 60;
        if (secs < 10)
            sec = "0" + secs;
        else
            sec = "" + secs;
        totalSecs = totalSecs / 60;
        mins = totalSecs % 60;
        if (mins < 10)
            min = "0" + mins;
        else
            min = "" + mins;
        totalSecs = totalSecs / 60;
        hrs = totalSecs;
        if (hrs < 10)
            hr = "0" + hrs;
        else
            hr = "" + hrs;
        return hr + ":" + min + ":" + sec;
    }
}
