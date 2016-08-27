package com.ameerhamza6733.dwnews;

import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by DELL 3542 on 8/4/2016.
 */
public class BBCUrduHomePageFragment extends Fragment {

    private static final String TAG = "ArtFragment_";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;

    protected LayoutManagerType mCurrentLayoutManagerType;
    protected RecyclerView mRecyclerView;
    protected RecyclerAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected ArrayList<RssItem> mDataset = new ArrayList<>();

    private Elements metalinks;
    private String mImageURL;
    private Document documentImage;
    private ProgressBar mProgressBar;
    private int mItemNumber;
    public View view;
    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);


        Log.d(TAG, "onCreate: ");

        setRetainInstance(true);
        new myPagerAdupter(getChildFragmentManager(),getContext());



    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.head_line_fragment, container, false);





        this.view=rootView;
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.Progressbar);
        mProgressBar.setProgress(0);

        LoadRssFeedsItems loadRssFeedsItems = new LoadRssFeedsItems();

        loadRssFeedsItems.setCircularProgressView(mProgressBar);




        mLayoutManager = new LinearLayoutManager(getActivity());

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){

            mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }else {
            mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
        }


        if (savedInstanceState != null) {


            Log.i(TAG,"save state is not null");
            mDataset= savedInstanceState.getParcelableArrayList("m");
            mAdapter = new RecyclerAdapter(mDataset);
            // Log.i(TAG, "m data set size in current affairs: " + mDataset.size());

            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);


        return rootView;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && mDataset.isEmpty()) {
            Log.d(TAG,"setUserVisibleHint");
            new LoadRssFeedsItems(getActivity()).execute(Constants.BBC_HOME_PAGE);
        }
    }

    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        savedInstanceState.putParcelableArrayList("m",mDataset);
        super.onSaveInstanceState(savedInstanceState);
    }


    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }


    private class LoadRssFeedsItems extends AsyncTask<String, Integer, Void> {
        private  Context mContext;
        private String mTitle, mDescription, mLink, mPubDate,mImageLink;

        private ProgressBar circularProgressView;

        public LoadRssFeedsItems(Context context)
        {
            mContext=context;

        }
        public LoadRssFeedsItems()
        {


        }
        public void setCircularProgressView(ProgressBar circularProgressView) {
            this.circularProgressView = circularProgressView;
        }


        protected Void doInBackground(String... urls) {
            try {

                Document rssDocument = Jsoup.connect(urls[0]).timeout(Constants.TIME_OUT).ignoreContentType(true).parser(Parser.xmlParser()).get();


                Elements mItems = rssDocument.select(Constants.ITEM);
                RssItem rssItem;
                mItemNumber = 0;
                for (Element element : mItems) {
                    // publishProgress(i);
                    mItemNumber++;
                    mTitle = element.select("title").first().text();
                    mDescription = element.select("description").first().text();
                    mLink = element.select("guid").last().text();
                    mPubDate = element.select("pubDate").first().text();
                    mImageURL=element.select("media|thumbnail").attr("url");


                    Log.i(TAG, "Item title: " + (mTitle == null ? "N/A" : mTitle));
                    Log.i(TAG, "Item Description: " + (mDescription == null ? "N/A" : mDescription));
                    Log.i(TAG, "Item link: " + (mLink == null ? "N/A" : mLink));
                    Log.i(TAG, "Item data: " + (mPubDate == null ? "N/A" : mPubDate));
                    Log.i(TAG, "Item image link: " + (mImageURL == null ? "N/A" : mImageURL));
                    Log.i(TAG, "item: : " + mItemNumber);
                    rssItem = new RssItem(mTitle, mDescription, mPubDate, mLink," ", mItemNumber,getActivity());
                    mDataset.add(rssItem);


                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... value) {
            super.onProgressUpdate(value);
            if (circularProgressView != null) {

                circularProgressView.setProgress(value[0]);
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mDataset.isEmpty()) {
                Snackbar mSnackbar = Snackbar.make(view, "Unable to connect  Home page", Snackbar.LENGTH_LONG)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                new LoadRssFeedsItems().execute(Constants.ART_AND_CULTURE);


                            }
                        });
                mSnackbar.show();
            } else {
                mAdapter = new RecyclerAdapter(mDataset);
                Log.i(TAG, "m data set size in current affairs: " + mDataset.size());

                mRecyclerView.setAdapter(mAdapter);


//                if(!new MySharedPreferences().loadPrefs(Constants.switchStateKey, true, mContext))
//                {
//                    getImageUrls get_ImageUrls = new getImageUrls();
//                    get_ImageUrls.execute();
//                }
            }
        }
    }

    private class getImageUrls extends AsyncTask<Void, Void, Void> {
        String mLink;
        String mLastHalfUrl;
        String url;
        int dash_A_index;

        @Override
        protected void onPreExecute() {
            Snackbar.make(getView(), "Loding Photos...", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            Log.i(TAG, "Item image link in seacond thread: " + (mImageURL == null ? "N/A" : mImageURL));

            try {



                for (int i = 0; i < mDataset.size(); i++) {
                    Thread.sleep(50);
                    mLink = mDataset.get(i).getPostLink();

                    dash_A_index = mLink.indexOf(Constants.BACK_SLASH_A);
                    mLastHalfUrl = mLink.substring(dash_A_index);
                    url = mLink.replace(Constants.BASE_URL, "");
                    url = url.replace(mLastHalfUrl, "");
                    url = StringUtils.replaceEach(URLEncoder.encode(url, "UTF-8"), new String[]{"+", "*", "%7E"}, new String[]{"%20", "%2A", "~"});
                    StringBuilder sb = new StringBuilder(url);
                    url = sb.append(mLastHalfUrl).toString();
                    url = Constants.BASE_URL + url;
                    documentImage = Jsoup.connect(url).timeout(Constants.TIME_OUT).get();
                    metalinks = documentImage.select(Constants.MATA_PROPTY_IMAGE);
                    mImageURL = metalinks.attr(Constants.CONTENT);
                    Log.i(TAG, "Item image link in seacond thread home page: " + (mImageURL == null ? "N/A" : mImageURL));
                    mDataset.get(i).setImageUrl(mImageURL);

                }

            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            Constants.IS_GET_IMAGE_URL=true;
            mAdapter.notifyDataSetChanged();
        }
    }

}
