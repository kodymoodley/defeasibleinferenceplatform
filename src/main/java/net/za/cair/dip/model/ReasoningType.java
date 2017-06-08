package net.za.cair.dip.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Copyright (C) 2011, Centre for Artificial Intelligence Research
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 */

/**
 * Author: Kody Moodley<br>
 * Centre for Artificial Intelligence Research<br>
 * UKZN and CSIR<br>
 * Date: 10-Oct-2011<br><br>
 */

public class ReasoningType implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8030360798819201554L;
	//public static final ReasoningType PREFERENTIAL = getInstance(0, "Preferential Closure");
	public static final ReasoningType RATIONAL = getInstance(0, "Rational Closure");
	//public static final ReasoningType LEX_RELEVANT = getInstance(4, "Lexically Relevant Closure");
	public static final ReasoningType RELEVANT = getInstance(2, "Basic Relevant Closure");
	public static final ReasoningType MIN_RELEVANT = getInstance(3, "Minimal Relevant Closure");
	public static final ReasoningType LEXICOGRAPHIC = getInstance(1, "Lexicographic Closure");
	//public static final ReasoningType TARSKIAN = getInstance(6, "Tarskian Closure");
	
	public static final ArrayList<ReasoningType> REASONING_TYPES= new ArrayList<ReasoningType>();
	public static final Map<String, ReasoningType> NAME_TYPE_MAP = new HashMap<String, ReasoningType>();
	public static final Map<ReasoningType, String> TYPE_NAME_MAP = new HashMap<ReasoningType, String>();
	public static final String[] REASONING_TYPE_NAMES = new String[4];
	
	public final int index;
	private final String name;

	private ReasoningType(int index, String name){
		this.index = index;
		this.name = name;
	}
	
	static {
		//REASONING_TYPES.add(PREFERENTIAL);
        REASONING_TYPES.add(RATIONAL);
        //REASONING_TYPES.add(LEX_RELEVANT);
       
        REASONING_TYPES.add(RELEVANT);
        REASONING_TYPES.add(MIN_RELEVANT);
        REASONING_TYPES.add(LEXICOGRAPHIC);
        //REASONING_TYPES.add(TARSKIAN);
        
        int count = 0;
        for (ReasoningType type: REASONING_TYPES){
        	REASONING_TYPE_NAMES[count] = new String(type.name);
        	count++;
        	TYPE_NAME_MAP.put(type, type.name);
            NAME_TYPE_MAP.put(type.name, type);
        }
    }
	
	@Override
    public String toString() {
        return name;
    }
	
	public int getIndex() {
        return index;
    }
	
	public String getName() {
        return name;
    }
	
	private static final ReasoningType getInstance(int i, String name){
        return new ReasoningType(i, name);
    }
}
