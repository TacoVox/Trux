package se.gu.tux.trux.gui.messaging;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import se.gu.tux.trux.datastructure.Friend;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-13.
 */
public class ChatFragment extends Fragment
{



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_chat_head, container, false);

        TextView tv = (TextView) view.findViewById(R.id.chat_head_text_view);

        MessageActivity act = (MessageActivity) getActivity();

        Friend friend = act.getListFriend();

        tv.setText(friend.getUsername());

        return view;
    }


} // en class
