package de.jeff_media.AngelChest;

import java.util.ArrayList;

class CaseInsensitiveStringList extends ArrayList<String> {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7754120806084460569L;

	@Override
    public boolean contains(Object o) {
        String paramStr = (String)o;
        for (String s : this) {
            if (paramStr.equalsIgnoreCase(s)) return true;
        }
        return false;
    }
}