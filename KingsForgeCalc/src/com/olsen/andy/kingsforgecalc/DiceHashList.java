package com.olsen.andy.kingsforgecalc;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public class DiceHashList extends HashMap<GameObject.GOColor, List<GameObject>> {
    DiceHashList() {
        super();
    }
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            switch (color) {
            case BLACK: str.append('B'); break;
            case GREEN: str.append("_G"); break;
            case RED  : str.append("_R"); break;    
            case BLUE : str.append("_b"); break;
            }
            for (GameObject go : this.get(color)) {
                str.append(go);
            }
        }
        return str.toString();
    }
    
    public String normalString() {
        StringBuilder str = new StringBuilder();
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            if (this.get(color).size() == 0) {
                continue;
            }
            switch (color) {
            case BLACK: str.append("Black "); break;
            case GREEN: str.append("Green "); break;
            case RED  : str.append("Red "); break;    
            case BLUE : str.append("Blue "); break;
            }
            for (GameObject go : this.get(color)) {
                str.append(go);
            }
            if (color != GameObject.GOColor.BLUE) { str.append('\n'); }
        }
        return str.toString();
    }
    
    public String verboseString() {
        StringBuilder str = new StringBuilder();
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            for (GameObject go : this.get(color)) {
                str.append(go.verboseToString() + '\n');
            }
        }
        return str.toString();
    }
}
