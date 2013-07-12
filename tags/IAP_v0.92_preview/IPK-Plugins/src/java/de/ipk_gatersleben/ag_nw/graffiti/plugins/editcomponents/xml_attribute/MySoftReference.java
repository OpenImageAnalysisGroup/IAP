package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.xml_attribute;

import java.lang.ref.SoftReference;

public class MySoftReference<T> extends SoftReference<T> {
	
	private boolean isWeak;
	private T ref;
	
	public MySoftReference(T referent, boolean isWeak) {
		super(referent);
		if (!isWeak)
			ref = referent;
	}
	
	@Override
	public T get() {
		if (isWeak)
			return super.get();
		else
			return ref;
	}
	
}
