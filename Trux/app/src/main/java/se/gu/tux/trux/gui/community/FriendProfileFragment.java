package se.gu.tux.trux.gui.community;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.application.FriendActionListener;
import se.gu.tux.trux.application.SocialHandler;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.gui.base.BaseAppActivity;
import se.gu.tux.trux.gui.main_home.HomeActivity;
import se.gu.tux.trux.gui.messaging.MessageActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;


/**
 * This class handles the friends profile page which view the friends picture, name,
 * message and remove button. The message button takes you to the conversation with
 * that friend and the remove button takes away the friend from you friends.
 */
public class FriendProfileFragment extends Fragment implements FriendActionListener {

    private TextView profileTitle;
    private ImageButton removeButton, messageButton;
    private ImageView profilePic;
    private Friend friend;
    private FriendProfileFragment thisFragment = this;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_profile, container, false);
        removeButton = (ImageButton) view.findViewById(R.id.fragment_info_remove_friend_button);
        messageButton = (ImageButton) view.findViewById(R.id.fragment_info_message_button);
        profileTitle = (TextView) view.findViewById(R.id.profile_title);
        profilePic = (ImageView) view.findViewById(R.id.infoPicture);

        //These arguments are sent from MapFrag if opened MapCommunityWindow or from FriendsWindow if opened from there
        if (this.getArguments() != null) {
            friend = (Friend) this.getArguments().getSerializable("friend");
        }

        showFriendInfo();

        return view;
    }

    /**
     * This method handles to view the profile picture of the friend and
     * why the name as well as the buttonClickListeners.
     */
    private void showFriendInfo() {
        if (friend != null) {
            profileTitle.setText(friend.getFirstname() + " " + friend.getLastname()
                    + "(" + friend.getUsername() + ")");

            if (friend.getProfilePic() != null) {
                Bitmap pic = Bitmap.createScaledBitmap(
                        SocialHandler.pictureToBitMap(friend.getProfilePic())
                        , 500, 500, false);
                profilePic.setImageBitmap(pic);
            }
            //On click it takes the user to the converstation with that friend.
            messageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), MessageActivity.class);
                    intent.setAction("OPEN_CHAT");
                    intent.putExtra("FRIEND_ID", friend.getFriendId());
                    intent.putExtra("FRIEND_USERNAME", friend.getUsername());
                    startActivity(intent);
                }
            });
            //On click it removes the friend from the friendlist
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Shows a DialogBox that makes sure that the user wants to remove the friend.
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.community_remove)
                            .setMessage(R.string.remove_dialog_message)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    removeButton.setEnabled(false);
                                    messageButton.setEnabled(false);
                                    try {
                                        DataHandler.gI().getSocialHandler().sendFriendRemove(thisFragment,
                                                friend.getFriendId());
                                    } catch (NotLoggedInException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .show();
                }
            });
        }
    }

    public void onStop() {
        super.onStop();
    }

    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
    }

    /**
     * This is never used in this class.
     *
     * @param friendId
     * @param accepted
     */
    @Override
    public void onFriendRequestAnswered(long friendId, boolean accepted) {
    }

    /**
     * This is never used in this class.
     *
     * @param friendId
     */
    @Override
    public void onFriendRequestSent(long friendId) {
    }

    /**
     * Called from a background thread, so we must run some things on UI thread when showing
     * updates here.
     *
     * @param friendId the friends ID.
     */
    @Override
    public void onFriendRemoveSent(long friendId) {
        DataHandler.gI().getSocialHandler().setFriendRequestsChanged(true);
        DataHandler.gI().getSocialHandler().setFriendsChanged(true);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentActivity a = getActivity();

                // Show a toast
                if (a instanceof BaseAppActivity) {
                    ((BaseAppActivity) a).showToast("The friend was removed.");
                }

                // Force update of the friends list, if we're in that activity
                if (a instanceof FriendsWindow) {
                    ((FriendsWindow) a).refresh();

                // Go back
                    a.getSupportFragmentManager().popBackStack();
                } else if (a instanceof HomeActivity) {
                // If not, we created this window from the map.
                // Then we need to clear the backstack from for example the short menu that
                // pops up before you choose to show the profile.
                // Let's pretent we did a back press
                    a.onBackPressed();
                }
            }
        });
    }
}