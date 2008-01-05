/*
 * mdic - ディクテーション用メモ帳付きプレーヤー
 *
 * Copyright (C) 2007 sanpo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package mdic;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

public class Timer {

    private TimerListener listener;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> future;
    private Runnable task;
    private long startTime;
    private int base;
    private int second;

    public Timer() {
        this.listener = null;

        this.scheduler = Executors.newScheduledThreadPool(1);
        this.task = new MDicTimerTask();
        this.base = 0;
        this.second = 0;
    }

    private void fireClockChanged() {
        if (listener != null) {
            listener.clockChanged(this);
        }
    }

    public void setListener(TimerListener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        listener = null;
    }

    public void start() {
        //System.out.println("Timer.start():");
        if (future != null) {
            future.cancel(true);
        }

        future = scheduler.scheduleAtFixedRate(task, 1000, 1000, TimeUnit.MILLISECONDS);

        startTime = System.currentTimeMillis();
    }

    public void stop() {
        // System.out.println("Timer.stop():" + (int) (d / 1000.0));

        if (future != null) {
            future.cancel(true);
            future = null;
        }
        
        base = second;
        
        fireClockChanged();
    }

    public void reset() {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
        
        base = 0;
        second = 0;
        
        fireClockChanged();
    }

    public void shutdown() {
        System.out.println("GoClock.shutdown");
        scheduler.shutdownNow();
    }

    public void step() {
        long currentTime = System.currentTimeMillis();
        second =  base + (int) (currentTime - startTime) / 1000;
        // System.out.println("Timer.step()");
        fireClockChanged();
    }

    public String getTimeString() {
        return secToString(second);
    }

    private String secToString(int sec) {
        int m = sec / 60;
        int s = sec - m * 60;

        StringBuffer str = new StringBuffer();
        str.append(Integer.toString(m));
        str.append(":");
        if (s < 10) {
            str.append("0");
        }
        str.append(Integer.toString(s));

        return str.toString();
    }

    class MDicTimerTask extends TimerTask {

        public MDicTimerTask() {
        }

        public void run() {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    step();
                }
            });
        }
    }
}
