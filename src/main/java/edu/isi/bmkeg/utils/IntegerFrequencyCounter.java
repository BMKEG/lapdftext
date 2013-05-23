package edu.isi.bmkeg.utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class IntegerFrequencyCounter implements Serializable
{

    Map<Integer, Integer> freq = new HashMap<Integer, Integer>();
    int kernel;
    int max = 0;
    int mp = 0;
    int nmp = 0;

    public String getDebug()
    {
        Object[] oo = this.freq.keySet().toArray();
        Arrays.sort(oo);
        String debug = "";
        for(int i = 0; i < oo.length; i++)
        {
            Integer ii = (Integer) oo[i];
            int c = getCount(ii);
            debug += ii.intValue() + ":" + c + "\n";
        }
        return debug;
    }


    public IntegerFrequencyCounter(int kernel)
    {
        this.kernel = kernel;
    }


    public int[] getOrderdValues()
    {
        Object[] oo = this.freq.keySet().toArray();
        Arrays.sort(oo);
        int[] ii = new int[oo.length];
        for(int i = 0; i < oo.length; i++)
        {
            ii[i] = ((Integer) oo[i]).intValue();
        }

        return ii;
    }


    public void add(Integer ii)
    {
        int i = ii.intValue();

        max = -1;
        mp = -1;

        for(int j = i - kernel; j <= i + kernel; j++)
        {
            Integer jj = new Integer(j);
            int weight = kernel - Math.abs(i - j) + 1;
            this.incrementCount(jj, weight);
        }
    }


    public int getMostPopular()
    {
        if(mp > 0) {
            return mp;
        }
        int max = 0;
        Iterator<Integer> it = this.freq.keySet().iterator();
        while(it.hasNext())
        {
            Integer MP = (Integer) it.next();
            int c = getCount(MP);
            if(c > max)
            {
                mp = MP.intValue();
                max = c;
            }
        }
        return mp;
    }


    public int getNextMostPopular() {
    	
        if(nmp > 0) {
            return nmp;
        }
        int mp = this.getMostPopular();
        int mpCount = this.getCount(new Integer(mp));

        int max = 0;
        Iterator<Integer> it = this.freq.keySet().iterator();
        while(it.hasNext()) {
            Integer NMP = (Integer) it.next();
            int c = getCount(NMP);
            if(c > max && c < mpCount)
            {
                nmp = NMP.intValue();
                max = c;
            }
        }
        return nmp;
    }


    public void incrementCount(Integer o, int d)
    {
        int c = this.getCount(o);
        Integer count = new Integer(c + d);
        this.freq.put(o, count);
    }


    public int getCount(Object o)
    {
        int c = 0;
        if(this.freq.containsKey(o))
        {
            Integer count = (Integer) this.freq.get(o);
            c = count.intValue();
        }
        return c;
    }


    public int getMax()
    {
        Object[] oo = this.freq.keySet().toArray();
        Arrays.sort(oo);
        max = ((Integer) oo[oo.length - 1]).intValue();
        return max;
    }


    public int getMin()
    {
        Object[] oo = this.freq.keySet().toArray();
        Arrays.sort(oo);
        max = ((Integer) oo[0]).intValue();
        return max;
    }


    public int sumKeys()
    {
        int c = 0;
        Iterator<Integer> it = this.freq.keySet().iterator();
        while(it.hasNext())
        {
            Integer cc = (Integer) it.next();
            c += cc.intValue();
        }
        return c;
    }

    public void reset(){
    	this.freq.clear();
    	this.max=0;
    	this.mp=0;
    	this.nmp=0;
    }

}
