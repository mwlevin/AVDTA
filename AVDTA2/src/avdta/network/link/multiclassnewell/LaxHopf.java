/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.multiclassnewell;


import avdta.network.link.MulticlassLTMLink;
import ilog.concert.IloAddable;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author mlevin
 */
public class LaxHopf 
{
    public static final double tau_hv=1.5;
    public static final double tau_av=0.25;
    public static final double v = 30;
    public static final double K = 240;
    public static final double veh_len = 5280.0/K;
    public static final double L = 1;
    
    public static boolean PRINT = false;
    
    private MulticlassLTMLink link;
    
    private List<InitialCondition> initial;
    private List<? extends BoundaryCondition> upstream;
    private List<? extends BoundaryCondition> downstream;
    private List<Region> regions;

    
    public LaxHopf(MulticlassLTMLink link, List<BoundaryCondition> uss, List<BoundaryCondition> dss, List<Region> rss)
    {
        this.link = link;
        initial = new ArrayList<>();
        upstream = uss;
        downstream = dss;
        regions = rss;
    }
    
    
    public double traceUncongestedCharacteristic(double t, double x)
    {
        // tp where xp = 0
        double tp = (v*t-x)/v;
        
        // xp where tp = 0
        double xp = x + v*(0-t);
        
        if(tp >= 0)
        {
            for(BoundaryCondition c : upstream)
            {
                if(c.isDefined(tp))
                {
                    return c.getC(tp);
                }
            }
        }
        else
        {
            for(InitialCondition c : initial)
            {
                if(c.isDefined(xp))
                {
                    return c.getC(xp);
                }
            }
        }
        
        return Integer.MAX_VALUE;
    }
    
    
    /**
     * Calculates N(t,x) for a specified grid of points and returns the output as a matrix.
     * @param t_start the smallest t value
     * @param t_end the largest t value
     * @param t_inc the t increment
     * @param x_start the smallest x value
     * @param x_end the largest x value
     * @param x_inc the x increment
     * @return a matrix containing the cumulative count values at each (t,x) coordinate. 
     */
    public double[][] calculateN(double t_start, double t_end, double t_inc, double x_start, double x_end, double x_inc) throws IloException
    {
        double[][] output = new double[(int)Math.ceil((t_end - t_start)/t_inc+1)][(int)Math.ceil((x_end - x_start)/x_inc+1)];
        
        int count = 0;
        
        int total_points = output.length * output[0].length;
        
        int diff = (int)Math.min(500, total_points/100);
        
        for(int r = 0; r < output.length; r++)
        {
            for(int c = 0; c < output[r].length; c++)
            {
                double t = t_start + r * t_inc;
                double x = x_start + c * x_inc;
                
                output[r][c] = calculateN(t, x);
                
                count++;
                
                if(count % diff == 0)
                {
                    System.out.println(count+" / "+total_points+" points ("+String.format("%.1f", 100.0*count/total_points)+"%)");
                }
            }
        }
        
        return output;
    }


    
    /**
     * Calculates k(t,x) for a specified grid of points and returns the output as a matrix.
     * @param t_start the smallest t value
     * @param t_end the largest t value
     * @param t_inc the t increment
     * @param x_start the smallest x value
     * @param x_end the largest x value
     * @param x_inc the x increment
     * @return a matrix containing the cumulative count values at each (t,x) coordinate. 
     */
    public double[][] calculateK(double t_start, double t_end, double t_inc, double x_start, double x_end, double x_inc) throws IloException
    {
        double[][] output = new double[(int)Math.ceil((t_end - t_start)/t_inc+1)][(int)Math.ceil((x_end - x_start)/x_inc+1)];
        
        int count = 0;
        
        int total_points = output.length * output[0].length;
        
        int diff = (int)Math.min(500, total_points/100);
        
        for(int r = 0; r < output.length; r++)
        {
            for(int c = 0; c < output[r].length; c++)
            {
                double t = t_start + r * t_inc;
                double x = x_start + c * x_inc;
                
                
                
                output[r][c] = Math.max(0, calculateK(t, x));
                
                
                count++;
                
                if(count % diff == 0)
                {
                    System.out.println(count+" / "+total_points+" points ("+String.format("%.1f", 100.0*count/total_points)+"%)");
                }
            }
        }
        
        return output;
    }
    
    /**
     * Calculates the density at a specific (t,x) point
     * @param t time (s)
     * @param x space (m)
     * @return k(t,x)
     */
    public double calculateK(double t, double x) throws IloException
    {
        double epsilon = 0.01;
        
        return (calculateN(t, x) - calculateN(t, x+epsilon))/(epsilon);
    }
    
    public double calculateN_up_Newell(double t)
    {
        double w = regions.get(0).getW(link);
        
        double T = L/w;
        
        System.out.println("T="+T+" K="+K+" L="+L);
        System.out.println("t="+t);
        
        return calculateDownstreamCondN(t-T)+K*L;
    }
    
    public double calculateDownstreamCondN(double t)
    {
        for(BoundaryCondition cond : downstream)
        {
            if(cond.isDefined(t))
            {
                return cond.getC(t);
            }
        }
        return Integer.MAX_VALUE;
    }

    
    /**
     * Use a system of linear equations to calculate N(t, 0). This assumes that we are in region I+1 (the last region).
     * Also assumes that there are no initial boundary conditions.
     * Have not added capacity constraint yet.
     * @param t
     * @param x
     * @return 
     */
    public double calculateN_up(double t, int I)
    {
        double output = Integer.MAX_VALUE;
        
        if(PRINT) 
        {
            System.out.println("Capacity: "+regions.get(I+1-1).getCapacity(link) * 30.0/3600.0);
        }
        
        outer: for(BoundaryCondition cond : downstream)
        {
         
            int i_start = 1;

            double[] ts = new double[I+1];
            double[] xs = new double[I+1];

            while(true)
            {
                if(PRINT)
                {
                    System.out.println("i_start = "+i_start+", cond = "+cond+", output = "+output+", I = "+I);
                }
                if(i_start == I+1)
                {
                    ts[0] = t-L/regions.get(I+1-1).getW(link);
                    if(cond.isDefined(ts[0]))
                    {
                        output = Math.min(output, cond.getC(ts[0]) + K*L);
                    }
                    
                    continue outer;
                }
                else if(i_start == I)
                {
                   if(i_start == 1)
                   {
                       double c0 = cond.getInitialC();
                       double w2 = regions.get(I+1-1).getW(link);
                       double w1 = regions.get(I-1).getW(link);
                       double q0 = cond.getFlow();
                       double b1 = regions.get(I-1).getUpperB();
                       
                       double A = -L + w2*t - w1*(b1-c0)/(q0-K*w1);
                       double B = w2 - w1 - w1*w1*K / (q0-K*w1);
                       ts[1] = A/B;
                       ts[0] = (b1 - c0 - L*w1*ts[1]) / (q0-K*w1);
                       xs[1] = w2*(t-ts[1]);
                               
                       output = Math.min(output, regions.get(1-1).getUpperB() + K*xs[1]);
                       
                       if(PRINT)
                       {
                            System.out.println("check "+(regions.get(1-1).getUpperB() + K*xs[1]));
                       }
                   }
                   
                    double tprime = findUpstreamTime(regions.get(i_start-1).getUpperB());
                    double wI1 = regions.get(I+1-1).getW(link);

                    // ts[I], xs[I] only variables
                    ts[I] = (v*tprime + wI1*t) / (wI1 + v);
                    xs[I] = v*(ts[I] - tprime);

                    output = Math.min(output, regions.get(I-1).getUpperB() + K*xs[I]);
                    
                    if(PRINT)
                    {
                        System.out.println("t'="+tprime+" "+t);
                        System.out.println("xs[I]="+xs[I]);
                        System.out.println("check "+(regions.get(I-1).getUpperB() + K*xs[I]));
                    }
                    continue outer;
                }
                else
                {
                    double bI = regions.get(I-1).getUpperB();
                    double c0 = cond.getInitialC();
                    double wI1 = regions.get(I+1-1).getW(link);
                    double w1 = regions.get(1-1).getW(link);
                    double q0 = cond.getFlow();
                    double b1 = regions.get(1-1).getUpperB();
                    
                    if(i_start == 1)
                    {
                        double A = (bI-c0-K*L+K*wI1*t) / (K*wI1);
                        double B = (c0-b1)/(K*w1);
                        double C = 0;
                        
                        for(int i = 2; i <= I; i++)
                        {
                            C += (regions.get(i-1).getUpperB() - regions.get(i-1 -1).getUpperB()) / (K * regions.get(i-1).getW(link));
                        }
                        
                        double D = q0 / (K*wI1) + 1 - q0/(K*w1);
                        
                        ts[0] = (A+B+C)/D;
                        ts[I] = ts[0] + (b1 - c0-q0*ts[0])/(K*w1) + C;
                        xs[I] = (bI-c0-q0*ts[0]-K*L) / -K;
                        xs[0] = L;
                        
                        xs[1] = (b1-c0-q0*ts[0]-K*L)/ -K;
                        ts[1] = ts[0] + (xs[1] - L)/-w1;
                        
                        for(int i = 1; i < I; i++)
                        {
                            ts[i+1] = ts[i] + (regions.get(i+1-1).getUpperB() - regions.get(i-1).getUpperB()) 
                                / (K * regions.get(i+1-1).getW(link));
                            xs[i+1] = (K*xs[i] - regions.get(i+1-1).getUpperB() + regions.get(i-1).getUpperB() )/K;
                        }
                        
                        if(PRINT)
                        {
                            System.out.println("t0="+ts[0]);
                        }
                        
                        // downstream boundary condition not defined for the t interval
                        if(!cond.isDefined(ts[0]))
                        {
                            continue outer;
                        }
                    }
                    else
                    {
                        double tprime = findUpstreamTime(regions.get(i_start-1).getUpperB());
                        double A = (regions.get(I-1).getUpperB() - regions.get(i_start-1).getUpperB())/K;
                        double B = v*tprime + regions.get(I+1-1).getW(link)*t;
                        double C = 0;
                        
                        for(int i = i_start+1; i <= I; i++)
                        {
                            C += (regions.get(i-1).getUpperB() - regions.get(i-1 -1).getUpperB()) / (K * regions.get(i-1).getW(link));
                        }
                        double D = v+regions.get(I+1-1).getW(link);
                        
                        ts[i_start] = (A + B + regions.get(I+1-1).getW(link)*C) / D;
                        ts[I] = ts[i_start] + C;
                        xs[I] = regions.get(I+1-1).getW(link) * (t-ts[I]);
                        xs[i_start] = (regions.get(I-1).getUpperB() - regions.get(i_start-1).getUpperB() + K*xs[I])/K;
                        
                        for(int i = 1; i < I; i++)
                        {
                            ts[i+1] = ts[i] + (regions.get(i+1-1).getUpperB() - regions.get(i-1).getUpperB()) 
                                / (K * regions.get(i+1-1).getW(link));
                            xs[i+1] = (K*xs[i] - regions.get(i+1-1).getUpperB() + regions.get(i-1).getUpperB() )/K;
                        }
                    }
                    
                    output = Math.min(output, K*xs[I] + regions.get(I-1).getUpperB());
                    
                    if(PRINT)
                    {
                        System.out.println("check "+(K*xs[I] + regions.get(I-1).getUpperB()));
                    }
                    
                    // update i_start
                    boolean cont = false;
                    for(int i = i_start+1; i <= I; i++)
                    {
                        if(traceUncongestedCharacteristic(ts[i], xs[i]) < regions.get(i-1).getUpperB())
                        {
                            i_start = i;
                            cont = true;
                        }
                    }
                    if(!cont)
                    {
                        continue outer;
                    }
                }
                
            }
            
        }
        
        return output;
    }
    
    public double findUpstreamTime(double c)
    {
        double output = -1;
        for(BoundaryCondition cond : upstream)
        {
            output = cond.calcT(c);
            if(output >= 0)
            {
                return output;
            }
        }
        return output;
    }
    
    public double calculateN(double t, double x) throws IloException
    {
        double epsilon = 0.0001;
        
        double traceUncongested = traceUncongestedCharacteristic(t, x);
        double Ntx = traceUncongested;
        
        int I = regions.size();
        
        if(PRINT)
        {
            System.out.println("\tUncongested: N(t,x) = "+traceUncongested);
        }
        
        for(BoundaryCondition d : downstream)
        {
            IloCplex cplex = new IloCplex();
            
            //if(!PRINT)
            {
                cplex.setParam(IloCplex.IntParam.MIPDisplay, 0);
                cplex.setOut(null);
            }

            IloNumVar[] ti = new IloNumVar[I]; // t_0 ... t_{I}
            IloNumVar[] xi = new IloNumVar[I]; // x_0 ... x_{I}
            IloNumVar[] Ti = new IloNumVar[I+1]; // T_1 ... T_{I}. T_0 ignored
            IloNumVar[] lambda = new IloNumVar[I];
            
            for(int i = 0; i < ti.length; i++)
            {
                ti[i] = cplex.numVar(0, Integer.MAX_VALUE);
                xi[i] = cplex.numVar(0, Integer.MAX_VALUE);
                lambda[i] = cplex.numVar(0, Integer.MAX_VALUE);
            }
            
            for(int i = 1; i < Ti.length; i++)
            {
                Ti[i] = cplex.numVar(0, Integer.MAX_VALUE);
            }
            
            // t_i = t - sum j=i+1 to I of Tj
            for(int i = 0; i < ti.length; i++)
            {
                IloLinearNumExpr rhs = cplex.linearNumExpr();
                
                for(int j = i+1; j <= I; j++)
                {
                    rhs.addTerm(-1, Ti[j]);
                }
                cplex.addEq(ti[i], cplex.sum(t, rhs));
            }

            
            // x_i = x + sum j=i+1 to I of Tj wj
            for(int i = 0; i < xi.length; i++)
            {
                IloLinearNumExpr rhs = cplex.linearNumExpr();
                for(int j = i+1; j <= I; j++)
                {
                    rhs.addTerm(regions.get(j-1).getW(link), Ti[j]);
                }
                cplex.addEq(xi[i], cplex.sum(x, rhs));
            }

            
            // x_0 = L
            cplex.addEq(xi[0], L);
            
            // t_0 within the range of the condition
            cplex.addLe(ti[0], d.getMaxT());
            cplex.addGe(ti[0], d.getMinT());
            
            
            
            // if t_hat_i >= 0
            // t_i >= t_hat_i + x_i/v
            // else if x_hat_i \in [0,L]
            // t_i >= (x_i - x_hat_i)/v
            
            for(int i = 1; i <= I-1; i++)
            {
                double t_hat_i = -1;
                double x_hat_i = -1;
                
                double b_i = regions.get(i-1).getUpperB();

                for(InitialCondition ic : initial)
                {
                    x_hat_i = Math.max(x_hat_i, ic.calcX(b_i));
                }
                
                for(BoundaryCondition uc : upstream)
                {
                    t_hat_i = Math.max(t_hat_i, uc.calcT(b_i));
                }

                if(t_hat_i >= 0)
                {
                    cplex.addGe(cplex.sum(ti[i], cplex.prod(-1, cplex.sum(t_hat_i, cplex.prod(xi[i], -1.0/v)))), cplex.prod(-1, lambda[i]));
                }
                else if(x_hat_i >= 0 && x_hat_i <= L)
                {
                    cplex.addGe(cplex.sum(ti[i], cplex.prod(-1, cplex.sum(cplex.prod(xi[i], 1.0/v), -x_hat_i/v))), cplex.prod(-1, lambda[i]));
                }
            }
            
            
            
            
            // N(t,x) <= b_{I-1} + K(x_{I-1} - x)
            if(I > 1)
            {
                for(int i = 1; i <= I-1; i++)
                {
                    
                    cplex.addGe(cplex.sum(cplex.sum(cplex.sum(d.getInitialC(), cplex.prod(d.getFlow(), cplex.sum(ti[0], -d.getMinT()))), 
                            cplex.sum(cplex.prod(K, xi[0]), cplex.prod(-K, xi[i]))),
                            -regions.get(i-1).getUpperB()), cplex.prod(-1, lambda[i]));
                    
                }
                
                
            }


            // N(t,x) <= c(t_0, x_0) + K(x_0 - x)
            
            
            
            IloLinearNumExpr obj = cplex.linearNumExpr();

            double beta = 1;
            
            for(int i = 1; i <= I-1; i++)
            {
                obj.addTerm(1, ti[i]);
                obj.addTerm(beta, lambda[i]);
            }
            
            cplex.addMinimize(obj);
            
            
            boolean solved = cplex.solve();
            
            if(solved)
            {
                double bound = d.getC(cplex.getValue(ti[0])) + K*(cplex.getValue(xi[0])-x);
                
                if(PRINT)
                {
                    System.out.println("\t\tN(t_0, x_0) = N("+(cplex.getValue(ti[0])*3600)+", "+cplex.getValue(xi[0])+") = "+d.getC(cplex.getValue(ti[0]))+" | "+bound);
                }
                
                boolean allActive = true;
                
                boolean Kchecked = false;
                
                for(int i = I-1; i >= 1; i--)
                {
                    boolean active = false;
                    
                    if(L -cplex.getValue(xi[i]) > epsilon && cplex.getValue(lambda[i]) < epsilon)
                    {
                        double t_hat_i = -1;
                        double x_hat_i = -1;

                        double b_i = regions.get(i-1).getUpperB();

                        for(InitialCondition ic : initial)
                        {
                            x_hat_i = Math.max(x_hat_i, ic.calcX(b_i));
                        }

                        for(BoundaryCondition uc : upstream)
                        {
                            t_hat_i = Math.max(t_hat_i, uc.calcT(b_i));
                        }

                        if(t_hat_i >= 0)
                        {
                            //System.out.println("* "+(cplex.getValue(ti[i]) - t_hat_i - cplex.getValue(xi[i])/v));
                            if(cplex.getValue(ti[i]) - t_hat_i - cplex.getValue(xi[i])/v <= epsilon)
                            {
                                active = true;
                            }
   
                        }
                        else if(x_hat_i >= 0 && x_hat_i <= L)
                        {
                            //System.out.println("** "+(cplex.getValue(ti[i]) - (cplex.getValue(xi[i])-x)/v));
                            if(cplex.getValue(ti[i]) - (cplex.getValue(xi[i])-x)/v <= epsilon)
                            {
                                active = true;
                            }
                        }
                        if(d.getC(cplex.getValue(ti[0])) + K*(cplex.getValue(xi[0]) - cplex.getValue(xi[1])) - regions.get(i-1).getUpperB() <= epsilon)
                        {
                            active = true;
                        }
                        //System.out.println("* "+(d.getC(cplex.getValue(ti[0])) + K*(cplex.getValue(xi[0]) - cplex.getValue(xi[1]))- regions.get(i-1).getUpperB()));
                        
                        if(active && !Kchecked)
                        {
                            double bound2 = regions.get(i-1).getUpperB() + K*(cplex.getValue(xi[i])-x);
                            bound = Math.min(bound, bound2);
                            Kchecked = true;
                        }
                        
                        if(PRINT)
                        {
                            PRINT = false;
                            System.out.println("\t\tN("+(3600*cplex.getValue(ti[i]))+", "+cplex.getValue(xi[i])+") = "+calculateN(cplex.getValue(ti[i]), cplex.getValue(xi[i]), 1)+"\t"+
                                    cplex.getValue(lambda[i])+"\t"+active);
                            PRINT = true;
                        }
                        
                        allActive = active && allActive;
                    }
                    
                    if(cplex.getValue(lambda[i]) > epsilon)
                    {
                        
                        for(int j = i+1; j <= I; j++)
                        {
                            if(cplex.getValue(Ti[j]) > epsilon)
                            {
                                allActive = false;
                            }
                        }
                        
                        //System.out.println("\t\t* "+cplex.getValue(Ti[1])+"\t"+cplex.getValue(Ti[2])+"\t"+cplex.getValue(lambda[1])+"\t"+allActive);
   
                    }
                    
                    
                }
                
                if(allActive)
                {
                    Ntx = Math.min(Ntx, bound);
                }
                
                if(PRINT)
                {
                    System.out.println("\t"+d+"\t N(t,x) = "+bound);
                }
            }
            else
            {
                if(PRINT)
                {
                    System.out.println("\t"+d+"\tinfeasible");
                }
            }

            cplex.end();
        }
        
        
        
        for(InitialCondition cond : initial)
        {
            IloCplex cplex = new IloCplex();
            
            //if(!PRINT)
            {
                cplex.setParam(IloCplex.IntParam.MIPDisplay, 0);
                cplex.setOut(null);
            }

            IloNumVar[] ti = new IloNumVar[I]; // t_0 ... t_{I}
            IloNumVar[] xi = new IloNumVar[I]; // x_0 ... x_{I}
            IloNumVar[] Ti = new IloNumVar[I+1]; // T_1 ... T_{I}. T_0 ignored
            IloNumVar[] lambda = new IloNumVar[I];
            
            for(int i = 0; i < ti.length; i++)
            {
                ti[i] = cplex.numVar(0, Integer.MAX_VALUE);
                xi[i] = cplex.numVar(0, Integer.MAX_VALUE);
                lambda[i] = cplex.numVar(0, Integer.MAX_VALUE);
            }
            
            for(int i = 1; i < Ti.length; i++)
            {
                Ti[i] = cplex.numVar(0, Integer.MAX_VALUE);
            }
            
            // t_i = t - sum j=i+1 to I of Tj
            for(int i = 0; i < ti.length; i++)
            {
                IloLinearNumExpr rhs = cplex.linearNumExpr();
                
                for(int j = i+1; j <= I; j++)
                {
                    rhs.addTerm(-1, Ti[j]);
                }
                cplex.addEq(ti[i], cplex.sum(t, rhs));
            }

            
            // x_i = x + sum j=i+1 to I of Tj wj
            for(int i = 0; i < xi.length; i++)
            {
                IloLinearNumExpr rhs = cplex.linearNumExpr();
                for(int j = i+1; j <= I; j++)
                {
                    rhs.addTerm(regions.get(j-1).getW(link), Ti[j]);
                }
                cplex.addEq(xi[i], cplex.sum(x, rhs));
            }

            
            // x_0 = L
            cplex.addEq(ti[0], 0);
            
            // t_0 within the range of the condition
            cplex.addLe(xi[0], cond.getMaxX());
            cplex.addGe(xi[0], cond.getMinX());
            
            
            
            // if t_hat_i >= 0
            // t_i >= t_hat_i + x_i/v
            // else if x_hat_i \in [0,L]
            // t_i >= (x_i - x_hat_i)/v
            
            for(int i = 1; i <= I-1; i++)
            {
                double t_hat_i = -1;
                double x_hat_i = -1;
                
                double b_i = regions.get(i-1).getUpperB();

                for(InitialCondition ic : initial)
                {
                    x_hat_i = Math.max(x_hat_i, ic.calcX(b_i));
                }
                
                for(BoundaryCondition uc : upstream)
                {
                    t_hat_i = Math.max(t_hat_i, uc.calcT(b_i));
                }

                if(t_hat_i >= 0)
                {
                    cplex.addGe(cplex.sum(ti[i], cplex.prod(-1, cplex.sum(t_hat_i, cplex.prod(xi[i], 1.0/v)))), cplex.prod(-1, lambda[i]));
                }
                else if(x_hat_i >= 0 && x_hat_i <= L)
                {
                    cplex.addGe(cplex.sum(ti[i], cplex.prod(-1, cplex.sum(cplex.prod(xi[i], 1.0/v), -x_hat_i/v))), cplex.prod(-1, lambda[i]));
                }
            }
            
            
            
            
            // N(t,x) <= b_{I-1} + K(x_{I-1} - x)
            if(I > 1)
            {
                for(int i = 1; i <= I-1; i++)
                {
                    cplex.addGe(cplex.sum(cplex.sum(cplex.sum(cond.getInitialC(), cplex.prod(-cond.getDensity(), cplex.sum(xi[0], -cond.getMaxX()))), 
                            cplex.sum(cplex.prod(K, xi[0]), cplex.prod(-K, xi[i]))),
                            -regions.get(i-1).getUpperB()), cplex.prod(-1, lambda[i]));
                }
                
                
            }


            // N(t,x) <= c(t_0, x_0) + K(x_0 - x)
            
            
            
            IloLinearNumExpr obj = cplex.linearNumExpr();

            double beta = 100;
            
            for(int i = 1; i <= I-1; i++)
            {
                obj.addTerm(1, ti[i]);
                obj.addTerm(beta, lambda[i]);
            }
            
            cplex.addMinimize(obj);
            
            
            boolean solved = cplex.solve();
            
            if(solved)
            {
                double bound = cond.getC(cplex.getValue(xi[0])) + K*(cplex.getValue(xi[0])-x);
                
                if(PRINT)
                {
                    System.out.println("\t\tN(t_0, x_0) = N("+(cplex.getValue(ti[0])*3600)+", "+cplex.getValue(xi[0])+") = "+cond.getC(cplex.getValue(xi[0]))+" | "+bound);
                }
                
                boolean allActive = true;
                
                boolean Kchecked = false;
                
                for(int i = I-1; i >= 1; i--)
                {
                    boolean active = false;
                    
                    if(L -cplex.getValue(xi[i]) > epsilon && cplex.getValue(lambda[i]) < epsilon)
                    {
                        double t_hat_i = -1;
                        double x_hat_i = -1;

                        double b_i = regions.get(i-1).getUpperB();

                        for(InitialCondition ic : initial)
                        {
                            x_hat_i = Math.max(x_hat_i, ic.calcX(b_i));
                        }

                        for(BoundaryCondition uc : upstream)
                        {
                            t_hat_i = Math.max(t_hat_i, uc.calcT(b_i));
                        }

                        if(t_hat_i >= 0)
                        {
                            //System.out.println("* "+(cplex.getValue(ti[i]) - t_hat_i - cplex.getValue(xi[i])/v));
                            if(cplex.getValue(ti[i]) - t_hat_i - cplex.getValue(xi[i])/v <= epsilon)
                            {
                                active = true;
                            }
   
                        }
                        else if(x_hat_i >= 0 && x_hat_i <= L)
                        {
                            //System.out.println("** "+(cplex.getValue(ti[i]) - (cplex.getValue(xi[i])-x)/v));
                            if(cplex.getValue(ti[i]) - (cplex.getValue(xi[i])-x)/v <= epsilon)
                            {
                                active = true;
                            }
                        }
                        if(cond.getC(cplex.getValue(xi[0])) + K*(cplex.getValue(xi[0]) - cplex.getValue(xi[1])) - regions.get(i-1).getUpperB() <= epsilon)
                        {
                            active = true;
                        }
                        //System.out.println("* "+(d.getC(cplex.getValue(ti[0])) + K*(cplex.getValue(xi[0]) - cplex.getValue(xi[1]))- regions.get(i-1).getUpperB()));
                        
                        if(active && !Kchecked)
                        {
                            double bound2 = regions.get(i-1).getUpperB() + K*(cplex.getValue(xi[i])-x);
                            bound = Math.min(bound, bound2);
                            Kchecked = true;
                        }
                        
                        if(PRINT)
                        {
                            PRINT = false;
                            System.out.println("\t\tN("+(3600*cplex.getValue(ti[i]))+", "+cplex.getValue(xi[i])+") = "+calculateN(cplex.getValue(ti[i]), cplex.getValue(xi[i]), 1)+"\t"+
                                    cplex.getValue(lambda[i])+"\t"+active);
                            PRINT = true;
                        }
                        
                        allActive = active && allActive;
                    }
                    
                    if(cplex.getValue(lambda[i]) > epsilon)
                    {
                        
                        for(int j = i+1; j <= I; j++)
                        {
                            if(cplex.getValue(Ti[j]) > epsilon)
                            {
                                allActive = false;
                            }
                        }
                    }
                    
                    
                }
                
                if(allActive)
                {
                    Ntx = Math.min(Ntx, bound);
                }
                
                if(PRINT)
                {
                    System.out.println("\t"+cond+"\t N(t,x) = "+bound);
                }
            }
            else
            {
                if(PRINT)
                {
                    System.out.println("\t"+cond+"\tinfeasible");
                }
            }

            cplex.end();
        }
        

        
        return Ntx;
    }
    
    public double calculateUpstreamCondN(double t)
    {
        for(BoundaryCondition cond : upstream)
        {
            if(cond.isDefined(t))
            {
                return cond.getC(t);
            }
        }
        return 0;
    }
    
    /**
     * Calculates the cumulative count at a specific (t,x) point
     * @param t time (s)
     * @param x space (m)
     * @return N(t,x)
     */
    public double calculateN2(double t, double x) throws IloException
    {
        double Ntx = 0;
        
        for(int i = 0; i < regions.size(); i++)
        {
            Ntx = calculateN(t, x, i+1);
            
            //System.out.println("N(t,x) with "+i+" regions: "+Ntx);
            
            if(Ntx <= regions.get(i).getUpperB())
            //if(true)
            {
                break;
            }
        }
        
        return Ntx;
    }
    
    // use only the first I regions
    public double calculateN(double t, double x, int I) throws IloException
    {
        double output = Integer.MAX_VALUE;
        
        // downstream boundary conditions
        
        double traceUncongested = traceUncongestedCharacteristic(t, x);
        
        if(PRINT)
        //if(I==2)
        {
            System.out.println("Uncongested: N(t,x) = "+traceUncongested);
        }
        
        Object best = null;
        
        for(BoundaryCondition d : downstream)
        {
            IloCplex cplex = new IloCplex();
            
            //if(!PRINT)
            {
                cplex.setParam(IloCplex.IntParam.MIPDisplay, 0);
                cplex.setOut(null);
            }

            IloNumVar Ntx = cplex.numVar(0, Integer.MAX_VALUE);
            cplex.addLe(Ntx, traceUncongested);

            IloNumVar[] ti = new IloNumVar[I]; // t_0 ... t_{I}
            IloNumVar[] xi = new IloNumVar[I]; // x_0 ... x_{I}
            IloNumVar[] Ti = new IloNumVar[I+1]; // T_1 ... T_{I}. T_0 ignored
            
            for(int i = 0; i < ti.length; i++)
            {
                ti[i] = cplex.numVar(0, Integer.MAX_VALUE);
                xi[i] = cplex.numVar(0, Integer.MAX_VALUE);
            }
            
            for(int i = 1; i < Ti.length; i++)
            {
                Ti[i] = cplex.numVar(0, Integer.MAX_VALUE);
            }
            
            // t_i = t - sum j=i+1 to I of Tj
            for(int i = 0; i < ti.length; i++)
            {
                IloLinearNumExpr rhs = cplex.linearNumExpr();
                
                for(int j = i+1; j <= I; j++)
                {
                    rhs.addTerm(-1, Ti[j]);
                }
                cplex.addEq(ti[i], cplex.sum(t, rhs));
            }

            
            // x_i = x + sum j=i+1 to I of Tj wj
            for(int i = 0; i < xi.length; i++)
            {
                IloLinearNumExpr rhs = cplex.linearNumExpr();
                for(int j = i+1; j <= I; j++)
                {
                    rhs.addTerm(regions.get(j-1).getW(link), Ti[j]);
                }
                cplex.addEq(xi[i], cplex.sum(x, rhs));
            }

            
            // x_0 = L
            cplex.addEq(xi[0], L);
            
            // t_0 within the range of the condition
            cplex.addLe(ti[0], d.getMaxT());
            cplex.addGe(ti[0], d.getMinT());
            
            
            
            // if t_hat_i >= 0
            // t_i >= t_hat_i + x_i/v
            // else if x_hat_i \in [0,L]
            // t_i >= (x_i - x_hat_i)/v
            
            for(int i = 1; i <= I-1; i++)
            {
                double t_hat_i = -1;
                double x_hat_i = -1;
                
                double b_i = regions.get(i-1).getUpperB();

                for(InitialCondition ic : initial)
                {
                    x_hat_i = Math.max(x_hat_i, ic.calcX(b_i));
                }
                
                for(BoundaryCondition uc : upstream)
                {
                    t_hat_i = Math.max(t_hat_i, uc.calcT(b_i));
                }

                if(t_hat_i >= 0)
                {
                    cplex.addGe(ti[i], cplex.sum(t_hat_i, cplex.prod(xi[i], 1.0/v)));
                }
                else if(x_hat_i >= 0 && x_hat_i <= L)
                {
                    cplex.addGe(ti[i], cplex.sum(cplex.prod(xi[i], 1.0/v), -x_hat_i/v));
                }
            }
            
            
            
            
            // N(t,x) <= b_{I-1} + K(x_{I-1} - x)
            if(I > 1)
            {
                cplex.addLe(Ntx,
                        cplex.sum(regions.get(I-1).getUpperB() - K*x, cplex.prod(K, xi[I-1])));
                

                // c(t_0, x_0) + K(x_0-x_i) >= b_i
                // initial_c + q * (ti[0] - mint) + K(xi[0] - xi[i])
                
                
                for(int i = 1; i <= I-1; i++)
                {
                    cplex.addGe(cplex.sum(cplex.sum(d.getInitialC(), cplex.prod(d.getFlow(), cplex.sum(ti[0], -d.getMinT()))), 
                            cplex.sum(cplex.prod(K, xi[0]), cplex.prod(-K, xi[i]))),
                            regions.get(i-1).getUpperB());
                }
                
                
            }
            else
            {
                cplex.addGe(cplex.sum(cplex.sum(d.getInitialC(), cplex.prod(d.getFlow(), cplex.sum(ti[0], -d.getMinT()))), 
                    cplex.sum(cplex.prod(K, xi[0]), -K*x)),
                    Ntx);
            }
            
            cplex.addGe(cplex.sum(cplex.sum(d.getInitialC(), cplex.prod(d.getFlow(), cplex.sum(ti[0], -d.getMinT()))), 
                    cplex.sum(cplex.prod(K, xi[0]), -K*x)),
                    Ntx);

            // N(t,x) <= c(t_0, x_0) + K(x_0 - x)
            
            
            
            IloLinearNumExpr obj = cplex.linearNumExpr();
            

            obj.addTerm(1, Ntx);
            
            for(int i = 1; i <= I-1; i++)
            {
                obj.addTerm(-0.01, ti[i]);
            }
            
            IloAddable obj_added = cplex.addMaximize(obj);
            
            
            boolean solved = cplex.solve();
            
            boolean matches = true;
            
            double outputN = Integer.MAX_VALUE;
            
            if(solved)
            {
                
                outputN = cplex.getValue(Ntx);
                
                double error_range = 0.01;
                
                // sanity check the boundary layers
                for(int i = 1; i < I; i++)
                {
                    if(calculateN(cplex.getValue(ti[i]), cplex.getValue(xi[i]), i) - regions.get(i-1).getUpperB() > error_range)
                    {
                        matches = false;
                    }
                }
                
                if(!matches)
                {
                    cplex.remove(obj_added);
                    
                    
                    obj = cplex.linearNumExpr();
            

                    for(int i = 1; i <= I-1; i++)
                    {
                        obj.addTerm(-0.01, ti[i]);
                    }

                    cplex.addMaximize(obj);

                    solved = cplex.solve();
                    
                    if(L - cplex.getValue(xi[I-1]) > 0.01)
                    {
                        outputN = Math.min(outputN, regions.get(I-2).getUpperB() + K* (cplex.getValue(xi[I-1]) - x));
                    }
                    outputN = Math.min(outputN, d.getC(cplex.getValue(ti[0]))+K*(cplex.getValue(xi[0])-x) );
                    
                    
                    //System.out.println("\tRe-solved ");
                    //System.out.println("\t"+(d.getC(cplex.getValue(ti[0]))+K*(cplex.getValue(xi[0])-x)));
                }
                
                
                
                matches = true;
                
                // sanity check the boundary layers
                for(int i = 1; i < I; i++)
                {
                    if(L - cplex.getValue(xi[i]) > 0.01 && 
                            calculateN(cplex.getValue(ti[i]), cplex.getValue(xi[i]), i) - regions.get(i-1).getUpperB() > error_range)
                    {
                        matches = false;
                    }
                }
            
                
                if(matches)
                {
                    output = Math.min(output, outputN);
                    best = d;
                    
                    if(PRINT)
                    //if(I==2)
                    {
                        System.out.println("Cplex output: N(t,x)["+I+"] = "+outputN+"\t"+d);
                        for(int i = 1; i < I; i++)
                        {
                            System.out.println("\tN(t_1, x_1) = N("+cplex.getValue(ti[i])+", "+cplex.getValue(xi[i])+") = "+calculateN(cplex.getValue(ti[i]), cplex.getValue(xi[i]), i));
                        }
                    }
                }
                else
                {
                    if(PRINT)
                    //if(I==2)
                    {
                        System.out.println("Cplex output: N(t,x)["+I+"] = "+cplex.getValue(Ntx)+"\t"+d);
                        for(int i = 1; i < I; i++)
                        {
                            System.out.println("\tN(t_1, x_1) = N("+cplex.getValue(ti[i])+", "+cplex.getValue(xi[i])+") = "+calculateN(cplex.getValue(ti[i]), cplex.getValue(xi[i]), i));
                        }
                    }
                }
            }
            else
            {
                if(PRINT)
                //if(I==2)
                {
                    System.out.println("Cplex output: N(t,x)["+I+"] = infty\t"+d);
                }
            }

            cplex.end();
        }
        
        //System.out.print(best+"("+I+")\t");
        
        
        for(InitialCondition cond : initial)
        {
            IloCplex cplex = new IloCplex();
            
            //if(!PRINT)
            {
                cplex.setParam(IloCplex.IntParam.MIPDisplay, 0);
                cplex.setOut(null);
            }

            IloNumVar Ntx = cplex.numVar(0, Integer.MAX_VALUE);
            cplex.addLe(Ntx, traceUncongested);

            IloNumVar[] ti = new IloNumVar[I]; // t_0 ... t_{I}
            IloNumVar[] xi = new IloNumVar[I]; // x_0 ... x_{I}
            IloNumVar[] Ti = new IloNumVar[I+1]; // T_1 ... T_{I}. T_0 ignored
            
            for(int i = 0; i < ti.length; i++)
            {
                ti[i] = cplex.numVar(0, Integer.MAX_VALUE);
                xi[i] = cplex.numVar(0, Integer.MAX_VALUE);
            }
            
            for(int i = 1; i < Ti.length; i++)
            {
                Ti[i] = cplex.numVar(0, Integer.MAX_VALUE);
            }
            
            // t_i = t - sum j=i+1 to I of Tj
            for(int i = 0; i < ti.length; i++)
            {
                IloLinearNumExpr rhs = cplex.linearNumExpr();
                
                for(int j = i+1; j <= I; j++)
                {
                    rhs.addTerm(-1, Ti[j]);
                }
                cplex.addEq(ti[i], cplex.sum(t, rhs));
            }

            
            // x_i = x + sum j=i+1 to I of Tj wj
            for(int i = 0; i < xi.length; i++)
            {
                IloLinearNumExpr rhs = cplex.linearNumExpr();
                for(int j = i+1; j <= I; j++)
                {
                    rhs.addTerm(regions.get(j-1).getW(link), Ti[j]);
                }
                cplex.addEq(xi[i], cplex.sum(x, rhs));
            }

            
            // t_0 = 0
            cplex.addEq(ti[0], 0);
            
            // x_0 within the range of the condition
            cplex.addLe(xi[0], cond.getMaxX());
            cplex.addGe(xi[0], cond.getMinX());
            
            
            
            // if t_hat_i >= 0
            // t_i >= t_hat_i + x_i/v
            // else if x_hat_i \in [0,L]
            // t_i >= (x_i - x_hat_i)/v
            
            for(int i = 1; i <= I-1; i++)
            {
                double t_hat_i = -1;
                double x_hat_i = -1;
                
                double b_i = regions.get(i-1).getUpperB();

                for(InitialCondition ic : initial)
                {
                    x_hat_i = Math.max(x_hat_i, ic.calcX(b_i));
                }
                
                for(BoundaryCondition uc : upstream)
                {
                    t_hat_i = Math.max(t_hat_i, uc.calcT(b_i));
                }

                if(t_hat_i >= 0)
                {
                    cplex.addGe(ti[i], cplex.sum(t_hat_i, cplex.prod(xi[i], 1.0/v)));
                }
                else if(x_hat_i >= 0 && x_hat_i <= L)
                {
                    cplex.addGe(ti[i], cplex.sum(cplex.prod(xi[i], 1.0/v), -x_hat_i/v));
                }
            }
            
            
            
            
            // N(t,x) <= b_{I-1} + K(x_{I-1} - x)
            if(I > 1)
            {

                
                // c(t_0, x_0) + K(x_0-x_i) >= b_i
                // initial_c + q * (ti[0] - mint) + K(xi[0] - xi[i])
                
                for(int i = 1; i <= I-1; i++)
                {
                    cplex.addGe(cplex.sum(cplex.sum(cond.getInitialC(), cplex.prod(-cond.getDensity(), cplex.sum(xi[0], -cond.getMaxX()))), 
                            cplex.sum(cplex.prod(K, xi[0]), cplex.prod(-K, xi[i]))),
                            regions.get(i-1).getUpperB());
                }
            }

            // N(t,x) <= c(t_0, x_0) + K(x_0 - x)
            cplex.addGe(cplex.sum(cplex.sum(cond.getInitialC(), cplex.prod(-cond.getDensity(), cplex.sum(xi[0], -cond.getMaxX()))), 
                    cplex.sum(cplex.prod(K, xi[0]), -K*x)),
                    Ntx);
            
            
            
            IloLinearNumExpr obj = cplex.linearNumExpr();
            obj.addTerm(1, Ntx);
            
            for(int i = 1; i <= I-1; i++)
            {
                obj.addTerm(-0.01, ti[i]);
            }
            
            
            IloAddable obj_added = cplex.addMaximize(obj);
            
            
            boolean solved = cplex.solve();
            
            boolean matches = true;
            
            double outputN = Integer.MAX_VALUE;
            
            if(solved)
            {
                
                outputN = cplex.getValue(Ntx);
                
                double error_range = 0.01;
                
                // sanity check the boundary layers
                for(int i = 1; i < I; i++)
                {
                    if(calculateN(cplex.getValue(ti[i]), cplex.getValue(xi[i]), i) - regions.get(i-1).getUpperB() > error_range)
                    {
                        matches = false;
                    }
                }
                
                if(!matches)
                {
                    cplex.remove(obj_added);
                    
                    
                    obj = cplex.linearNumExpr();
            

                    for(int i = 1; i <= I-1; i++)
                    {
                        obj.addTerm(-0.01, ti[i]);
                    }

                    cplex.addMaximize(obj);

                    solved = cplex.solve();
                    
                    if(L - cplex.getValue(xi[I-1]) > 0.01)
                    {
                        outputN = Math.min(outputN, regions.get(I-2).getUpperB() + K* (cplex.getValue(xi[I-1]) - x));
                    }
                    outputN = Math.min(outputN, cond.getC(cplex.getValue(xi[0]))+K*(cplex.getValue(xi[0])-x) );
                    
                    
                    //System.out.println("\tRe-solved ");
                    //System.out.println("\t"+(d.getC(cplex.getValue(ti[0]))+K*(cplex.getValue(xi[0])-x)));
                }
                
                
                
                matches = true;
                
                // sanity check the boundary layers
                for(int i = 1; i < I; i++)
                {
                    if(L - cplex.getValue(xi[i]) > 0.01 && 
                            calculateN(cplex.getValue(ti[i]), cplex.getValue(xi[i]), i) - regions.get(i-1).getUpperB() > error_range)
                    {
                        matches = false;
                    }
                }
            
                
                if(matches)
                {
                    output = Math.min(output, outputN);

                    
                    if(PRINT)
                    {
                        System.out.println("Cplex output: N(t,x)["+I+"] = "+outputN+"\t"+cond);
                        for(int i = 1; i < I; i++)
                        {
                            System.out.println("\tN(t_1, x_1) = N("+cplex.getValue(ti[i])+", "+cplex.getValue(xi[i])+") = "+calculateN(cplex.getValue(ti[i]), cplex.getValue(xi[i]), i));
                        }
                    }
                }
                else
                {
                    if(PRINT)
                    {
                        System.out.println("Cplex output: N(t,x)["+I+"] = "+cplex.getValue(Ntx)+"\t"+cond);
                        for(int i = 1; i < I; i++)
                        {
                            System.out.println("\tN(t_1, x_1) = N("+cplex.getValue(ti[i])+", "+cplex.getValue(xi[i])+") = "+calculateN(cplex.getValue(ti[i]), cplex.getValue(xi[i]), i));
                        }
                    }
                }
            }
            else
            {
                if(PRINT)
                {
                    System.out.println("Cplex output: N(t,x)["+I+"] = infty\t"+cond);
                }
            }

            cplex.end();
        }
        
        
        
        output = Math.min(output, traceUncongested);
        
        return output;
    }
    
}
