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
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    private static void appInit(String[] args){
        File file = null;
        
        for (String s : args) {
            System.out.println("file:" + s);
            if (s.matches(".+\\.mp3$")) {
                file = new File(s);
            }
        }
        
        Player player = new JPlayer();
        player.connect();
        
        if(file != null && file.exists()){
            player.setFile(file);
        }
        
        JFrame window = new MainFrame(player);
        window.setVisible(true);
    }
    
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                appInit(args);
            }
        });
    }
}