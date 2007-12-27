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

import java.awt.Color;


public enum ResultType {
    ADD("add", 255, 160, 160),
    DEL("del", 255, 160, 160),
    CHANGE("change", 160, 255, 160),
    NOT_CHANGE("not_change", 255, 255, 255);
    
    private String name;
    private Color bgColor;
    
    private ResultType(String name, int red, int green, int blue){
        this.name = name;
        this.bgColor = new Color(red, green, blue);
    }
    
    public String getName(){
        return name;
    }
    
    public Color getColor(){
        return bgColor;
    }
}