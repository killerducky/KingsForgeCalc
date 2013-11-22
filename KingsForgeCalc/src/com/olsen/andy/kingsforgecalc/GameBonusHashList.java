package com.olsen.andy.kingsforgecalc;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

//public class GameBonusHashList extends HashMap<GameBonus.Bonus, List<GameBonus>> implements Iterable<GameBonus> {
public class GameBonusHashList implements Iterable<GameBonus> {

    private HashMap<GameBonus.Bonus, List<GameBonus>> hashList;

    public GameBonusHashList() {
        hashList = new HashMap<GameBonus.Bonus, List<GameBonus>>();
    }
    
    public List<GameBonus> get(GameBonus.Bonus bonusType) { return hashList.get(bonusType); }
    public void put(GameBonus.Bonus bonusType, List<GameBonus> gb) { hashList.put(bonusType, gb); }
    
    @Override
    public ListIterator<GameBonus> iterator() {
        ListIterator<GameBonus> it = new MyIter();
        return it;
    }

    class MyIter implements ListIterator<GameBonus> {
        private ListIterator<GameBonus> a6Iter;
        private ListIterator<GameBonus> wdIter;
        private ListIterator<GameBonus> p2Iter;
        private ListIterator<GameBonus> currIter;
        
        public MyIter() {
            List<GameBonus> a6List = hashList.get(GameBonus.Bonus.A6);
            a6Iter = a6List.listIterator();
            a6Iter = hashList.get(GameBonus.Bonus.A6).listIterator();
            wdIter = hashList.get(GameBonus.Bonus.WD).listIterator();
            p2Iter = hashList.get(GameBonus.Bonus.P2).listIterator();
            currIter = a6Iter;
        }
        
        public boolean hasNext() {
            if (currIter == a6Iter) {
                return a6Iter.hasNext() || wdIter.hasNext() || p2Iter.hasNext();
            } else if (currIter == wdIter) {
                return wdIter.hasNext() || p2Iter.hasNext();
            } else if (currIter == p2Iter) {
                return p2Iter.hasNext();
            }
            return false;
        }
        public GameBonus next() {
            if (currIter == a6Iter) {
                if (a6Iter.hasNext()) { 
                    return a6Iter.next(); 
                } else {
                    currIter = wdIter;
                }
            }
            if (currIter == wdIter) {
                if (wdIter.hasNext()) { 
                    return wdIter.next(); 
                } else {
                    currIter = p2Iter;
                }
            }
            assert (currIter == p2Iter);
            return p2Iter.next(); 
        }
        public boolean hasPrevious() {
            if (currIter == p2Iter) {
                return p2Iter.hasPrevious() || wdIter.hasPrevious() || a6Iter.hasPrevious();
            } else if (currIter == wdIter) {
                return wdIter.hasPrevious() || a6Iter.hasPrevious();
            } else if (currIter == a6Iter) {
                return a6Iter.hasPrevious();
            }
            return false;
        }
        public GameBonus previous() {
            if (currIter == p2Iter) {
                if (p2Iter.hasPrevious()) { 
                    return p2Iter.previous(); 
                } else {
                    currIter = wdIter;
                }
            }
            if (currIter == wdIter) {
                if (wdIter.hasPrevious()) { 
                    return wdIter.previous(); 
                } else {
                    currIter = a6Iter;
                }
            }
            assert (currIter == a6Iter);
            return a6Iter.next(); 

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
