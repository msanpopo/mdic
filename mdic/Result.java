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

import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import diff.Diff;
import diff.Difference;

public class Result {

    private String script;
    private String input;
    private Word[] fromList;
    private Word[] toList;
    private List<Difference> list;

    public Result(String script, String input) {

        this.script = script;
        this.input = input;

        String[] fromArray = script.split("\\s+");
        String[] toArray = input.split("\\s+");

        int nFrom = fromArray.length;
        int nTo = toArray.length;

        fromList = new Word[nFrom];
        toList = new Word[nTo];

        for (int i = 0; i < nFrom; ++i) {
            Word word = new Word(fromArray[i]);
            fromList[i] = word;
        }

        for (int i = 0; i < nTo; ++i) {
            Word word = new Word(toArray[i]);
            toList[i] = word;
        }

        Diff diff = new Diff(fromArray, toArray);

        list = diff.diff();

        System.out.println("Difference##########");

        for (Object o : list) {
            if (o instanceof Difference) {
                Difference d = (Difference) o;
                int delStart = d.getDeletedStart();
                int delEnd = d.getDeletedEnd();
                int addStart = d.getAddedStart();
                int addEnd = d.getAddedEnd();

                if (delEnd == -1) {
                    for (int i = addStart; i <= addEnd; ++i) {
                        toList[i].setAdd();
                    }
                } else if (addEnd == -1) {
                    for (int i = delStart; i <= delEnd; ++i) {
                        fromList[i].setDel();
                    }
                } else {
                    for (int i = addStart; i <= addEnd; ++i) {
                        toList[i].setChange();
                    }
                    for (int i = delStart; i <= delEnd; ++i) {
                        fromList[i].setChange();
                    }
                }
                System.out.println("Difference:" + d);
            }
        }
    }

    public int getWordNumber(){
        return fromList.length;
    }
    
    public int getCorrectNumber(){
        int count = 0;
        for(Word w : fromList){
            if(w.getResultType() == ResultType.NOT_CHANGE){
                count += 1;
            }
        }
        
        return count;
    }
    
    public void renderFrom(StyledDocument doc) {
        for (Word w : fromList) {
            try {
                String str = w.getString();
                String style = w.getResultType().getName();
                doc.insertString(doc.getLength(), str, doc.getStyle(style));
                doc.insertString(doc.getLength(), " ", doc.getStyle(ResultType.NOT_CHANGE.getName()));
            } catch (BadLocationException ex) {
                System.err.println("err:renderFrom:" + ex);
            }
        }
    }

    public void renderTo(StyledDocument doc) {
        for (Word w : toList) {
            try {
                String str = w.getString();
                String style = w.getResultType().getName();
                doc.insertString(doc.getLength(), str, doc.getStyle(style));
                doc.insertString(doc.getLength(), " ", doc.getStyle(ResultType.NOT_CHANGE.getName()));
            } catch (BadLocationException ex) {
                System.err.println("err:renderTo:" + ex);
            }
        }
    }

    class Word {
        private String str;
        private ResultType type;

        public Word(String str) {
            this.str = str;
            this.type = ResultType.NOT_CHANGE;
        }

        public String getString(){
            return str;
        }
        
        public void setAdd() {
            type = ResultType.ADD;
        }

        public void setDel() {
            type = ResultType.DEL;
        }

        public void setChange() {
            type = ResultType.CHANGE;
        }

        public ResultType getResultType(){
            return type;
        }
    }
} 
