/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta;

import avdta.network.Simulator;
import java.io.File;
import java.util.Scanner;

/**
 *
 * @author ml26893
 */
public class CLI 
{
    public static void CLI()
    {
        try
        {
            Scanner scan1 = new Scanner(System.in);
            String input;
            Simulator run = new Simulator("random");
            //run.msa(2);
            main:
            while(true)
            {
                System.out.println("These are your options; you can either enter the option number or the option name written after the colon:");
                System.out.println("1> Run MSA: MSA");
                System.out.println("2> Read Signal Network: Read Signals");
                System.out.println("3> Read TBR Network: Read TBR");
                System.out.println("4> Print Link Travel Time: Print Link TT ");
                System.out.println("5> Exit Simulation: Exit");
                System.out.println("Enter the command:");
                input = scan1.next();
                if(input.trim().equals("1") || input.trim().toUpperCase().equals("MSA"))
                {
                    System.out.println("Enter the following information:");
                    System.out.print("Network name:");
                    String network = scan1.next();
                    System.out.print("Scenario name:");
                    String scenario = scan1.next();
                    System.out.print("Demand file:");
                    String demand = scan1.next();
                    System.out.print("AV proportion (0 to 1):");
                    double proportion = Double.parseDouble(scan1.next());
                    String model;
                    System.out.print("CTM or LTM?:");
                    model = scan1.next();
                    if(model != null)
                    model = model.toUpperCase();
                    while((model == null) || (!model.equals("CTM") && (!model.equals("LTM"))))
                    {
                        System.out.println("Wrong input!");
                        System.out.print("CTM or LTM?:");
                        model = scan1.next();
                        if(model != null)
                            model = model.toUpperCase();
                    }
                    int max_iter, start_iter;
                    double min_gap;
                    System.out.println("Here are your MSA options (enter the option number):");
                    System.out.println("1> MSA with maximum iteration specified");
                    System.out.println("2> MSA with maximum iteration and connvergence criterion specified");
                    System.out.println("3> MSA with starting iteration count and maximum iteration specified");
                    System.out.println("4> MSA with all three specified");
                    System.out.print("Enter your option:");
                    int choice = Integer.parseInt(scan1.next());
                    while(!(choice ==1 || choice == 2 || choice==3 || choice==4))
                    {
                        System.out.println("Wrong input!");
                        System.out.println("Here are your MSA options (enter the option number):");
                        System.out.println("1> MSA with maximum iteration specified");
                        System.out.println("2> MSA with maximum iteration and connvergence criterion specified");
                        System.out.println("3> MSA with starting iteration count and maximum iteration specified");
                        System.out.println("4> MSA with all three specified");
                        System.out.print("Enter your option:");
                        choice = Integer.parseInt(scan1.next());
                    }
                    switch(choice)
                    {
                        case 1: 
                            System.out.print("Enter the maximum iteration:");
                            max_iter = Integer.parseInt(scan1.next());
                            run.msa(max_iter);
                            break;
                        case 2:
                            System.out.print("Enter the maximum iteration:");
                            max_iter = Integer.parseInt(scan1.next());
                            System.out.print("Enter the minimum gap:");
                            min_gap = Double.parseDouble(scan1.next());
                            run.msa(max_iter, min_gap);
                            break;
                        case 3:
                            System.out.print("Enter the maximum iteration:");
                            max_iter = Integer.parseInt(scan1.next());
                            System.out.print("Enter the start iteration:");
                            start_iter = Integer.parseInt(scan1.next());
                            run.msa_cont(start_iter, max_iter);
                            break;
                        case 4:
                            System.out.print("Enter the maximum iteration:");
                            max_iter = Integer.parseInt(scan1.next());
                            System.out.print("Enter the start iteration:");
                            start_iter = Integer.parseInt(scan1.next());
                            System.out.print("Enter the minimum gap:");
                            min_gap = Double.parseDouble(scan1.next());
                            run.msa_cont(start_iter, max_iter, min_gap);
                            break;
                        default:
                            System.out.println("Wrong option");
                            System.exit(0);
                    }
                    System.out.print("Enter the maximum iteration:");
                    max_iter = Integer.parseInt(scan1.next());
                    System.out.print("Enter the start iteration:");
                    max_iter = Integer.parseInt(scan1.next());
                    System.out.print("Enter the maximum iteration:");
                    max_iter = Integer.parseInt(scan1.next());

                    System.out.println(network + " " + scenario + " " + demand + " " + proportion + " " + model + " " + proportion);
                    //Results msa_cont(int start_iter, int max_iter, double min_gap) throws IOException
                    //run.msa_cont();
                    continue main;
                }
                else if(input.trim().equals("2") || input.trim().toUpperCase().equals("READ SIGNALS"))
                {
                    System.out.println("Enter the following information:");
                    System.out.print("Network name:");
                    String network = scan1.next();
                    System.out.print("Link type (Integer):");
                    int linktype = Integer.parseInt(scan1.next());
                    System.out.print("Demand file:");
                    String demandfile = scan1.next();
                    Simulator.readSignalsNetwork(network, linktype, demandfile);
                    
                    continue main;
                }
                else if(input.trim().equals("3") || input.trim().toUpperCase().equals("READ TBR"))
                {
                    System.out.println("Enter the following information:");
                    System.out.print("Network name:");
                    String network = scan1.next();
                    System.out.print("Link type (Integer):");
                    int linktype = Integer.parseInt(scan1.next());
                    System.out.print("Demand file:");
                    String demandfile = scan1.next();
                    Simulator.readSignalsNetwork(network, linktype, demandfile);
                    
                    continue main;
                }
                else if(input.trim().equals("4") || input.trim().toUpperCase().equals("PRINT LINK TT"))
                {
                    System.out.println("Enter the following information:");
                    System.out.print("Scenario name:");
                    String scenario = scan1.next();
                    System.out.println("Output can be found at: "+"results/"+scenario+"linktt.txt");
                    System.out.print("Star time:");
                    int start = Integer.parseInt(scan1.next());
                    while(start < 0)
                    {
                        System.out.println("Start time cannot be negative!");
                        System.out.print("Star time:");
                        start = Integer.parseInt(scan1.next());
                    }
                    System.out.print("End time:");
                    int end = Integer.parseInt(scan1.next());
                    while(end < 0 || end < start)
                    {
                        System.out.println("End time cannot be negative and should not be less than the start time!");
                        System.out.print("End time:");
                        end = Integer.parseInt(scan1.next());
                    }
                    
                    File file = new File("results/"+scenario+"linktt.txt");
                    
                    if(run != null)
                    {
                        run.printLinkTT(start, end, file);
                        continue main;
                    }
                    else
                    {
                        System.out.println("A simulation has to be performed before you print the results.");
                        continue main;
                    }
                    
                }
                else if(input.trim().equals("5") || input.trim().toUpperCase().equals("EXIT"))
                {
                    int pause = 10;  //pause in seconds
                    System.out.println("Exiting in "+pause+" second.");
                    System.out.println("*** This interface has been developed by Sudesh K Agrawal. ***");
                    
                    try
                    {
                        Thread.sleep(pause*1000); //milliseconds pause
                    }
                    catch(InterruptedException e2)
                    {
                        Thread.currentThread().interrupt();
                    }
                    System.exit(0);
                }
                else
                {
                    System.out.println("Wrong input!");
                    System.out.println("Try Again? (y/n)");
                    char decision = scan1.next().charAt(0);
                    if(decision == 'y' || decision == 'Y')
                    {
                        continue main;
                    }
                    else
                    {
                        //throw new Exception("Wrong input! Exiting because user does not wish to try again.");
                        System.out.println("Wrong input! Exiting because user does not wish to try again.");
                        int pause = 10;  //pause in seconds
                        System.out.println("System exiting in "+pause+ " second.");
                        System.out.println("*** This interface has been developed by Sudesh K Agrawal. ***");
                        try
                        {
                            Thread.sleep(pause*1000); //milliseconds pause
                        }
                        catch(InterruptedException e2)
                        {
                            Thread.currentThread().interrupt();
                        }
                        System.exit(1);
                    }
                }
            }
            
        }
        catch(Exception e)
        {
            System.out.println("Something went wrong!");
            System.out.println("Error message: "+e);
            int pause = 10;  //pause in seconds
            System.out.println("System exiting in "+pause+ " second.");
            try
            {
                Thread.sleep(pause*1000); //milliseconds pause
            }
            catch(InterruptedException e2)
            {
                Thread.currentThread().interrupt();
            }
            System.exit(1);
       }
    }
    
    public static void GraphicalCLI()
    {
        C.io.setTitle("Simulation run");    //Sets the title of the command line window
        try
        {
            String input;
            C.io.println("These are your options; you can enter the option number or the option name written after the colon:");
            C.io.println("1> Run MSA: MSA");
            C.io.println("2> Read Signal Network: Read Signals");
            C.io.println("3> Read TBR Network: Read TBR");
            C.io.println("4> Go back to Main");
            C.io.println("Enter the command:");
            input = C.io.nextLine();
            //System.out.println("You wish to run:"input);
            if(input.equals("MSA") || input.equals("1"))
            {
                C.io.println("Enter the following information:");
                C.io.print("Network name:");
                String network = C.io.nextLine();
                C.io.print("Scenario name:");
                String scenario = C.io.nextLine();
                C.io.print("Demand file:");
                String demand = C.io.nextLine();
                C.io.print("AV proportion (0 to 1):");
                double proportion = Double.parseDouble(C.io.nextLine());
                String model;
                C.io.print("CTM or LTM?:");
                model = C.io.nextLine();
                if(model != null)
                    model = model.toUpperCase();
                while( (model == null) || (!model.equals("CTM") && !model.equals("LTM")) )
                {
                    C.io.println("Wrong input!");
                    C.io.print("CTM or LTM?:");
                    model = C.io.nextLine();
                    if(model != null)
                        model = model.toUpperCase();
                }
                int max_iter, start_iter;
                double min_gap;
                C.io.println("Here are your MSA options (enter the option number):");
                C.io.println("1> MSA with maximum iteration specified");
                C.io.println("2> MSA with maximum iteration and connvergence criterion specified");
                C.io.println("3> MSA with starting iteration count and maximum iteration specified");
                C.io.println("4> MSA with all three specified");
                C.io.print("Enter your option:");
                int choice = Integer.parseInt(C.io.nextLine());
                while(!(choice ==1 || choice == 2 || choice==3 || choice==4))
                {
                    C.io.println("Wrong input!");
                    C.io.println("Here are your MSA options (enter the option number):");
                    C.io.println("1> MSA with maximum iteration specified");
                    C.io.println("2> MSA with maximum iteration and connvergence criterion specified");
                    C.io.println("3> MSA with starting iteration count and maximum iteration specified");
                    C.io.println("4> MSA with all three specified");
                    C.io.print("Enter your option:");
                    choice = Integer.parseInt(C.io.nextLine());
                }
                switch(choice)
                {
                    case 1: 
                        C.io.print("Enter the maximum iteration:");
                        max_iter = Integer.parseInt(C.io.nextLine());
                        break;
                    case 2:
                        C.io.print("Enter the maximum iteration:");
                        max_iter = Integer.parseInt(C.io.nextLine());
                        C.io.print("Enter the minimum gap:");
                        min_gap = Double.parseDouble(C.io.nextLine());
                        break;
                    case 3:
                        C.io.print("Enter the maximum iteration:");
                        max_iter = Integer.parseInt(C.io.nextLine());
                        C.io.print("Enter the start iteration:");
                        start_iter = Integer.parseInt(C.io.nextLine());
                        break;
                    case 4:
                        C.io.print("Enter the maximum iteration:");
                        max_iter = Integer.parseInt(C.io.nextLine());
                        C.io.print("Enter the start iteration:");
                        start_iter = Integer.parseInt(C.io.nextLine());
                        C.io.print("Enter the minimum gap:");
                        min_gap = Double.parseDouble(C.io.nextLine());
                        break;
                    default:
                        C.io.println("Wrong option");
                        System.exit(0);
                }
                C.io.print("Enter the maximum iteration:");
                max_iter = Integer.parseInt(C.io.nextLine());
                C.io.print("Enter the start iteration:");
                max_iter = Integer.parseInt(C.io.nextLine());
                C.io.print("Enter the maximum iteration:");
                max_iter = Integer.parseInt(C.io.nextLine());
         
                //SAVMain.main(args);
                //SecurityTest.main(args);
                C.io.println(network +" "+scenario+" "+demand+" "+proportion+" "+model+" "+proportion);
            }
            else if(input.equals("Read Signals") || input.equals("2"))
            {
                C.io.println("Enter the following information:");
                C.io.print("Network name:");
                String network = C.io.nextLine();
                C.io.print("Link type (Integer):");
                int linktype = Integer.parseInt(C.io.nextLine());
                C.io.print("Demand file:");
                String demandfile = C.io.nextLine();
                Simulator.readSignalsNetwork(network, linktype, demandfile);
         
                //System.out.println(StopSign.findT(true, 3*Math.PI));
            }
            else if(input.equals("Read TBR") || input.equals("3"))
            {
                C.io.println("Enter the following information:");
                C.io.print("Network name:");
                String network = C.io.nextLine();
                C.io.print("Link type (Integer):");
                int linktype = Integer.parseInt(C.io.nextLine());
                C.io.print("Demand file:");
                String demandfile = C.io.nextLine();
                Simulator.readSignalsNetwork(network, linktype, demandfile);
            }
            else
            {
                throw new Exception("Wrong input! Try again.");
            }
        }
        catch(Exception e)
        {
            C.io.println("Something went wrong!");
            C.io.println("Error message: "+e);
            int pause = 10;  //pause in seconds
            C.io.println("System exiting in "+pause+" second.");
            try
            {
                Thread.sleep(pause*1000); //milliseconds pause
            }
            catch(InterruptedException e2)
            {
                Thread.currentThread().interrupt();
            }
            System.exit(1);
        }
        
    }
}
