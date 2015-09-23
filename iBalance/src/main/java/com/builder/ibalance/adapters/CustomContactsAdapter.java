package com.builder.ibalance.adapters;

 
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.builder.ibalance.R;
import com.builder.ibalance.util.MyApplication;
import com.squareup.picasso.Picasso;
 
 
public class CustomContactsAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<ContactsModel> contactItems;
    Typeface tf;
    public CustomContactsAdapter(List<ContactsModel> contactItems) {
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
        ContactsModel m = contactItems.get(position);
        convertView.setTag(m.getContact_number());
        // thumbnail image
        //thumbNail.setImageUrl(m.getThumbnailUrl(), imageLoader);
        String image_uri = m.getImage_uri();
       
        if (image_uri!=null) {
        	//Log.d("PAI", image_uri);
        	Picasso.with(convertView.getContext()).load(image_uri).into(contactPicture);
//            try {
//             bp = MediaStore.Images.Media
//               .getBitmap(convertView.getContext().getContentResolver(),
//                 Uri.parse(image_uri));
//             if(bp!=null)
//            	 contactPicture.setImageBitmap(bp);
//            } catch (FileNotFoundException e) {
//            	//Log.d("Adapeter", "Error "+m.getContact_name() + " image uri = "+//Log.d("Adapeter", "Error "+m.getContact_name()));
//             //e.printStackTrace();
//            } catch (IOException e) {
//            	//Log.d("Adapeter", "Error "+m.getContact_name());
//             //e.printStackTrace();
//            }
        }
        else
        	contactPicture.setImageResource( R.drawable.default_contact_picture);
       
        
        // Name
        contact_name.setText(m.getContact_name());
         
        // contact_number
        contact_number.setText(m.getContact_number());
         
        // circle and State
        circle_state.setText(m.getContact_circle()+","+m.getContact_state());
         
        // total_duration
        total_duration.setText(m.getTotal_duration());
        
        cost.setText("Rs. "+String.format("%.2f", m.callCost));
 
        return convertView;
    }
 
}
