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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;

public class JPlayer implements Player {

    private ArrayList<PlayerListener> listener;
    private PlayerState state;
    private PlayThread thread;
    private File file;
    private AudioDevice audioDevice;
    private Bitstream bitstream;
    private Decoder decoder;
    private float totalSecond;
    private int totalFrame;
    /**
     * 現在処理中のフレーム。<br>
     */
    private int currentReadingFrame;
    private int currentPlayingFrame;
    private int fromMark;  // frame
    private int toMark;     // frame 
    /**
     * 音として聞こえているフレームと処理しているフレームには時間のずれがある。それを補正するためにゲタをはかせる。
     */
    private int frameOffset;
    
    private int offsetCounter;

    public JPlayer() {
        listener = new ArrayList<PlayerListener>();

        state = PlayerState.STOP;
        thread = null;
        file = null;

        decoder = null;
        audioDevice = null;
        bitstream = null;
        
        totalSecond = 0.0f;
        totalFrame = 0;
        currentReadingFrame = 0;
        currentPlayingFrame = 0;

        fromMark = 0;
        toMark = 0;
        
        frameOffset = 50;
        offsetCounter = 0;
    }

    public void addPlayerListener(PlayerListener listener) {
        this.listener.add(listener);
    }

    public int getFrame() {
        return totalFrame;
    }

    public float getSecond() {
        return totalSecond;
    }

    public float getCurrentSecond() {
        return totalSecond * currentPlayingFrame / totalFrame;
    }

    public PlayerState getState() {
        return state;
    }

    private int countFrame() throws BitstreamException{
        int count = 0;
        while(true) {
            boolean ret = skipFrame();
            if(ret == false){
                break;
            }
            count += 1;
        }
        return count;
    }

    private boolean skipFrame() throws BitstreamException{
        Header h = bitstream.readFrame();
        if (h == null) {
            return false;
        }
        bitstream.closeFrame();
        
        return true;
    }

    public void setFile(File file) {
        if (state.isPlay()) {
            pause();
        }

        this.file = file;

        initLength(file);
        setCurrentFrame(0);
        
        fromMark = 0;
        toMark = totalFrame;
        
        fireFileChanged();
        fireMarkChanged();
    }

    private void initLength(File file) {
        long streamSize = file.length();
        System.out.println("fileLength:" + streamSize);
        
        createBitstream(file, 0);
        
        Header header = null;
        try {
            header = bitstream.readFrame();

            totalFrame = countFrame();
            totalSecond = header.total_ms((int) streamSize) / 1000.0f;

            System.out.println("totalSecond:" + totalSecond);
            System.out.println("totalFrame:" + totalFrame);
            
        } catch (BitstreamException ex) {
            System.err.println("error:JPlayer.initLength:" + ex);
        }
    }

    private void createBitstream(File file, int targetFrame) {
        bitstream = null;
        
        try {
            FileInputStream fin = new FileInputStream(file);
            BufferedInputStream bin = new BufferedInputStream(fin);
            bitstream = new Bitstream(bin);

        } catch (FileNotFoundException ex) {
            System.err.println("error:JPlayer.getBitstream:" + ex);
        }

        try{
            for (int i = 0; i < targetFrame; ++i) {
                skipFrame();
            }
        }catch(BitstreamException ex){
            System.err.println("error:JPlayer.createBitstream:" + ex);
        }
        
        fireFrameChanged();
    }

    public synchronized void closeAll(){
        if(audioDevice != null){
            audioDevice.close();
        }

        if(bitstream != null){
            try{
                bitstream.close();
            }catch(BitstreamException ex){
                System.err.println("error:JPlayer.pause:" + ex);
            }
            bitstream = null;
        }

        decoder = null;
    }
    
    public boolean connect() {
        return true;
    }

    public void quit() {
        pause();
        System.exit(0);
    }

    public void top() {
        PlayerState oldState = state;
        
        pause();

        setCurrentFrame(0);

        if (oldState.isPlay()) {
            play();
        }
    }

    public void backward(int sec){
        PlayerState oldState = state;
        
        pause();
        
        int frame = (int)((float)totalFrame * sec / totalSecond);
        int pos = currentPlayingFrame - frame;
        setCurrentFrame(pos);

        if (oldState.isPlay()) {
            play();
        }
    }
    
    public void forward(int sec){
        PlayerState oldState = state;
        
        pause();
        
        int frame = (int)((float)totalFrame * sec / totalSecond);
        int pos = currentPlayingFrame + frame;
        setCurrentFrame(pos);

        if (oldState.isPlay()) {
            play();
        }
    }
    
    private synchronized void openAll(){
        decoder = new Decoder();
        
        try {
            audioDevice = FactoryRegistry.systemRegistry().createAudioDevice();
            audioDevice.open(decoder);
        } catch (JavaLayerException ex) {
            System.err.println("error:JPlayer.openAll:" + ex);
        }
        
        createBitstream(file, currentPlayingFrame);
    }
    
    public void play() {
        if(state.isPlay()){
            return;
        }
        
        if(file == null){
            return;
        }

        System.out.println("play:read:" + currentReadingFrame + " play:" + currentPlayingFrame);
        openAll();
        
        offsetCounter = 0;
        
        thread = new PlayThread(this);
        thread.start();

        setState(PlayerState.PLAY);
    }

    public void pause() {
        System.out.println("pause:read:" + currentReadingFrame + " play:" + currentPlayingFrame);
        if(state.isPlay() == false){
            return;
        }
        
        if(thread != null){
            thread.playStop();
        }
        
        synchronized(this){
            if(audioDevice != null){
                audioDevice.close();
            }
        }
  
        if(thread != null){
            try{
                thread.join();
            }catch(InterruptedException ex){
                System.err.println("error:JPlayer.pause:" + ex);
            }
            thread = null;
        }

        closeAll();

        currentReadingFrame = currentPlayingFrame;
        
        setState(PlayerState.STOP);
    }

    public void markFrom() {
        System.out.println("markFrom:read:" + currentReadingFrame + " play:" + currentPlayingFrame);
        fromMark = currentPlayingFrame;
        if(fromMark < 0){
            fromMark = 0;
        }
        if(fromMark > toMark){
            System.err.println("erro:JPlayer.markFrom:fromMark > toMark :from:" + fromMark + ":to:" + toMark);
        }
        System.out.println("JPlayer.markFrom:" + fromMark);
        
        fireMarkChanged();
    }
    
    public void markTo() {
        System.out.println("markTo:read:" + currentReadingFrame + " play:" + currentPlayingFrame);
        toMark = currentPlayingFrame;
        if(toMark < 0){
            toMark = 0;
        }
        if(fromMark > toMark){
            System.err.println("erro:JPlayer.markTo:fromMark > toMark :from:" + fromMark + ":to:" + toMark);
        }
        System.out.println("JPlayer.markTo:" + toMark);
        
        fireMarkChanged();
        
        pause();
    }
    
    public void shiftMark(){
        System.out.println("shiftMark:f:" + fromMark + " t:" + toMark);
        fromMark = toMark;
        toMark = totalFrame;
        fireMarkChanged();
        System.out.println("shiftMark:f:" + fromMark + " t:" + toMark);
    }
    
    public void shiftFromMark(int n){
        fromMark += n;
        if(fromMark < 0){
            fromMark = 0;
        }
        if(fromMark > totalFrame){
            fromMark = totalFrame;
        }
        fireMarkChanged();
    }
    
    public void shiftToMark(int n){
        toMark += n;
        if(toMark < 0){
            toMark = 0;
        }
        if(toMark > totalFrame){
            toMark = totalFrame;
        }
        fireMarkChanged();
    }
    
    public void jumpMark() {
        pause();

        setCurrentFrame(fromMark);

        play();
    }

    public void resetMark(){
        fromMark = 0;
        toMark = totalFrame;
        fireMarkChanged();
    }
    
    public void jump(float pos) {
        System.out.println("jump:" + pos);
        PlayerState oldState = state;

        pause();

        int newFrame = (int)(pos * totalFrame);
        setCurrentFrame(newFrame);
                
        if (oldState.isPlay()) {
            play();
        }
    }

    private void setState(PlayerState newState) {
        PlayerState oldState = state;
        state = newState;

        fireStateChanged(state, oldState);
    }

    private void setCurrentFrame(int newFrame){
        System.out.println("setCurrentFrame:" + newFrame);
        currentPlayingFrame = newFrame;
        currentReadingFrame = newFrame;
        fireFrameChanged();
    }
    
    private void fireFileChanged() {
        if (listener.isEmpty() == false) {
            for (PlayerListener l : listener) {
                l.fileChanged(file.getName());
            }
        }
    }
    
    private void fireFrameChanged() {
        if (listener.isEmpty() == false) {
            float currentSecond = totalSecond * currentPlayingFrame / totalFrame;
            for (PlayerListener l : listener) {
                FrameChangedSignal signal = new FrameChangedSignal(l, currentPlayingFrame, totalFrame, currentSecond, totalSecond);
                SwingUtilities.invokeLater(signal);
            }
        }
    }

    private void fireStateChanged(PlayerState newState, PlayerState oldState) {
        if (listener.isEmpty() == false) {
            for (PlayerListener l : listener) {
                StateChangedSignal signal = new StateChangedSignal(l, newState, oldState);
                SwingUtilities.invokeLater(signal);
            }
        }
    }

    private void fireMarkChanged() {
        float fromPos = (float)fromMark / totalFrame;
        float fromSec = totalSecond * fromPos;
        float toPos = (float)toMark / totalFrame;
        float toSec = totalSecond * toPos;
        if (listener.isEmpty() == false) {
            for (PlayerListener l : listener) {
                l.markChanged(fromPos, fromSec, toPos, toSec);
            }
        }
    }
    
    class FrameChangedSignal extends Thread {
        private PlayerListener listener;
        private int currentFrame;
        private int totalFrame;
        private float currentSec;
        private float totalSec;
        
        public FrameChangedSignal(PlayerListener listener, int currentFrame, int totalFrame, float currentSec, float totalSec) {
            this.listener = listener;
            this.currentFrame = currentFrame;
            this.totalFrame = totalFrame;
            this.currentSec = currentSec;
            this.totalSec = totalSec;
        }

        @Override
        public void run() {
            listener.frameChanged(currentFrame, totalFrame, currentSec, totalSec);
        }
    }
    
    class StateChangedSignal extends Thread {
        private PlayerListener listener;
        private PlayerState newState;
        private PlayerState oldState;
        
        public StateChangedSignal(PlayerListener listener, PlayerState newState, PlayerState oldState) {
            this.listener = listener;
            this.newState = newState;
            this.oldState = oldState;
        }

        @Override
        public void run() {
            listener.stateChanged(newState, oldState);
        }
    }
    
    class PlayThread extends Thread {

        private JPlayer player;
        private boolean stopQuickly;

        public PlayThread(JPlayer player) {
            this.player = player;
            this.stopQuickly = false;
        }

        public void playStop() {
            stopQuickly = true;
        }

        /**
         * 外部からの playStop() に対してはすぐに終了する。
         * それ以外の toMark や終端へ到達した場合はバッファーに入れたデータを再生した後で終了する。
         */
        @Override
        public void run() {
            boolean retval = true;
            boolean reachMark = false;
            try {
                while (retval && stopQuickly == false && reachMark == false) {
                    retval = decodeFrame();
                    stepFrame();

                    if(currentReadingFrame == toMark){
                        System.out.println("thrad reach:" + toMark);
                        reachMark = true;
                    }
                }
            } catch (JavaLayerException ex) {
                System.err.println("error:run.loop:" + ex);
            }

            if(stopQuickly == false){  // 最後まで再生された
                synchronized(player){
                    audioDevice.flush();
                }
            }
            boolean goTop = !retval; 
            finish(goTop);
            player.closeAll();
 
            System.out.println("PlayerThread:finished:read:" + currentReadingFrame + " play:" + currentPlayingFrame);
        }
        
        private void stepFrame(){
            currentReadingFrame += 1;

            if(offsetCounter < frameOffset){
                offsetCounter += 1;
            }else{
                currentPlayingFrame += 1;
                fireFrameChanged();
            }
        }

        private void finish(boolean goTop){
            if (goTop) {
                setCurrentFrame(0);
            } else {
                /*
                 * 中断によるスレッド終了の時は読み込んだところまで再生されたことにする。
                 */
                setCurrentFrame(currentReadingFrame);
            }
            setState(PlayerState.STOP);
        }

        private synchronized boolean decodeFrame() throws JavaLayerException {
            Header h = bitstream.readFrame();
            if (h == null) {
                return false;
            }

            SampleBuffer output = (SampleBuffer) decoder.decodeFrame(h, bitstream);

            synchronized (this) {
                audioDevice.write(output.getBuffer(), 0, output.getBufferLength());
            }

            bitstream.closeFrame();

            return true;
        }
    }
}