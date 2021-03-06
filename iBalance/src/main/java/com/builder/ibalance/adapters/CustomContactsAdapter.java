package com.builder.ibalance.adapters;


import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.builder.ibalance.R;
import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.util.CircleTransform;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.squareup.picasso.Picasso;

import java.util.List;
 
 
public class CustomContactsAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<ContactDetailModel> contactItems;
    Typeface tf;
    CircleTransform mCircleTransform = new CircleTransform();
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
                .findViewById(R.id.recents_picture);
        TextView contact_name = (TextView) convertView.findViewById(R.id.recents_name);
        contact_name.setTypeface(tf);
        TextView contact_number = (TextView) convertView.findViewById(R.id.recents_number);
        contact_name.setTypeface(tf);
        TextView circle_state = (TextView) convertView.findViewById(R.id.circle);
        circle_state.setTypeface(tf);
        TextView total_duration = (TextView) convertView.findViewById(R.id.recents_duration);
        total_duration.setTypeface(tf);
        TextView cost = (TextView) convertView.findViewById(R.id.contact_spent);
        cost.setTypeface(tf);
        // getting movie data for the row
        ContactDetailModel m = contactItems.get(position);
        convertView.setTag(m);
        // thumbnail image
        //thumbNail.setImageUrl(m.getThumbnailUrl(), imageLoader);
        String image_uri = m.image_uri;

        if (image_uri!=null) {
        	//Log.d("PAI", image_uri);
        	Picasso.with(convertView.getContext()).load(image_uri).transform(mCircleTransform).into(contactPicture);
        }
        else
            Picasso.with(convertView.getContext()).load(R.drawable.default_contact_picture).transform(mCircleTransform).into(contactPicture);
       
        
        // Name
        contact_name.setText(m.name);
         
        // contact_number
        contact_number.setText(m.number);
         
        // circle and State
        circle_state.setText(m.carrier+","+m.circle);
         
        // total_duration
        total_duration.setText(Helper.getTotalDurationFormatted(m.out_duration));
        
        cost.setText("Rs. "+String.format("%.2f", m.total_cost));
 
        return convertView;
    }

}
