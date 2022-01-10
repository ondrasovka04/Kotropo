package cz.ucenislovicek.BakalariAPI;

import android.content.Context;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import org.joda.time.LocalDate;

import cz.ucenislovicek.BakalariAPI.rozvrh.AppSingleton;
import cz.ucenislovicek.BakalariAPI.rozvrh.RozvrhAPI;
import cz.ucenislovicek.BakalariAPI.rozvrh.RozvrhWrapper;
import cz.ucenislovicek.BakalariAPI.rozvrh.Utils;
import cz.ucenislovicek.BakalariAPI.rozvrh.items.Rozvrh;

public class LoadBag {
    public static final int SUCCESS = 0;
    public static final int LOGIN_FAILED = 1;
    public static final int UNEXPECTED_RESPONSE = 2;
    public static final int UNREACHABLE = 3;
    public static final int NO_CACHE = 4;
    public static final int ERROR = 5;
    public static final int OFFLINE = 5;

    private LiveData<RozvrhWrapper> liveData;

    private static Rozvrh currentRozvrh;


    private LocalDate week = null;
    private boolean offline = false;
    private RozvrhAPI rozvrhAPI = null;

    private final Context context;
    private final LifecycleOwner lifecycleOwner;
    public Rozvrh rozvrh;


    public LoadBag(Context context, LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;

    }

    public Context getContext() {
        return context;
    }


    public void getRozvrh(int weekIndex) {

        //what week is it from now (0: this, 1: next, -1: last, Integer.MAX_VALUE: permanent)
        if (weekIndex == Integer.MAX_VALUE) {
            week = null;
        } else {
            week = Utils.getDisplayWeekMonday(getContext()).plusWeeks(weekIndex);
        }


        rozvrhAPI = AppSingleton.getInstance(context).getRozvrhAPI();

        if (liveData != null) {
            liveData.removeObservers(lifecycleOwner);
        }
        liveData = rozvrhAPI.getLiveData(week);

        liveData.observe(lifecycleOwner, rozvrhWrapper -> {
            if (rozvrhWrapper.getSource() == RozvrhWrapper.SOURCE_CACHE) {
                onCacheResponse(rozvrhWrapper.getCode(), rozvrhWrapper.getRozvrh());

            } else if (rozvrhWrapper.getSource() == RozvrhWrapper.SOURCE_NET) {
                onNetResponse(rozvrhWrapper.getCode(), rozvrhWrapper.getRozvrh());
            }
        });
    }

    private void onNetResponse(int code, Rozvrh rozvrh) {
        //check if fragment was not removed while loading
        if (rozvrh != null) {
            rozvrhAPI.clearMemory();
            this.rozvrh = rozvrh;
        }
        //onNetLoaded
        if (code == SUCCESS) {
            if (offline) {
                rozvrhAPI.clearMemory();
                this.rozvrh = rozvrh;
            }
            offline = false;

        } else {
            offline = true;
            //displayInfo.setLoadingState(DisplayInfo.ERROR);

            if (code == UNREACHABLE) {
                //MainActivity.showAlert(context,context.getResources().getString(R.string.UNREACHABLE),context.getResources().getString(R.string.UNREACHABLEsubtext));
            } else if (code == UNEXPECTED_RESPONSE) {
                //MainActivity.showAlert(context,context.getResources().getString(R.string.ERROR),context.getResources().getString(R.string.ERRORsubtext));
            } else if (code == LOGIN_FAILED) {
                //MainActivity.showAlert(context,context.getResources().getString(R.string.LOGINFAIL),context.getResources().getString(R.string.LOGINFAIL));
            }

            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n" + rozvrh.getHodiny());

        }
    }

    private void onNetResponse(int code, Rozvrh rozvrh, Runnable runnable) {
        //check if fragment was not removed while loading
        if (rozvrh != null) {
            rozvrhAPI.clearMemory();
            this.rozvrh = rozvrh;
        }
        //onNetLoaded
        if (code == SUCCESS) {
            runnable.run();
            if (offline) {
                rozvrhAPI.clearMemory();
                this.rozvrh = rozvrh;
                //MainActivity.showAlert(context,context.getResources().getString(R.string.OFFLINE), context.getResources().getString(R.string.OFFLINEsubtext));
            }
            offline = false;

        } else {
            offline = true;
            //displayInfo.setLoadingState(DisplayInfo.ERROR);

            if (code == UNREACHABLE) {

                //MainActivity.showAlert(context,context.getResources().getString(R.string.UNREACHABLE),context.getResources().getString(R.string.UNREACHABLEsubtext));
            } else if (code == UNEXPECTED_RESPONSE) {
                //MainActivity.showAlert(context,context.getResources().getString(R.string.ERROR),context.getResources().getString(R.string.ERRORsubtext));
            } else if (code == LOGIN_FAILED) {
                //MainActivity.showAlert(context,context.getResources().getString(R.string.LOGINFAIL),context.getResources().getString(R.string.LOGINFAIL));
            }


        }

    }

    private void onCacheResponse(int code, Rozvrh rozvrh) {
        //check if fragment was not removed while loading
        if (code == SUCCESS) {
            this.rozvrh = rozvrh;
        } else {
            //MainActivity.showAlert(context,context.getResources().getString(R.string.ERROR),context.getResources().getString(R.string.ERRORsubtext));
        }
        //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" + rozvrh.getDny().get(0).getHodiny().get(2).getZkrskup());
    }

    public void refresh(int weekIndex, Runnable runnable, Runnable runnable2) {
        // displayInfo.setLoadingState(DisplayInfo.LOADING);
        if (weekIndex == Integer.MAX_VALUE) week = null;
        else week = Utils.getDisplayWeekMonday(getContext()).plusWeeks(weekIndex);

        rozvrhAPI.refresh(week, rw -> {
            /*if (rw.getCode() != SUCCESS){
                displayWeek(weekIndex, false);
            }else {*/
            onNetResponse(rw.getCode(), rw.getRozvrh(), runnable2);
            runnable.run();
            /*}*/
        });
    }

    public static Rozvrh getCurrentRozvrh() {
        return currentRozvrh;
    }


}


