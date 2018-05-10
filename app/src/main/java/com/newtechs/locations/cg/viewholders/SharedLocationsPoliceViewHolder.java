package com.newtechs.locations.cg.viewholders;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.newtechs.locations.cg.R;
import com.newtechs.locations.cg.activities.CaseRoutingActivity;
import com.newtechs.locations.cg.utilities.VehicleData;

/**
 * Created by Niru.R on 05-10-2018.
 */

public class SharedLocationsPoliceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    View mView;
    Context mContext;
    String locationString;
    String vehicleNoString;

    public SharedLocationsPoliceViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        itemView.setOnClickListener(this);

    }

    public void bindData(VehicleData data,int flag){
        TextView vehicleNo = itemView.findViewById(R.id.vehiclenotext);
        TextView vehicleNoText = itemView.findViewById(R.id.vtext);
        TextView location = itemView.findViewById(R.id.locationtext);
        TextView verifiedText = itemView.findViewById(R.id.verifiedtext);
        if (flag==1){
            vehicleNoText.setVisibility(View.GONE);
            vehicleNo.setVisibility(View.GONE);
        }
        vehicleNoString = data.vehicleNo;
        vehicleNo.setText(data.vehicleNo);
        String s = data.location;
        String splits[]=s.split(",");
        location.setText(splits[0]+"\n"+splits[1]);
        locationString = data.location;
        verifiedText.setText(data.verifiedtext);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(mContext,CaseRoutingActivity.class);
        intent.putExtra("location",locationString);
        intent.putExtra("vehicleno",vehicleNoString);
        mContext.startActivity(intent);
    }
}
