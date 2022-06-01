package com.example.ezusb;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RegistrationAdapter extends RecyclerView.Adapter<RegistrationAdapter.ViewHolder>{

    private String TAG = RegistrationAdapter.class.getSimpleName() + "(TAG)";
    private List<RegistrationModel> mData = new ArrayList<>();
    private ItemClickListener clickListener;


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView number, name;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_Name);
            number = (TextView) itemView.findViewById(R.id.tv_Number);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onItemClick(view, getPosition());
            }
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setClickListener(ItemClickListener listener) {
        this.clickListener = listener;
    }



        @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_now,parent,false);
            return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(mData.get(position).patient_name);
        holder.number.setText(mData.get(position).look_number);
    }

    public void setData(List<RegistrationModel> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}

class RegistrationModel{

    //看診號
    String patient_name = "";
    //患者姓名
    String look_number = "";
    // iD
    String ID = "";
    // 病患生日
    String birthday = "";
    // 預掛識別碼
    String Rid = "";

}