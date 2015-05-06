package se.gu.tux.trux.gui.community;



import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class FragmentPageAdapterCommunity extends FragmentPagerAdapter {

    public FragmentPageAdapterCommunity(FragmentManager fm) {
        super(fm);
    }
    public Fragment getItem(int arg0) {


        //try {
        //    final Speed speed = (Speed) DataHandler.getInstance().getData(new Speed(0));
        //    if (speed != null && (Double) speed.getValue() > 0) {
                switch (arg0) {
                    case 0:
                        return new MapFrag();
                    default:
                        break;
                }
                return null;
            }
           // else switch (arg0){

           //     case 0:
           //         return new MapFrag();
           //     default:
           //         break;
         //   }
          //  return null;
       // }
       // catch (Exception e){
        //    e.printStackTrace();
       // }

  //  return null;}

    @Override
    public int getCount() {
        return 1;
    }


}
