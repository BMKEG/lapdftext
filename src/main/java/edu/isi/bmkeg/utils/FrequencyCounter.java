package edu.isi.bmkeg.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FrequencyCounter implements Serializable {

	private Map<Object, Integer> freq = new HashMap<Object, Integer>();

	public void add(FrequencyCounter fc) {
		Iterator<Object> it = this.freq.keySet().iterator();
		while (it.hasNext()) {
			Object o = it.next();
			int c1 = fc.getCount(o);
			int c2 = this.getCount(o);
			this.freq.put(o, new Integer(c1 + c2));
		}
	}

	public void add(Object o) {
		int c = this.getCount(o);
		Integer count = new Integer(c + 1);
		this.freq.put(o, count);
	}

	public Object getMostPopular() {
		int max = 0;
		Object mp = null;
		Iterator<Object> it = this.freq.keySet().iterator();
		while (it.hasNext()) {
			Object MP = it.next();
			int c = getCount(MP);
			if (c > max) {
				mp = MP;
				max = c;
			}
		}

		return mp;
	}

	public Object getNextMostPopular() {
		
		int max = 0;
		
		Object nmp = null;
		
		Object mp = this.getMostPopular();
		int mpCount = this.getCount(mp);
		
		Iterator<Object> it = this.freq.keySet().iterator();
		while (it.hasNext()) {
			Object NMP = it.next();
			int c = getCount(NMP);
			if (c > max && c < mpCount) {
				nmp = NMP;
				max = c;
			}
		}

		return nmp;
	}

	public Object getThirdMostPopular() {
		
		int max = 0;

		Object nnmp = null;

		Object nmp = this.getNextMostPopular();
		int nmpCount = this.getCount(nmp);
		
		Iterator<Object> it = this.freq.keySet().iterator();
		while (it.hasNext()) {
			Object NNMP = it.next();
			int c = getCount(NNMP);
			if (c > max && c < nmpCount) {
				nnmp = NNMP;
				max = c;
			}
		}

		return nnmp;
	}
	
	public int getCount(Object o) {
		int c = 0;
		if (this.freq.containsKey(o)) {
			Integer count = this.freq.get(o);
			c = count.intValue();
		}

		return c;
	}

	public int countOptions() {
		return this.freq.size();
	}

	public void reset() {
		this.freq.clear();

	}
}
