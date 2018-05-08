package com.newtechs.locations.cg;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Dell on 07-05-2018.
 */

public class MyRecycler  extends RecyclerView.Adapter<MyHolder>{
    Context context;
    ArrayList arrayList1,arrayList2,arrayList3;
    LayoutInflater inflater;
    public MyRecycler(Context context,ArrayList arrayList1,ArrayList arrayList2,ArrayList arrayList3)
    {
      this.context=context;
      this.arrayList1=arrayList1;
      this.arrayList2=arrayList2;
      this.arrayList3=arrayList3;
      inflater=LayoutInflater.from(context);
    }
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.myrecycler,null);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
      holder.veh_no.setText("Vehicle No:"+arrayList1.get(position).toString());
        holder.dist.setText("Dist:"+arrayList2.get(position).toString());
        holder.time.setText("Estimated time:"+arrayList3.get(position).toString());
    }

    @Override
    public int getItemCount() {
        return arrayList1.size();
    }
}
class MyHolder extends RecyclerView.ViewHolder {
    TextView veh_no,dist,time;
    public MyHolder(View itemView) {
        super(itemView);
        veh_no=itemView.findViewById(R.id.vehicleno);
        dist=itemView.findViewById(R.id.Dist);
        time=itemView.findViewById(R.id.estimatedtime);
    }
}