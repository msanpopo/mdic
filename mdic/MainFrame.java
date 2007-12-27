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

import mdic.action.BackwardAction;
import mdic.action.ClearTextAction;
import mdic.action.DiffAction;
import mdic.action.ExitAction;
import mdic.action.ForwardAction;
import mdic.action.JumpAction;
import mdic.action.MarkFromAction;
import mdic.action.MarkToAction;
import mdic.action.OpenAction;
import mdic.action.PauseAction;
import mdic.action.ResetMarkAction;
import mdic.action.ShiftMarkAction;
import mdic.action.StartAction;
import mdic.action.TopAction;
import java.util.Hashtable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class MainFrame extends javax.swing.JFrame implements PlayerListener{
    private static final String PLAY_ICON = "mdic/image/media-playback-start.png";
    private static final String PAUSE_ICON = "mdic/image/media-playback-pause.png";
    private static final String FROM_ICON = "mdic/image/mark_from.png";
    private static final String TO_ICON = "mdic/image/mark_to.png";
    
    private Icon playIcon;
    private Icon pauseIcon;
    private Mark fromMark;
    private Mark toMark;
    private StyledDocument doc;
    private StyledDocument script;
    private Player player;
    private boolean slideIsAdjusting;
    
    private Result result;
    private int markShift;
    
    private Hashtable<Integer, JLabel> labelHash;
    private OpenAction openAction;
    private TopAction topAction;
    private BackwardAction backwardAction;
    private ForwardAction forwardAction;
    private StartAction startAction;
    private PauseAction pauseAction;
    private MarkFromAction markFromAction;
    private MarkToAction markToAction;
    private ShiftMarkAction shiftMarkAction;
    private JumpAction jumpAction;
    private ResetMarkAction resetMarkAction;
    private ClearTextAction clearTextAction;
    private DiffAction diffAction;
    
    public MainFrame(Player player) {
        ClassLoader cl = this.getClass().getClassLoader();
        playIcon  = new ImageIcon(cl.getResource(PLAY_ICON));
        pauseIcon = new ImageIcon(cl.getResource(PAUSE_ICON));

        this.fromMark = new Mark(FROM_ICON, 0);
        this.toMark = new Mark(TO_ICON, 1000);
        
        this.player = player;
        this.player.addPlayerListener(this);
        
        this.slideIsAdjusting = false;
        
        this.result = null;
        
        this.markShift = 20;
        
        this.labelHash = new Hashtable<Integer, JLabel>();
        this.labelHash.put(fromMark.getPos(), fromMark.getLabel());
        this.labelHash.put(toMark.getPos(), toMark.getLabel());
        
        initComponents();
        
        doc = inputTextPane.getStyledDocument();
        addStylesToDocument(doc);
        
        script = scriptTextPane.getStyledDocument();
        addStylesToDocument(script);
        
        slider.setLabelTable(labelHash);
        
        openAction = new OpenAction(player);
        topAction = new TopAction(player);
        backwardAction = new BackwardAction(player);
        forwardAction = new ForwardAction(player);
        startAction = new StartAction(player);
        pauseAction = new PauseAction(player);
        markFromAction = new MarkFromAction(player);
        markToAction = new MarkToAction(player);
        shiftMarkAction = new ShiftMarkAction(player);
        resetMarkAction = new ResetMarkAction(player);
        jumpAction = new JumpAction(player);
        clearTextAction = new ClearTextAction(player, this);
        diffAction = new DiffAction(player, this);
        
        openMenuItem.setAction(openAction);
        clearTextMenuItem.setAction(clearTextAction);
        diffMenuItem.setAction(diffAction);
        exitMenuItem.setAction(new ExitAction(player, this));
        startMenuItem.setAction(startAction);
        pauseMenuItem.setAction(pauseAction);
        jumpTopMenuItem.setAction(topAction);
        backwardMenuItem.setAction(backwardAction);
        forwardMenuItem.setAction(forwardAction);
        jumpMarkMenuItem.setAction(jumpAction);
        markFromMenuItem.setAction(markFromAction);
        markToMenuItem.setAction(markToAction);
        shiftMarkMenuItem.setAction(shiftMarkAction);
        resetMarkMenuItem.setAction(resetMarkAction);
        
        openButton.setAction(openAction);
        clearTextButton.setAction(clearTextAction);
        diffButton.setAction(diffAction);
        
        topButton.setAction(topAction);
        backwardButton.setAction(backwardAction);
        forwardButton.setAction(forwardAction);
        startButton.setAction(startAction);
        pauseButton.setAction(pauseAction);
        fromButton.setAction(markFromAction);
        toButton.setAction(markToAction);
        shiftButton.setAction(shiftMarkAction);
        jumpButton.setAction(jumpAction);
        
        openButton.setText("");
        clearTextButton.setText("");
        diffButton.setText("");
        
        topButton.setText("");
        backwardButton.setText("");
        forwardButton.setText("");
        startButton.setText("");
        pauseButton.setText("");
        
        fromButton.setText("");
        toButton.setText("");
        shiftButton.setText("");
        jumpButton.setText("");
    }
    
    public void fileChanged(String name) {
        this.setTitle(name);
        clearText();
        setResult(null);
    }
    
    public void frameChanged(int frame, int totalFrame, float sec, float totalSec){
        if(slider.getValueIsAdjusting() == false){   
            setSlider(sec, totalSec);
            setSliderLabel(sec, totalSec);
        }
    }
    
    public void stateChanged(PlayerState newState, PlayerState oldState){
        if(newState.isPlay()){
            statusLabel.setIcon(playIcon);
        }else{
            statusLabel.setIcon(pauseIcon);
        }
    }

    public void markChanged(float fromPos, float fromSec, float toPos, float toSec){
        
        fromMark.setPos((int)(fromPos * 1000));
        toMark.setPos((int)(toPos * 1000));
        
        labelHash.clear();
        labelHash.put(fromMark.getPos(), fromMark.getLabel());
        labelHash.put(toMark.getPos(), toMark.getLabel());

        fromLabel.setText(secToString(fromSec));
        toLabel.setText(secToString(toSec));
        
        slider.repaint();
    }
    
    private void setSlider(float current, float total) {
        int rate = (int) (current / total * 1000.0f);

        slider.setValue(rate);
    }
    
    private void setSliderLabel(float current, float total) {
        StringBuilder str = new StringBuilder();

        str.append(secToString(current));
        str.append(" / ");
        str.append(secToString(total));
        
        timeLabel.setText(str.toString());
    }
    
    private String secToString(float second){
        int min = (int)(second / 60);
        int sec = (int)(second - min * 60);
        
        StringBuilder str = new StringBuilder();
        str.append(min).append(":");
        if (sec < 10) {
            str.append("0");
        }
        str.append(sec);
        
        return str.toString();
    }
    
    public String getInput(){
        return inputTextPane.getText();
    }
    
    public String getScript(){
        return scriptTextPane.getText();
    }
    
    public void clearText(){
        inputTextPane.setText(null);
    }
    
    public void setResult(Result result){
        this.result= result;

        if(result == null){
            resultLabel.setText(null);
        }else{            
            inputTextPane.setText(null);
            scriptTextPane.setText(null);
        
            result.renderTo(doc);
            result.renderFrom(script);
            
            int correct = result.getCorrectNumber();
            int word = result.getWordNumber();
            StringBuilder str = new StringBuilder();
            str.append(correct);
            str.append(" / ");
            str.append(word);
            str.append(" ( ");
            str.append((int)((float) correct / word * 100));
            str.append(" % )");
            resultLabel.setText(str.toString());
        }
    }
    
    public void addText(String text, String style) {
        StringBuilder str = new StringBuilder();
        str.append(text).append("\n");
        try {
            // System.out.println("InOutPanel:" + style + ":" + text);
            doc.insertString(doc.getLength(), str.toString(), doc.getStyle(style));
        } catch (BadLocationException e) {
            System.err.println("error:addText : " + text + " :" + e);
        }
    }
    
    private void addStylesToDocument(StyledDocument doc) {
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(def, "SansSerif");

        for(ResultType rt : ResultType.values()){
            Style s = doc.addStyle(rt.getName(), def);
            StyleConstants.setBackground(s, rt.getColor());
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        fromButton = new javax.swing.JButton();
        toButton = new javax.swing.JButton();
        jumpButton = new javax.swing.JButton();
        shiftButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        slider = new javax.swing.JSlider();
        timeLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        jToolBar3 = new javax.swing.JToolBar();
        topButton = new javax.swing.JButton();
        backwardButton = new javax.swing.JButton();
        startButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        forwardButton = new javax.swing.JButton();
        jToolBar2 = new javax.swing.JToolBar();
        openButton = new javax.swing.JButton();
        clearTextButton = new javax.swing.JButton();
        diffButton = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        inputTextPane = new javax.swing.JTextPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        scriptTextPane = new javax.swing.JTextPane();
        jToolBar4 = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        fromLabel = new javax.swing.JLabel();
        decFromButton = new javax.swing.JButton();
        incFromButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        toLabel = new javax.swing.JLabel();
        decToButton = new javax.swing.JButton();
        incToButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        resultLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        clearTextMenuItem = new javax.swing.JMenuItem();
        diffMenuItem = new javax.swing.JMenuItem();
        playMenu = new javax.swing.JMenu();
        jumpTopMenuItem = new javax.swing.JMenuItem();
        backwardMenuItem = new javax.swing.JMenuItem();
        startMenuItem = new javax.swing.JMenuItem();
        pauseMenuItem = new javax.swing.JMenuItem();
        forwardMenuItem = new javax.swing.JMenuItem();
        markMenu = new javax.swing.JMenu();
        markFromMenuItem = new javax.swing.JMenuItem();
        markToMenuItem = new javax.swing.JMenuItem();
        jumpMarkMenuItem = new javax.swing.JMenuItem();
        shiftMarkMenuItem = new javax.swing.JMenuItem();
        resetMarkMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        decFromMenuItem = new javax.swing.JMenuItem();
        incFromMenuItem = new javax.swing.JMenuItem();
        decToMenuItem = new javax.swing.JMenuItem();
        incToMenuItem = new javax.swing.JMenuItem();

        setLocationByPlatform(true);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                formComponentHidden(evt);
            }
        });

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        fromButton.setText("From");
        fromButton.setFocusable(false);
        fromButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        fromButton.setOpaque(false);
        fromButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(fromButton);

        toButton.setText("To");
        toButton.setFocusable(false);
        toButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toButton.setOpaque(false);
        toButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(toButton);

        jumpButton.setText("Jump");
        jumpButton.setFocusable(false);
        jumpButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jumpButton.setOpaque(false);
        jumpButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jumpButton);

        shiftButton.setText("Shift");
        shiftButton.setFocusable(false);
        shiftButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        shiftButton.setOpaque(false);
        shiftButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(shiftButton);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        slider.setMaximum(1000);
        slider.setPaintLabels(true);
        slider.setValue(0);
        slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderStateChanged(evt);
            }
        });

        timeLabel.setFont(new java.awt.Font("Dialog", 1, 12));
        timeLabel.setText("0:00 / 0:00");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(slider, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeLabel)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(slider, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(timeLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE))
                .addContainerGap())
        );

        jToolBar3.setFloatable(false);
        jToolBar3.setRollover(true);

        topButton.setText("Top");
        topButton.setBorderPainted(false);
        topButton.setFocusable(false);
        topButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        topButton.setOpaque(false);
        topButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar3.add(topButton);

        backwardButton.setText("<-");
        backwardButton.setBorderPainted(false);
        backwardButton.setFocusable(false);
        backwardButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        backwardButton.setOpaque(false);
        backwardButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar3.add(backwardButton);

        startButton.setText("Start");
        startButton.setBorderPainted(false);
        startButton.setFocusable(false);
        startButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        startButton.setOpaque(false);
        startButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar3.add(startButton);

        pauseButton.setText("Pause");
        pauseButton.setBorderPainted(false);
        pauseButton.setFocusable(false);
        pauseButton.setOpaque(false);
        jToolBar3.add(pauseButton);

        forwardButton.setText("->");
        forwardButton.setBorderPainted(false);
        forwardButton.setFocusable(false);
        forwardButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        forwardButton.setOpaque(false);
        forwardButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar3.add(forwardButton);

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);

        openButton.setText("Open");
        openButton.setBorderPainted(false);
        openButton.setFocusPainted(false);
        openButton.setFocusable(false);
        openButton.setOpaque(false);
        jToolBar2.add(openButton);

        clearTextButton.setText("Clear");
        clearTextButton.setBorderPainted(false);
        clearTextButton.setFocusable(false);
        clearTextButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        clearTextButton.setOpaque(false);
        clearTextButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(clearTextButton);

        diffButton.setText("Diff");
        diffButton.setBorderPainted(false);
        diffButton.setFocusable(false);
        diffButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        diffButton.setOpaque(false);
        diffButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(diffButton);

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setDividerSize(5);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        inputTextPane.setFont(new java.awt.Font("Dialog", 1, 12));
        inputTextPane.setPreferredSize(new java.awt.Dimension(6, 150));
        jScrollPane2.setViewportView(inputTextPane);

        jSplitPane1.setTopComponent(jScrollPane2);

        scriptTextPane.setFont(new java.awt.Font("Dialog", 1, 12));
        jScrollPane1.setViewportView(scriptTextPane);

        jSplitPane1.setRightComponent(jScrollPane1);

        jToolBar4.setFloatable(false);
        jToolBar4.setRollover(true);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mdic/image/from_22x22.png"))); // NOI18N
        jToolBar4.add(jLabel1);

        fromLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        fromLabel.setText(" 0:00");
        fromLabel.setMaximumSize(new java.awt.Dimension(50, 15));
        fromLabel.setMinimumSize(new java.awt.Dimension(50, 15));
        fromLabel.setPreferredSize(new java.awt.Dimension(50, 15));
        jToolBar4.add(fromLabel);

        decFromButton.setText("<");
        decFromButton.setFocusable(false);
        decFromButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        decFromButton.setOpaque(false);
        decFromButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        decFromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decFromButtonActionPerformed(evt);
            }
        });
        jToolBar4.add(decFromButton);

        incFromButton.setText(">");
        incFromButton.setFocusable(false);
        incFromButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        incFromButton.setOpaque(false);
        incFromButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        incFromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                incFromButtonActionPerformed(evt);
            }
        });
        jToolBar4.add(incFromButton);

        jLabel3.setText("      ");
        jToolBar4.add(jLabel3);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mdic/image/to_22x22.png"))); // NOI18N
        jToolBar4.add(jLabel2);

        toLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        toLabel.setText(" 0:00");
        toLabel.setMaximumSize(new java.awt.Dimension(50, 15));
        toLabel.setMinimumSize(new java.awt.Dimension(50, 15));
        toLabel.setPreferredSize(new java.awt.Dimension(50, 15));
        jToolBar4.add(toLabel);

        decToButton.setText("<");
        decToButton.setFocusable(false);
        decToButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        decToButton.setOpaque(false);
        decToButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        decToButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decToButtonActionPerformed(evt);
            }
        });
        jToolBar4.add(decToButton);

        incToButton.setText(">");
        incToButton.setFocusable(false);
        incToButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        incToButton.setOpaque(false);
        incToButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        incToButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                incToButtonActionPerformed(evt);
            }
        });
        jToolBar4.add(incToButton);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        resultLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(resultLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(resultLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
        );

        fileMenu.setText("File");

        openMenuItem.setText("Open");
        fileMenu.add(openMenuItem);
        fileMenu.add(jSeparator1);

        exitMenuItem.setText("Exit");
        fileMenu.add(exitMenuItem);

        jMenuBar1.add(fileMenu);

        editMenu.setText("Edit");

        clearTextMenuItem.setText("Clear");
        editMenu.add(clearTextMenuItem);

        diffMenuItem.setText("Diff");
        editMenu.add(diffMenuItem);

        jMenuBar1.add(editMenu);

        playMenu.setText("Play");

        jumpTopMenuItem.setText("Top");
        playMenu.add(jumpTopMenuItem);

        backwardMenuItem.setText("Backward");
        playMenu.add(backwardMenuItem);

        startMenuItem.setText("Start");
        playMenu.add(startMenuItem);

        pauseMenuItem.setText("Pause");
        playMenu.add(pauseMenuItem);

        forwardMenuItem.setText("Forward");
        playMenu.add(forwardMenuItem);

        jMenuBar1.add(playMenu);

        markMenu.setText("Mark");

        markFromMenuItem.setText("Mark (From)");
        markMenu.add(markFromMenuItem);

        markToMenuItem.setText("Mark (To)");
        markMenu.add(markToMenuItem);

        jumpMarkMenuItem.setText("Jump");
        markMenu.add(jumpMarkMenuItem);

        shiftMarkMenuItem.setText("Shift");
        markMenu.add(shiftMarkMenuItem);

        resetMarkMenuItem.setText("Reset");
        markMenu.add(resetMarkMenuItem);
        markMenu.add(jSeparator2);

        decFromMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, java.awt.event.InputEvent.CTRL_MASK));
        decFromMenuItem.setText("< (From)");
        decFromMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decFromMenuItemActionPerformed(evt);
            }
        });
        markMenu.add(decFromMenuItem);

        incFromMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, java.awt.event.InputEvent.CTRL_MASK));
        incFromMenuItem.setText("> (From)");
        incFromMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                incFromMenuItemActionPerformed(evt);
            }
        });
        markMenu.add(incFromMenuItem);

        decToMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, java.awt.event.InputEvent.CTRL_MASK));
        decToMenuItem.setText("< (To)");
        decToMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decToMenuItemActionPerformed(evt);
            }
        });
        markMenu.add(decToMenuItem);

        incToMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.CTRL_MASK));
        incToMenuItem.setText("> (To)");
        incToMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                incToMenuItemActionPerformed(evt);
            }
        });
        markMenu.add(incToMenuItem);

        jMenuBar1.add(markMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(309, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jToolBar4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderStateChanged
        float rate = slider.getValue() / 1000.0f;
        float sec = player.getSecond();
        float pos = rate * sec;
        setSliderLabel(pos, sec);
        
        // スライダーのドラッグが終わるタイミングをとる
        if (slideIsAdjusting == false && slider.getValueIsAdjusting()) {
            slideIsAdjusting = true;

        }else if(slideIsAdjusting == true && slider.getValueIsAdjusting() == false){
            System.out.println("slider:" + rate);
            
            slideIsAdjusting = false;
            player.jump(rate);
        }
//GEN-LAST:event_sliderStateChanged
    }                                   

    private void formComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
        System.out.println("MainFrame componentHidden");
        dispose();
        player.quit();
    }//GEN-LAST:event_formComponentHidden

    private void decFromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decFromButtonActionPerformed
        player.shiftFromMark(-markShift);
    }//GEN-LAST:event_decFromButtonActionPerformed

    private void incFromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_incFromButtonActionPerformed
        player.shiftFromMark(markShift);
    }//GEN-LAST:event_incFromButtonActionPerformed

    private void decToButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decToButtonActionPerformed
        player.shiftToMark(-markShift);
    }//GEN-LAST:event_decToButtonActionPerformed

    private void incToButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_incToButtonActionPerformed
        player.shiftToMark(markShift);
    }//GEN-LAST:event_incToButtonActionPerformed

    private void decFromMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decFromMenuItemActionPerformed
        player.shiftFromMark(-markShift);
}//GEN-LAST:event_decFromMenuItemActionPerformed

    private void incFromMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_incFromMenuItemActionPerformed
        player.shiftFromMark(markShift);
    }//GEN-LAST:event_incFromMenuItemActionPerformed

    private void decToMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decToMenuItemActionPerformed
        player.shiftToMark(-markShift);
    }//GEN-LAST:event_decToMenuItemActionPerformed

    private void incToMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_incToMenuItemActionPerformed
        player.shiftToMark(markShift);
    }//GEN-LAST:event_incToMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backwardButton;
    private javax.swing.JMenuItem backwardMenuItem;
    private javax.swing.JButton clearTextButton;
    private javax.swing.JMenuItem clearTextMenuItem;
    private javax.swing.JButton decFromButton;
    private javax.swing.JMenuItem decFromMenuItem;
    private javax.swing.JButton decToButton;
    private javax.swing.JMenuItem decToMenuItem;
    private javax.swing.JButton diffButton;
    private javax.swing.JMenuItem diffMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JButton forwardButton;
    private javax.swing.JMenuItem forwardMenuItem;
    private javax.swing.JButton fromButton;
    private javax.swing.JLabel fromLabel;
    private javax.swing.JButton incFromButton;
    private javax.swing.JMenuItem incFromMenuItem;
    private javax.swing.JButton incToButton;
    private javax.swing.JMenuItem incToMenuItem;
    private javax.swing.JTextPane inputTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JToolBar jToolBar4;
    private javax.swing.JButton jumpButton;
    private javax.swing.JMenuItem jumpMarkMenuItem;
    private javax.swing.JMenuItem jumpTopMenuItem;
    private javax.swing.JMenuItem markFromMenuItem;
    private javax.swing.JMenu markMenu;
    private javax.swing.JMenuItem markToMenuItem;
    private javax.swing.JButton openButton;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JButton pauseButton;
    private javax.swing.JMenuItem pauseMenuItem;
    private javax.swing.JMenu playMenu;
    private javax.swing.JMenuItem resetMarkMenuItem;
    private javax.swing.JLabel resultLabel;
    private javax.swing.JTextPane scriptTextPane;
    private javax.swing.JButton shiftButton;
    private javax.swing.JMenuItem shiftMarkMenuItem;
    private javax.swing.JSlider slider;
    private javax.swing.JButton startButton;
    private javax.swing.JMenuItem startMenuItem;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JButton toButton;
    private javax.swing.JLabel toLabel;
    private javax.swing.JButton topButton;
    // End of variables declaration//GEN-END:variables
}
