/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.example.android.basicmediadecoder;


import android.animation.TimeAnimator;
import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import com.example.android.common.media.MediaCodecWrapper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This activity uses a {@link android.view.TextureView} to render the frames of a video decoded using
 * {@link android.media.MediaCodec} API.
 */
public class MainActivity extends Activity {

    private TextureView mPlaybackView;
    private TimeAnimator mTimeAnimator = new TimeAnimator();
    private static int MICRO_SEC = 1000000;

    // A utility that wraps up the underlying input and output buffer processing operations
    // into an east to use API.
    private MediaCodecWrapper mCodecWrapper;
    private MediaExtractor mExtractor = new MediaExtractor();
    TextView mAttribView = null;
    List<Integer> ids;

    ArrayList<ByteBuffer> FrameData;
    ArrayList<Long> FramePTS;
    ArrayList<Integer> FrameSize;
    ArrayList<Integer> FrameFlags;

    private static int frameCounter = 0;

    int j, k;

    boolean isEosCome = true;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_main);
        mPlaybackView = (TextureView) findViewById(R.id.PlaybackView);
        mAttribView = (TextView) findViewById(R.id.AttribView);

        FrameData = new ArrayList<ByteBuffer>();
        FramePTS = new ArrayList<Long>();
        FrameSize = new ArrayList<Integer>();
        FrameFlags = new ArrayList<Integer>();


        ids = new ArrayList<>();
        for (Field field : R.raw.class.getFields()) {
            try {
                ids.add(field.getInt(field));
            } catch (Exception e) {
                //compiled app contains files like '$change' or 'serialVersionUID'
                //which are no real media files
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTimeAnimator != null && mTimeAnimator.isRunning()) {
            mTimeAnimator.end();
        }

        if (mCodecWrapper != null) {
            mCodecWrapper.stopAndRelease();
            mExtractor.release();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_play) {
            mAttribView.setVisibility(View.VISIBLE);
            startPlayback();
            item.setEnabled(false);
        }
        return true;
    }


    public void startPlayback() {

        int count = 0;
        //for (k = 0; k < 4; k++) {
        // Construct a URI that points to the video resource that we want to play
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.frame_083_0);

        while (count < 1) {
            MediaExtractor tempExtractor = new MediaExtractor();
            switch (count) {
                case 0:
                    videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.frame_083_0);
                    break;
                case 1:
                    videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.frame_041_0);
                    break;
                case 2:
                    videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.frame_042_0);
                    break;
                case 3:
                    videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.frame_043_0);
                    break;
                case 4:
                    videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.frame_044_0);
                    break;
                case 5:
                    videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.frame_045_0);
                    break;
                case 6:
                    videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.frame_046_0);
                    break;
                case 7:
                    videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.frame_047_0);
                    break;
                case 8:
                    videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.frame_048_0);
                    break;
                case 9:
                    videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.frame_049_0);
                    break;
            }

            try {
                tempExtractor.setDataSource(this, videoUri, null);

                int nTracks = tempExtractor.getTrackCount();

                // Begin by unselecting all of the tracks in the extractor, so we won't see
                // any tracks that we haven't explicitly selected.
                for (int i = 0; i < nTracks; ++i) {
                    tempExtractor.unselectTrack(i);
                }

                /*Create the mCodecWrapper only once*/

                for (int i = 0; i < nTracks; ++i) {
                    // Try to create a video codec for this track. This call will return null if the
                    // track is not a video track, or not a recognized video format. Once it returns
                    // a valid MediaCodecWrapper, we can break out of the loop.
                    if (count == 0)
                        mCodecWrapper = MediaCodecWrapper.fromVideoFormat(tempExtractor.getTrackFormat(i), new Surface(mPlaybackView.getSurfaceTexture()));

                    if (mCodecWrapper != null) {
                        tempExtractor.selectTrack(i);
                        break;
                    }
                }
                int counter = 0;
                FrameData.add(ByteBuffer.allocate(512));
                int size;
                size = tempExtractor.readSampleData(FrameData.get(counter), 0);
                counter++;

                while (size > 0) {

                    FramePTS.add(MICRO_SEC * count + tempExtractor.getSampleTime());
                    FrameSize.add(size);
                    FrameFlags.add(tempExtractor.getSampleFlags());
                    tempExtractor.advance();
                    FrameData.add(ByteBuffer.allocate(512));
                    size = tempExtractor.readSampleData(FrameData.get(counter), 0);
                    counter++;
                    if (size <= 0)
                        FrameData.remove(counter - 1);
                    /*for(int i = 0; i<size;i++)
                        Log.d("bbbb",String.valueOf(buffer.get()));
                    Log.d("bbbb","aaaaaaaaaaaaaa");*/
                }
                tempExtractor.release();

            } catch (IOException e) {
                e.printStackTrace();
            }
            count++;
        }


        Log.d("bbbb", "ok");
        // BEGIN_INCLUDE(initialize_extractor)
        //===============mExtractor.setDataSource(this, videoUri, null);

        //==============int nTracks = mExtractor.getTrackCount();

        // Begin by unselecting all of the tracks in the extractor, so we won't see
        // any tracks that we haven't explicitly selected.
            /*=============for (int i = 0; i < nTracks; ++i) {
                mExtractor.unselectTrack(i);
            }==============*/


        // Find the first video track in the stream. In a real-world application
        // it's possible that the stream would contain multiple tracks, but this
        // sample assumes that we just want to play the first one.
            /*=============for (int i = 0; i < nTracks; ++i) {
                // Try to create a video codec for this track. This call will return null if the
                // track is not a video track, or not a recognized video format. Once it returns
                // a valid MediaCodecWrapper, we can break out of the loop.
                mCodecWrapper = MediaCodecWrapper.fromVideoFormat(mExtractor.getTrackFormat(i), new Surface(mPlaybackView.getSurfaceTexture()));

                if (mCodecWrapper != null) {
                    mExtractor.selectTrack(i);
                    break;
                }
            }=================*/
        // END_INCLUDE(initialize_extractor)


        // By using a {@link TimeAnimator}, we can sync our media rendering commands with
        // the system display frame rendering. The animator ticks as the {@link Choreographer}
        // receives VSYNC events.
        mTimeAnimator.setTimeListener(new TimeAnimator.TimeListener() {
            @Override
            public void onTimeUpdate(final TimeAnimator animation,
                                     final long totalTime,
                                     final long deltaTime) {

                    /*==============boolean isEos = ((mExtractor.getSampleFlags() & MediaCodec
                            .BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM);======*/


                //isEos = ((FrameFlags.get(frameCounter) & MediaCodec.BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM);

                //if(isEos)
                //Log.d("bbbb","EoS detected");


                // BEGIN_INCLUDE(write_sample)

                // Try to submit the sample to the codec and if successful advance the
                // extractor to the next available sample to read.

                    /*for(int i =0;i<FramePTS.size();i++){
                        Log.d("bbbb","Size :"+String.valueOf(FrameSize.get(i)));
                        for(int j =0;j<FrameSize.get(i);j++){
                            Log.d("bbbb",String.valueOf(FrameData.get(i).get()));
                        }
                    }*/
                boolean result = false;
                if (frameCounter < 7) {
                    result = mCodecWrapper.writeSample(mExtractor,
                            false,
                            FramePTS.get(frameCounter),
                            FrameFlags.get(frameCounter),
                            FrameData.get(frameCounter),
                            FrameSize.get(frameCounter),
                            frameCounter);
                    frameCounter++;
                }

                if (result) {
                    // Advancing the extractor is a blocking operation and it MUST be
                    // executed outside the main thread in real applications.
                    // Log.d("bbbb","frame  "+String.valueOf(frameCounter));

                }


                // END_INCLUDE(write_sample)

                // Examine the sample at the head of the queue to see if its ready to be
                // rendered and is not zero sized End-of-Stream record.
                MediaCodec.BufferInfo out_bufferInfo = new MediaCodec.BufferInfo();
                mCodecWrapper.peekSample(out_bufferInfo);

                Log.d("bbbb", "OutBuffer Size" + " " + String.valueOf(out_bufferInfo.size) + " " + String.valueOf(frameCounter) + "  " +
                        String.valueOf(out_bufferInfo.presentationTimeUs / 1000) + " " + String.valueOf(totalTime));

                // BEGIN_INCLUDE(render_sample)
                if (out_bufferInfo.presentationTimeUs / 1000 < totalTime) {
                    // Pop the sample off the queue and send it to {@link Surface}
                    mCodecWrapper.popSample(true);
                    //Log.d("bbbb","rendering" + " "+ String.valueOf(totalTime));
                } else if (out_bufferInfo.size <= 0 && frameCounter == 7) {
                    mTimeAnimator.end();
                    mCodecWrapper.stopAndRelease();


                    //mExtractor.release();

                }
                // END_INCLUDE(render_sample)

            }
        });

        // We're all set. Kick off the animator to process buffers and render video frames as
        // they become available
        mTimeAnimator.start();

        //}

    }
}
