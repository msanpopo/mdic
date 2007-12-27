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

import java.io.File;

public interface Player {
    void addPlayerListener(PlayerListener listener);
    
    int getFrame();
    float getSecond();
    PlayerState getState();

    boolean connect();
    void quit();
    
    void setFile(File file);
    
    void top();
    void backward(int second);
    void forward(int second);
    void play();
    void pause();
    void markFrom();
    void markTo();
    void shiftMark();
    void shiftFromMark(int n);
    void shiftToMark(int n);
    void jumpMark();
    void resetMark();
    void jump(float pos);
}