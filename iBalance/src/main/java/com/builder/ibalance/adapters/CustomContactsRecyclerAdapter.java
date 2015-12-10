package com.builder.ibalance.adapters;

/**
 * Created by sunny on 11/30/15.
 */
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

import com.builder.ibalance.ContactDetailActivity;
import com.builder.ibalance.R;
import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.util.CircleTransform;
import com.builder.ibalance.util.Helper;
import com.squareup.picasso.Picasso;

public class CustomContactsRecyclerAdapter extends RecyclerView.Adapter<CustomContactsRecyclerAdapter.ViewHolder> {

    private List<ContactDetailModel> contactItems;
    Typeface tf;
    CircleTransform mCircleTransform = new CircleTransform();
    private Context mContext;
    //add context for image to load from picasso chnage adapter to context,list


    public CustomContactsRecyclerAdapter(List<ContactDetailModel> items) {
        this.contactItems = items;

    }

    public CustomContactsRecyclerAdapter(Context context,List<ContactDetailModel> items) {
        this.contactItems = items;
        this.mContext = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_list_item, viewGroup, false);
        ViewHolder mViewHolder = new ViewHolder(v);
        v.setOnClickListener(mViewHolder);
        return mViewHolder;

    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        ContactDetailModel item = contactItems.get(i);


        String image_uri = item.image_uri;

        if (image_uri!=null) {
            Picasso.with(mContext).load(image_uri).transform(mCircleTransform).into(viewHolder.contactPicture);
        }
        else
            Picasso.with(mContext).load(R.drawable.default_contact_picture).transform(mCircleTransform).into(viewHolder.contactPicture);


        // Name
        viewHolder.contact_name.setText(item.name);

        // contact_number
        viewHolder.contact_number.setText(item.number);
        viewHolder.contact_number.setTag(item);

        // circle and State
        viewHolder.circle_state.setText(item.carrier+","+item.circle);

        // total_duration
        viewHolder.total_duration.setText(Helper.getTotalDurationFormatted(item.out_duration));

        viewHolder.cost.setText("Rs. "+String.format("%.2f", item.total_cost));




    }

    @Override
    public int getItemCount() {
        return contactItems.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView contact_name;
        private final TextView contact_number;
        private final TextView circle_state;
        private final TextView total_duration;
        private final TextView cost;
        private final ImageView contactPicture;


        ViewHolder(View convertView) {
            super(convertView);
            contactPicture = (ImageView) convertView
                    .findViewById(R.id.recents_picture);
            contact_name = (TextView) convertView.findViewById(R.id.recents_name);
            contact_name.setTypeface(tf);
            contact_number = (TextView) convertView.findViewById(R.id.recents_number);
            contact_name.setTypeface(tf);
            circle_state = (TextView) convertView.findViewById(R.id.circle);
            circle_state.setTypeface(tf);
            total_duration = (TextView) convertView.findViewById(R.id.recents_duration);
            total_duration.setTypeface(tf);
            cost = (TextView) convertView.findViewById(R.id.contact_spent);
            cost.setTypeface(tf);

        }

        @Override
        public void onClick(View v) {

            Intent mIntent = new Intent(mContext,
                    ContactDetailActivity.class);
            mIntent.putExtra("DETAILS", (ContactDetailModel) v.findViewById(R.id.recents_number).getTag());
            v.getContext().startActivity(mIntent);
        }
    }

}

