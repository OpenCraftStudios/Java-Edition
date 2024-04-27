package net.op.data;

import java.util.HashMap;
import java.util.Map;

public class NBT {

    public Map<String, String> properties = new HashMap<>();

    public NBT() {
    }

    public static NBT format(String nbt) {
        NBT objNBT = new NBT();

        int cbrackets = 0;
        int brackets = 0;
        int parenthesis = 0;
        boolean qmarks = false;
        boolean apostrophe = false;

        if (!(nbt.startsWith("[") && nbt.endsWith("]"))) {
            throw new NBTFormatException("Invalid NBT");
        }

        nbt = nbt.substring(1, nbt.length() - 1) + ',';

        char[] charArray = nbt.toCharArray();

        String key = "";
        String value = "";
        boolean keyIterated = false;
        
        for (char c : charArray) {
        	if (c == '"') {
        		qmarks = !qmarks;
        		continue;
        	} else if (c == '\'') {
        		apostrophe = !apostrophe;
        		continue;
        	}
        	
        	if (keyIterated) {
                value += c;
            } else {
                key += c;
            }
        	
        	if (brackets > 0 || cbrackets > 0 || parenthesis > 0 || apostrophe || qmarks) {
                continue;
            }
        	
            switch (c) {
                case '=' -> {
                    if (keyIterated) {
                        throw new NBTFormatException("Cannot have 2 keys!");
                    }
                    keyIterated = true;
                    continue;
                }
                case ',' -> {
                    objNBT.properties.put(key.trim(), value.trim());

                    key = "";
                    value = "";
                    keyIterated = false;
                    continue;
                }
                
                case '(' -> parenthesis++;
                case ')' -> parenthesis--;
                case '[' -> brackets++;
                case ']' -> brackets--;
                case '{' -> cbrackets++;
                case '}' -> cbrackets--;
            }
            
        }

        if (!(cbrackets == 0 && brackets == 0 && parenthesis == 0) || qmarks || apostrophe) {
            throw new NBTFormatException();
        }

        return objNBT;
    }

}
