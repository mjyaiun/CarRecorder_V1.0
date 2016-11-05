/*
 * Copyright 2016 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.edu.scu.carrecorder.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuAdapter;

import java.util.List;

import cn.edu.scu.carrecorder.R;
import cn.edu.scu.carrecorder.listener.OnItemClickListener;

/**
 * Created by YOLANDA on 2016/7/22.
 */
public class FileMenuAdapter extends SwipeMenuAdapter<FileMenuAdapter.DefaultViewHolder> {

    private List<String> titles;
    private List<Integer> durations;

    private OnItemClickListener mOnItemClickListener;

    public FileMenuAdapter(List<String> titles, List<Integer> durations) {
        this.titles = titles;
        this.durations = durations;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return titles == null ? 0 : titles.size();
    }

    @Override
    public View onCreateContentView(ViewGroup parent, int viewType) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.fileitem, parent, false);
    }

    @Override
    public FileMenuAdapter.DefaultViewHolder onCompatCreateViewHolder(View realContentView, int viewType) {
        return new DefaultViewHolder(realContentView);
    }

    @Override
    public void onBindViewHolder(FileMenuAdapter.DefaultViewHolder holder, int position) {
        holder.setData(titles.get(position), durations.get(position));
        holder.setOnItemClickListener(mOnItemClickListener);
    }

    static class DefaultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle;
        TextView tvDuration;
        OnItemClickListener mOnItemClickListener;

        public DefaultViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            tvDuration = (TextView) itemView.findViewById(R.id.tv_duration);
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        public void setData(String title, int duration) {
            this.tvTitle.setText(title);

            String text = "时长:";
            if (duration / 60 / 60 != 0) {
                text += duration / 60 / 60 + "时" + duration / 60 + "分";
            }
            if(duration / 60 != 0) {
                text += duration / 60 + "分";
            }
            text += duration % 60 + "秒";
            this.tvDuration.setText(text);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(getAdapterPosition());
            }
        }
    }

}
