/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.network.ReadNetwork;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 *
 * @author Michael
 */
public class ChangeOptions extends JFrame
{
    
    private String network;
    
    private Map<String, String> options;
    
    private JList<String> list;
    
    private JButton save;
    
    
    public ChangeOptions(final String network)
    {
        this.network = network;
        
        readOptions();
        
        setTitle(GUI.getTitle());
        setIconImage(GUI.getIcon());
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        GraphicUtils.constrain(p, new JLabel("Change options for "+network), 0, 0, 3, 1);
        
        list = new JList<String>();
        updateListData();
        
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        GraphicUtils.constrain(p, new JScrollPane(list), 0, 1, 1, 8);
        
        JButton addnew = new JButton("Add new");
        
        GraphicUtils.constrain(p, addnew, 1, 1, 2, 1, GridBagConstraints.CENTER);
        GraphicUtils.constrain(p, new JLabel("Key"), 1, 2, 1, 1);
        GraphicUtils.constrain(p, new JLabel("Value"), 1, 3, 1, 1);
        
        final JTextField key = new JTextField(25);
        final JTextField value = new JTextField(25);
        
        GraphicUtils.constrain(p, key, 2, 2, 1, 1);
        GraphicUtils.constrain(p, value, 2, 3, 1, 1);
        
        save = new JButton("Save");
        GraphicUtils.constrain(p, save, 1, 4, 2, 1, GridBagConstraints.CENTER);
        
        save.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                String k = key.getText().replaceAll("\\s", "");
                String v = value.getText().replaceAll("\\s", "");
                
                key.setText("");
                value.setText("");
                list.setSelectedIndex(-1);
                save.setEnabled(false);
                
                if(k.length() > 0)
                {
                    options.put(k, v);
                    writeOptions();
                }
            }
        });
        
        addnew.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                key.setText("");
                value.setText("");
                list.clearSelection();
            }
        });
        
        list.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                String k = list.getSelectedValue();
                
                if(k == null)
                {
                    key.setText("");
                    value.setText("");
                }
                else
                {
                    key.setText(k);
                    value.setText(options.get(k));
                }
            }
        });
        
        
        
        
        
        
        
        add(p);
        
        pack();
        setResizable(false);
        

        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(1);
            }
        });
        
        setVisible(true);
    }
    
    public void updateListData()
    {
        String[] data = new String[options.size()];
        
        int idx = 0;
        for(String x : options.keySet())
        {
            data[idx++] = x;
        }
        
        list.setListData(data);
    }
    
    public File getOptionsFile()
    {
        return new File("data/"+network+"/options.txt");
    }
    public void readOptions()
    {
        options = new TreeMap<String, String>();
        
        
        
        try
        {
            File file = getOptionsFile();
        
            if(!file.exists())
            {
                ReadNetwork.fillOptions(network);
            }
            
            Scanner filein = new Scanner(file);
        
            while(filein.hasNext())
            {
                options.put(filein.next(), filein.next());
            }
        
            filein.close();
        }
        catch(IOException ex)
        {
            
        }
    }
    
    public void writeOptions()
    {
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    PrintStream fileout = new PrintStream(new FileOutputStream(getOptionsFile()), true);

                    for(String k : options.keySet())
                    {
                        fileout.println(k+"\t"+options.get(k));
                    }

                    fileout.close();
                    updateListData();

                    save.setEnabled(true);
                }
                catch(IOException ex)
                {
                    ex.printStackTrace(System.err);
                }
            }
        };
        t.start();
 
    }
}
