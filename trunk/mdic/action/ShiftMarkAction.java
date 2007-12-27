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

package mdic.action;

import mdic.Player;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

public class ShiftMarkAction extends AbstractAction{
    private static final String TEXT = "Shift";
    
    private static final String ICON = "mdic/image/shift.png";
    
    private Player player;
    
    public ShiftMarkAction(Player player) {
        ClassLoader cl = this.getClass().getClassLoader();
        Icon icon  = new ImageIcon(cl.getResource(ICON));
        
        putValue(Action.NAME, TEXT);
        putValue(Action.SHORT_DESCRIPTION, TEXT);
        putValue(Action.SMALL_ICON, icon);
        putValue(Action.LARGE_ICON_KEY, icon);
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        
        this.player = player;
    }
    
    public void actionPerformed(ActionEvent e) {
        player.shiftMark();
    }
}