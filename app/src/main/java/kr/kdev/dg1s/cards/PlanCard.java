package kr.kdev.dg1s.cards;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import kr.kdev.dg1s.R;
import kr.kdev.dg1s.cards.provider.PlanProvider;
import kr.kdev.dg1s.cards.provider.datatypes.Plan;

public class PlanCard implements PlanProvider.PlanProviderInterface {

    private final int targetDelay = 1000;
    PlanProvider provider;
    Context context;
    ViewGroup viewParent;
    CardView planCard;
    LinearLayout header;
    FrameLayout contents;
    LinearLayout schedule;
    TextView planText;
    LinearLayout summary;
    TextView summaryText;
    private CardViewStatusNotifier statusNotifier;
    private long timeAtLastViewChange;

    public PlanCard(Context arg0, ViewGroup parent, Activity activity) {

        try { // Checks if callback is implemented
            statusNotifier = (CardViewStatusNotifier) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    "needs to implement CardViewStatusNotifier");
        }

        context = arg0;

        viewParent = parent;

        planCard = (CardView) LayoutInflater.from(context).inflate(R.layout.card_plan, parent, false);
        header = (LinearLayout) planCard.findViewById(R.id.header);
        contents = (FrameLayout) planCard.findViewById(R.id.contents);

        schedule = (LinearLayout) contents.findViewById(R.id.schedule);

        planText = (TextView) schedule.findViewById(R.id.plans);
        summary = (LinearLayout) schedule.findViewById(R.id.summary_area);
        summaryText = (TextView) summary.findViewById(R.id.summary);

        provider = new PlanProvider(context, this);
        provider.requestPlan(false);

        hidePlans();
        timeAtLastViewChange = System.currentTimeMillis() - targetDelay;

        parent.addView(planCard);
    }

    public void onPlanReceived(boolean succeeded, Plan plan, ArrayList<Integer> summary) {
        if (succeeded) {
            statusNotifier.notifyCompletion(this, CardViewStatusNotifier.SUCCESS);
        } else {
            statusNotifier.notifyCompletion(this, CardViewStatusNotifier.FAILURE);
        }
        showSchedules(plan, summary);
    }

    public void requestUpdate(boolean isForced) {
        hidePlans();
        provider.requestPlan(isForced);
    }

    long delay() {
        Log.d("WeatherCardAnimationDelay",
                String.valueOf((timeAtLastViewChange + targetDelay) - System.currentTimeMillis()) + " " +
                        "milliseconds delayed");
        return (timeAtLastViewChange + targetDelay) - System.currentTimeMillis();
    }

    void hidePlans() {
        final Handler handler = new Handler();
        long delay = delay();
        if (delay < 0) {
            delay = 0;
        }
        handler.postDelayed(new hideSchdulesAction(), delay);
    }

    void showSchedules(Plan plan, ArrayList<Integer> summaryArray) {
        final Handler handler = new Handler();
        long delay = delay();
        if (delay < 0) {
            delay = 0;
        }
        handler.postDelayed(new showSchedulesAction(plan, summaryArray), delay);
    }

    private void addView(View view, LinearLayout viewParent) {
        if (view.getParent() == null) {
            viewParent.addView(view);
        }
    }

    class hideSchdulesAction implements Runnable {
        @Override
        public void run() {
            schedule.removeAllViews();
        }
    }

    class showSchedulesAction implements Runnable {

        Plan plan;
        ArrayList<Integer> summaryArray;

        showSchedulesAction(Plan planInput, ArrayList<Integer> summaryInput) {
            this.plan = planInput;
            this.summaryArray = summaryInput;
        }

        @Override
        public void run() {
            if ("".equals(plan.getPlans())) {
                planText.setText(context.getString(R.string.error_no_schudules));
            } else {
                planText.setText(plan.getPlans());
            }

            if (summaryArray.get(0) == -1) {
                Log.e("GRADE", "GRADE NOT SET OR LOADED W/ ERRORS");
                summaryText.setText(context.getString(R.string.set_grade));
            } else {
                summaryText.setText(context.getString(R.string.day_total) + summaryArray.get(0) + "\n" +
                        context.getString(R.string.day_events) + summaryArray.get(1) + "\n" +
                        context.getString(R.string.day_studying) + summaryArray.get(2));
            }

            addView(planText, schedule);
            addView(summary, schedule);
        }
    }

}
