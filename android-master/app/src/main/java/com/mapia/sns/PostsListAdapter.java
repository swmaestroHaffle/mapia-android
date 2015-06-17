package com.mapia.sns;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapia.sns.asne.facebook.FacebookSocialNetwork;
import com.mapia.sns.asne.instagram.InstagramSocialNetwork;
//import com.mapia.sns.asne.twitter.TwitterSocialNetwork;
import com.mapia.sns.model.SocialPost;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

//import com.github.gorbin.asne.googleplus.GooglePlusSocialNetwork;
//import com.github.gorbin.asne.linkedin.LinkedInSocialNetwork;

public class PostsListAdapter extends BaseAdapter {
    private final Activity context;
    private ViewHolder holder;
    private ArrayList<SocialPost> posts;
    private int networkId;
    private int image;

    public PostsListAdapter(Activity context, ArrayList<SocialPost> posts, int socialNetworkID) {
        this.context = context;
        this.posts = posts;
        this.networkId = socialNetworkID;
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public Object getItem(int i) {
        return posts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(com.mapia.R.layout.friend_row, null, true);
            holder = new ViewHolder();
            holder.id = (TextView) convertView.findViewById(com.mapia.R.id.id);
            holder.label = (TextView) convertView.findViewById(com.mapia.R.id.label);
            holder.imageView = (ImageView) convertView.findViewById(com.mapia.R.id.image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        colorRow(networkId);
        holder.id.setText(posts.get(position).id);
        holder.label.setText(posts.get(position).name);
        if (posts.get(position).avatarURL != null) {
            Picasso.with(context)
                    .load(posts.get(position).avatarURL)
                    .placeholder(image)
                    .error(image)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(image);
        }
        return convertView;
    }

    private void colorRow(int networkId){
        int color = context.getResources().getColor(com.mapia.R.color.dark);
        switch (networkId) {
//            case TwitterSocialNetwork.ID:
//                color = context.getResources().getColor(com.mapia.R.color.twitter);
//                image = com.mapia.R.drawable.twitter_user;
//                break;
            case InstagramSocialNetwork.ID:
                color = context.getResources().getColor(com.mapia.R.color.instagram);
                image = com.mapia.R.drawable.instagram_user;
                break;
            case FacebookSocialNetwork.ID:
                color = context.getResources().getColor(com.mapia.R.color.facebook);
                image = com.mapia.R.drawable.com_facebook_profile_picture_blank_square;
                break;
//            case GooglePlusSocialNetwork.ID:
//                color = context.getResources().getColor(R.color.google_plus);
//                image = R.drawable.user;
//                break;
        }
        holder.label.setTextColor(color);
    }

    static class ViewHolder {
        public ImageView imageView;
        public TextView id;
        public TextView label;
    }
}