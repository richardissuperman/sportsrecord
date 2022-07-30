package com.app.sportsrecord;

import android.os.CountDownTimer;

public class TimerTaskChain {

    private TaskTimer firstTimer;
    private TaskTimer currentTimer;

    public TimerTaskChain addTask(int delayMs, Task task) {
        if (firstTimer == null) {
            firstTimer = new TaskTimer(task, delayMs, 1000);
            currentTimer = firstTimer;
        } else {
            TaskTimer nextTimnerTask = new TaskTimer(task, delayMs, 1000);
            currentTimer.addContinueTask(nextTimnerTask);
            currentTimer = nextTimnerTask;
        }
        return this;
    }

    public void startChainTasks(){
        if (firstTimer != null) {
            firstTimer.start();
        } else {
            throw new RuntimeException("Can not start an empty task timer");
        }
    }

    public static abstract class Task {
        public void onTick(long l) { }

        abstract void onFinished();
    }

    public static class TaskTimer extends CountDownTimer {
        private Task mTask;
        private TaskTimer mContinueTask;

        public TaskTimer(Task task, int delay, int tick) {
            super(delay, tick);
            mTask = task;
        }

        @Override
        public void onTick(long l) {
            mTask.onTick(l);
        }

        @Override
        public void onFinish() {
            mTask.onFinished();
            if (mContinueTask != null) {
                mContinueTask.start();
            }
        }

        public void addContinueTask(TaskTimer taskTimer) {
            mContinueTask = taskTimer;
        }
    }
}
