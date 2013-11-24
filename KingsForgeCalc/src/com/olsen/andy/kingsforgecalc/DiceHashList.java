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
        String str = "";
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            switch (color) {
            case BLACK: str += "B"; break;
            case GREEN: str += "_G"; break;
            case RED  : str += "_R"; break;    
            case BLUE : str += "_b"; break;
            }
            for (GameObject go : this.get(color)) {
                str += go;
            }
        }
        return str;
    }
    
    public String normalString() {
        String str = "";
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            if (this.get(color).size() == 0) {
                continue;
            }
            switch (color) {
            case BLACK: str += "Black "; break;
            case GREEN: str += "Green "; break;
            case RED  : str += "Red "; break;    
            case BLUE : str += "Blue "; break;
            }
            for (GameObject go : this.get(color)) {
                str += go;
            }
            if (color != GameObject.GOColor.BLUE) { str += "\n"; }
        }
        return str;
    }
    
    public String verboseString() {
        String str = "";
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            for (GameObject go : this.get(color)) {
                str += go.verboseToString() + "\n";
            }
        }
        return str;
    }
}
