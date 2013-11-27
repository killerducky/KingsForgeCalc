package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import com.olsen.andy.kingsforgecalc.DiceHashList.Builder;

@SuppressWarnings("serial")
public class GameBonusHashList extends HashMap<GameBonus.Bonus, List<GameBonus>> implements Iterable<GameBonus> {
    public GameBonusHashList() {
        super();
    }
    public static class Builder {
        private GameBonusHashList bonusHashList;

        public Builder() {
            bonusHashList = new GameBonusHashList();
            for (GameBonus.Bonus bonusType : GameBonus.Bonus.values()) {
                bonusHashList.put(bonusType, new ArrayList<GameBonus>());
            }
        }
        public GameBonusHashList build () {
            return bonusHashList;
        }
    }

    @Override
    public ListIterator<GameBonus> iterator() {
        ListIterator<GameBonus> it = new MyIter();
        return it;
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (GameBonus gb : this) {
            str.append(" " + gb);
        }
        return str.toString();
    }

    class MyIter implements ListIterator<GameBonus> {
        private List<ListIterator<GameBonus>> iters;
        private Integer currIterIndex;
        
        public MyIter() {
            iters = new ArrayList<ListIterator<GameBonus>>();
            iters.add(get(GameBonus.Bonus.A1TO6).listIterator());
            iters.add(get(GameBonus.Bonus.WD).listIterator());
            iters.add(get(GameBonus.Bonus.A6).listIterator());
            iters.add(get(GameBonus.Bonus.P2).listIterator());
            iters.add(get(GameBonus.Bonus.P1X3).listIterator());
            iters.add(get(GameBonus.Bonus.P1).listIterator());
            iters.add(get(GameBonus.Bonus.RR).listIterator());
            currIterIndex = 0;
        }
        
        public boolean hasNext() {
            for (int i=currIterIndex; i < iters.size(); i++) {
                if (iters.get(i).hasNext()) { return true; }
            }
            return false;
        }
        public GameBonus next() {
            for (; currIterIndex < iters.size(); currIterIndex++) {
                if (iters.get(currIterIndex).hasNext()) { 
                    return iters.get(currIterIndex).next();
                }
            }
            throw new NoSuchElementException();
        }
        public boolean hasPrevious() {
            for (int i=currIterIndex; i>=0; i--) {
                if (iters.get(i).hasPrevious()) { return true; }
            }
            return false;
        }
        public GameBonus previous() {
            for (; currIterIndex >= 0; currIterIndex--) {
                if (iters.get(currIterIndex).hasPrevious()) { 
                    return iters.get(currIterIndex).previous(); 
                }
            }
            throw new NoSuchElementException();
        }
        public void set(GameBonus gb) {
            throw new UnsupportedOperationException();
        }
        public void add(GameBonus gb) {
            throw new UnsupportedOperationException();
        }
        public int previousIndex() {
            throw new UnsupportedOperationException();
        }
        public int nextIndex() {
            throw new UnsupportedOperationException();
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
        public void set() {
            throw new UnsupportedOperationException();
        }
    }
}
