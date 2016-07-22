package com.surajit.bitcanny.mqtttest;

import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Surajit Sarkar on 20/7/16.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected abstract @IdRes int getFragmentContainer();

    public void removeAllFragmentFromBackstack(){
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public void LoadFragment(Fragment fragment){
        LoadFragment(fragment, "mTrack");
    }

    public void LoadBackstackFragment(Fragment fragment){
        LoadBackstackFragment(fragment, "mTrack");
    }

    public void LoadFragment(Fragment fragment,String title){
        removeAllFragmentFromBackstack();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(getFragmentContainer(), fragment);
        fragmentTransaction.commit();
        setTitle(title);
    }

    public void LoadBackstackFragment(Fragment fragment, String title){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(getFragmentContainer(), fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        if(title!=null)
            setTitle(title);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment frg = fragmentManager.findFragmentById(getFragmentContainer());
        if(frg instanceof FragmentBackPressedListener){
            FragmentBackPressedListener l = (FragmentBackPressedListener)frg;
            if(!l.onBackPressed()){
                super.onBackPressed();
            }
        }
        else {
            super.onBackPressed();
        }
    }

    public void setActionBarTitle(String title){

        getSupportActionBar().setTitle(title);
    }
}
