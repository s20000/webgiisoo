package com.giisoo.core.R;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;

import com.giisoo.core.bean.Bean;

public class rtest {

    public static void main(String[] args) throws REXPMismatchException, REngineException {
        RConnection c = new RConnection();
        REXP x = c.eval("R.version.string");

        c.assign("x", new int[10]);
        c.assign("y", new int[10]);

        double[] d = c.eval("rnorm(100)").asDoubles();

        RList l = c.eval("lowess(x,y)").asList();

        System.out.println(Bean.toString(l.toArray()));

        // c.serverShutdown();
        c.close();

    }

}
