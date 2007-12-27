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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;


public class Mark {
    private JLabel label;
    
    private Integer pos;    // 0 - 1000
    
    public Mark(String iconPath, int pos){
        ClassLoader cl = this.getClass().getClassLoader();
        Icon icon = new ImageIcon(cl.getResource(iconPath));
        
        this.label = new JLabel(icon);
        this.pos = new Integer(pos);
    }
    
    public Integer getPos(){
        return pos;
    }
    
    public void setPos(int newPos){
        pos = new Integer(newPos);
    }
    
    public JLabel getLabel(){
        return label;
    }
}