package com.nanosl.helpmate.request;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nanosl.helpmate.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Thilina on 5/31/2017.
 */

public class RequestViewHolder extends RecyclerView.ViewHolder {
    Request request;
    TextView messageTextView;
    ImageView messageImageView;
    TextView messengerTextView;
    TextView cityTextView;
    TextView addedTimeTextView;
    CircleImageView messengerImageView;
    LinearLayout messageLinearLayout;

    public RequestViewHolder(final View v) {
        super(v);
        messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
        messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
        messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
        cityTextView = (TextView) itemView.findViewById(R.id.cityTextView);
        addedTimeTextView = (TextView) itemView.findViewById(R.id.addedTimeTextView);
        messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);

        messageLinearLayout = (LinearLayout) itemView.findViewById(R.id.messageLinearLayout);
        messageLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent displayCollectionCenterActivity = new Intent(v.getContext(), DisplayRequestActivity.class);

                displayCollectionCenterActivity.putExtra("request", request); //Optional parameters
                v.getContext().startActivity(displayCollectionCenterActivity);
            }
        });
    }
}
