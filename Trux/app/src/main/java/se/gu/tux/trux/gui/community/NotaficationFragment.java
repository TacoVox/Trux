package se.gu.tux.trux.gui.community;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;




import se.gu.tux.trux.datastructure.Notification;

import se.gu.tux.trux.gui.main_home.HomeActivity;
import se.gu.tux.trux.gui.messaging.MessageActivity;
import tux.gu.se.trux.R;


public class NotaficationFragment extends Fragment{

    TextView messageTextView;
    Space space;
    LinearLayout linearLayout;

    Notification not;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TextView messagesTextView = (TextView) getActivity().findViewById(R.id.notaFicationMessage);
        Space space = (Space) getActivity().findViewById(R.id.notificationSpace);
        LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.notificationLayout);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.notafication_row, container, false);


        showNotafication();
        return view;

    }

    public void showNotafication(){

        if(not.isNewMessages()){
            messageTextView.setText("You Have A New Messages");
        }
        if(not.isNewFriends()){
            messageTextView.setText("You Have A New Friend Request");
        }

    }
    public void onClick(){
        space.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), HomeActivity.class);
                startActivity(intent);
            }
        });
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MessageActivity.class);
                startActivity(intent);
            }
        });
    }


}