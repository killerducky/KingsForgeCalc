package com.olsen.andy.kingsforgecalc;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public class DiceHashList extends HashMap<GameObject.GOColor, List<GameObject>> {
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
}
