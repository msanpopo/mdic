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

import mdic.MainFrame;
import mdic.Player;
import mdic.Result;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class DiffAction extends AbstractAction{
    private static final String TEXT = "Diff";
    
    private static final String ICON = "mdic/image/dialog-information.png";
    
    private Player player;
    private MainFrame frame;
    
    public DiffAction(Player player, MainFrame frame) {
        ClassLoader cl = this.getClass().getClassLoader();
        Icon icon  = new ImageIcon(cl.getResource(ICON));
        
        putValue(Action.NAME, TEXT);
        putValue(Action.SHORT_DESCRIPTION, TEXT);
        putValue(Action.SMALL_ICON, icon);
        putValue(Action.LARGE_ICON_KEY, icon);
//        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
        this.player = player;
        this.frame = frame;
    }
    
    public void actionPerformed(ActionEvent e) {
        String script = frame.getScript();
        String input = frame.getInput();
                
        Result result = new Result(script, input);
        frame.setResult(result);
    }
}