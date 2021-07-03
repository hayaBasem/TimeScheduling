package com.time.scheduling.recycler;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.time.scheduling.DataBase.Task;
import com.time.scheduling.R;

import java.text.SimpleDateFormat;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PlanViewAdapter extends ListAdapter<Task, PlanViewAdapter.PlanViewHolder> {


    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.isEnabled() == newItem.isEnabled()
                    && oldItem.getTime() == newItem.getTime()
                    && oldItem.getDescription().equals(newItem.getDescription())
                    && oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getPriority() == newItem.getPriority();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.isEnabled() == newItem.isEnabled()
                    && oldItem.getTime() == newItem.getTime()
                    && oldItem.getDescription().equals(newItem.getDescription())
                    && oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getPriority() == newItem.getPriority();
        }
    };

    public PlanViewAdapter(Activity activity) {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.plan_item, parent, false);
        return new PlanViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final PlanViewHolder holder, final int position) {

        final Task currentItem = getItem(position);
        holder.bindTo(currentItem);
    }


    public Task getTaskView(int position) {
        return getItem(position);
    }


    static class PlanViewHolder extends RecyclerView.ViewHolder {

        private TextView title,time,description,enabling;

        public PlanViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            time = itemView.findViewById(R.id.time);
            description = itemView.findViewById(R.id.description);
            enabling = itemView.findViewById(R.id.enabling);

        }


        public void bindTo(Task currentItem) {
            title.setText(currentItem.getTitle());
            description.setText(currentItem.getDescription());

            long alarmTimeInMillis = currentItem.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                    Locale.getDefault());
            String formattedTime = sdf.format(alarmTimeInMillis);
            time.setText(formattedTime);


            if(currentItem.isEnabled()){
                enabling.setText("In Waite");
                enabling.setTextColor(Color.parseColor("#B04EF33D"));
            }else {
                if(currentItem.getTime() > System.currentTimeMillis() ){
                    enabling.setText("Un Enable");
                    enabling.setTextColor(Color.parseColor("#D35454"));
                }else {
                    enabling.setText("Finshed");
                    enabling.setTextColor(Color.parseColor("#000000"));
                }

            }

        }
    }


}