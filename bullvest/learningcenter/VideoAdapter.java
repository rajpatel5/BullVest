package com.target.android.bullvest.learningcenter;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.VideoView;

import com.target.android.bullvest.R;
import com.target.android.bullvest.utils.Video;

import java.util.List;

/**
 * Created by Ashwin on 26/06/2018.
 */

public class VideoAdapter extends ArrayAdapter<Video> {

    private Context mContext;
    private List<Video> mVideos;

    private boolean playing = false;

    public VideoAdapter(@NonNull Context context, @NonNull List<Video> objects) {
        super(context, R.layout.video_card_view, objects);
        mContext = context;
        mVideos = objects;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.video_card_view, null);
            holder = new ViewHolder();

            holder.videoView = (VideoView) convertView.findViewById(R.id.videoView);
            holder.videoTitle = (TextView) convertView.findViewById(R.id.videoTitle);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        /* Get clicked view and play video url at this position */
        try {
            Video video = mVideos.get(position);
            //play video using android api, when video view is clicked.
            String url = video.getVideoUrl(); // your URL here
            Uri videoUri = Uri.parse(url);
            holder.videoView.setVideoURI(videoUri);
            holder.videoTitle.setText(video.getVideoTitle());

            holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // This is just to show image when loaded
                    mp.start();
                    mp.pause();
                }
            });

            // Listens for user's touch
            holder.videoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // This determines if the user wants to pause or play
                    if (event.getAction() == 0) {
                        if (!playing) {
                            playing = true;
                        } else {
                            playing = false;
                        }
                    }

                    // Plays or pauses the video
                    if (playing) {
                        holder.videoView.start();
                    } else {
                        holder.videoView.pause();
                    }
                    return playing;
                }
            });

            // Resets the video
            holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.seekTo(0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    public static class ViewHolder {
        VideoView videoView;
        TextView videoTitle;
    }
}
