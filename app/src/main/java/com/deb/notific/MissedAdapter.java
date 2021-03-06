package com.deb.notific;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.deb.notific.helper.Contract;
import com.deb.notific.helper.pnumber;

public class MissedAdapter extends RecyclerView.Adapter<MissedAdapter.MissedViewHolder> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private Cursor mCursor;
    String name,number,time;
    public MissedAdapter(Context context, Cursor cursor) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mCursor = cursor;
    }
    public class MissedViewHolder extends RecyclerView.ViewHolder{
        TextView name,time,number;
        ConstraintLayout mConstraintLayout;
        public MissedViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contactname);
            time = itemView.findViewById(R.id.timeview);
            number = itemView.findViewById(R.id.numberview);
            mConstraintLayout = itemView.findViewById(R.id.calllaay);
        }
    }

    @NonNull
    @Override
    public MissedAdapter.MissedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.customview2, parent, false);
        return new MissedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MissedAdapter.MissedViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }
       name = mCursor.getString(mCursor.getColumnIndex(Contract.MissedCalls.COLUMN_NAME));
        number = mCursor.getString(mCursor.getColumnIndex(Contract.MissedCalls.COLUMN_NUMBER));
        time = mCursor.getString(mCursor.getColumnIndex(Contract.MissedCalls.COLUMN_TIME));
        long id = mCursor.getLong(mCursor.getColumnIndex(Contract.MissedCalls._ID));
        holder.name.setText(name);
        holder.time.setText(time);
        holder.number.setText(number);
        holder.itemView.setTag(id);
        holder.mConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri number1 = Uri.parse( "tel:"+"+91" +number);
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number1);
                mContext.startActivity(callIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
    public void swapCursor(Cursor newCursor){
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }
}
