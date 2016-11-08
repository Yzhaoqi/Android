package com.yzq.android.musicplayer;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by YZQ on 2016/11/8.
 */

public class MusicAdapter extends BaseAdapter {
    private Context context;
    private List<MusicItem> list;

    public MusicAdapter(Context context, List<MusicItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        if (list == null) {
            return null;
        }
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View convertView;
        ViewHolder viewHolder;

        if (view == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item, null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView)convertView.findViewById(R.id.title);
            //viewHolder.image = (ImageView)convertView.findViewById(R.id.cover_preview);
            convertView.setTag(viewHolder);
        } else {
            convertView = view;
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.name.setText(list.get(i).getName());
        /* // add cover
        DrawCover dc = new DrawCover();
        dc.setCover(context, viewHolder.image, Uri.parse(list.get(i).getPath()));
        */

        return convertView;
    }

    private class ViewHolder {
        public TextView name;
    }
}
