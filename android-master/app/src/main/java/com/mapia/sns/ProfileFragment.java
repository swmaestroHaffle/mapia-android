package com.mapia.sns;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapia.MainFragment;
import com.mapia.sns.asne.core.SocialNetwork;
import com.mapia.sns.asne.core.listener.OnPostingCompleteListener;
import com.mapia.sns.asne.core.listener.OnRequestSocialPersonCompleteListener;
import com.mapia.sns.asne.facebook.FacebookSocialNetwork;
import com.mapia.sns.asne.twitter.TwitterSocialNetwork;
import com.mapia.sns.model.SocialPerson;
import com.squareup.picasso.Picasso;

//import com.github.gorbin.asne.googleplus.GooglePlusSocialNetwork;
//import com.github.gorbin.asne.linkedin.LinkedInSocialNetwork;

public class ProfileFragment extends Fragment implements OnRequestSocialPersonCompleteListener {
    private String message = "Need simple social networks integration? Check this lbrary:";
    private String link = "https://github.com/gorbin/ASNE";

    private static final String NETWORK_ID = "NETWORK_ID";
    private SocialNetwork socialNetwork;
    private int networkId;
    private ImageView photo;
    private TextView name;
    private TextView id;
    private TextView info;
    private Button friends;
    private Button share;
    private RelativeLayout frame;

    public static ProfileFragment newInstannce(int id) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt(NETWORK_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        networkId = getArguments().containsKey(NETWORK_ID) ? getArguments().getInt(NETWORK_ID) : 0;
        ((SNSActivity)getActivity()).getSupportActionBar().setTitle("Profile");

        View rootView = inflater.inflate(com.mapia.R.layout.profile_fragment, container, false);

        frame = (RelativeLayout) rootView.findViewById(com.mapia.R.id.frame);
        photo = (ImageView) rootView.findViewById(com.mapia.R.id.imageView);
        name = (TextView) rootView.findViewById(com.mapia.R.id.name);
        id = (TextView) rootView.findViewById(com.mapia.R.id.id);
        info = (TextView) rootView.findViewById(com.mapia.R.id.info);
        friends = (Button) rootView.findViewById(com.mapia.R.id.friends);
        friends.setOnClickListener(friendsClick);
        share = (Button) rootView.findViewById(com.mapia.R.id.share);
        share.setOnClickListener(shareClick);
        colorProfile(networkId);

        socialNetwork = MainFragment.mSocialNetworkManager.getSocialNetwork(networkId);
        socialNetwork.setOnRequestCurrentPersonCompleteListener(this);
        socialNetwork.requestCurrentPerson();

        SNSActivity.showProgress("Loading social person");
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(com.mapia.R.menu.main, menu);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_logout:
//                socialNetwork.logout();
//                getActivity().getSupportFragmentManager().popBackStack();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    @Override
    public void onRequestSocialPersonSuccess(int i, SocialPerson socialPerson) {
        SNSActivity.hideProgress();
        name.setText(socialPerson.name);
        id.setText(socialPerson.id);
        String socialPersonString = socialPerson.toString();
        String infoString = socialPersonString.substring(socialPersonString.indexOf("{")+1, socialPersonString.lastIndexOf("}"));
        info.setText(infoString.replace(", ", "\n"));
        Picasso.with(getActivity())
                .load(socialPerson.avatarURL)
                .into(photo);
    }

    @Override
    public void onError(int networkId, String requestID, String errorMessage, Object data) {
        SNSActivity.hideProgress();
        Toast.makeText(getActivity(), "ERROR: " + errorMessage, Toast.LENGTH_LONG).show();
    }

    private View.OnClickListener friendsClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FriendsListFragment friends = FriendsListFragment.newInstannce(networkId);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .addToBackStack("friends")
                    .replace(com.mapia.R.id.container, friends)
                    .commit();
        }
    };

    private View.OnClickListener shareClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder ad = alertDialogInit("Would you like to post Link:", link);
            ad.setPositiveButton("Post link", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Bundle postParams = new Bundle();
                    postParams.putString(SocialNetwork.BUNDLE_NAME, "Simple and easy way to add social networks for android application");
                    postParams.putString(SocialNetwork.BUNDLE_LINK, link);
//                    if(networkId == GooglePlusSocialNetwork.ID) {
//                        socialNetwork.requestPostDialog(postParams, postingComplete);
//                    } else {
//                        socialNetwork.requestPostLink(postParams, message, postingComplete);
//                    }
                }
            });
            ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.cancel();
                }
            });
            ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    dialog.cancel();
                }
            });
            ad.create().show();
        }
    };

    private OnPostingCompleteListener postingComplete = new OnPostingCompleteListener() {
        @Override
        public void onPostSuccessfully(int socialNetworkID) {
            Toast.makeText(getActivity(), "Sent", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(int socialNetworkID, String requestID, String errorMessage, Object data) {
            Toast.makeText(getActivity(), "Error while sending: " + errorMessage, Toast.LENGTH_LONG).show();
        }
    };

    private void colorProfile(int networkId){
        int color = getResources().getColor(com.mapia.R.color.dark);
        int image = com.mapia.R.drawable.user;
        switch (networkId) {
            case TwitterSocialNetwork.ID:
                color = getResources().getColor(com.mapia.R.color.twitter);
                image = com.mapia.R.drawable.twitter_user;
                break;
//            case LinkedInSocialNetwork.ID:
//                color = getResources().getColor(R.color.linkedin);
//                image = R.drawable.linkedin_user;
//                break;
//            case GooglePlusSocialNetwork.ID:
//                color = getResources().getColor(R.color.google_plus);
//                image = R.drawable.g_plus_user;
//                break;
            case FacebookSocialNetwork.ID:
                color = getResources().getColor(com.mapia.R.color.facebook);
                image = com.mapia.R.drawable.com_facebook_profile_picture_blank_square;
                break;
        }
        frame.setBackgroundColor(color);
        name.setTextColor(color);
        friends.setBackgroundColor(color);
        share.setBackgroundColor(color);
        photo.setBackgroundColor(color);
        photo.setImageResource(image);
    }

    private AlertDialog.Builder alertDialogInit(String title, String message){
        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
        ad.setTitle(title);
        ad.setMessage(message);
        ad.setCancelable(true);
        return ad;
    }
}
