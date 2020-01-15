package simulation;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Random;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Scanner;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.table.*;
import javax.swing.*;
import java.text.*;
import java.awt.*;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;
import javax.swing.JComponent;
import javax.swing.ImageIcon;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 *
 * @author Shania Dicen
 */
public class SimulationGUI extends javax.swing.JFrame {

    int numCust = 0;
    int methodCallCount = 0;
    Scanner sc = new Scanner(System.in);
    double SIMULATION_TIME = 40;
    int maxQt1 = 0;
    int maxQt2 = 0;
    int maxQt3 = 0;
    int maxQt4 = 0;
    int maxQt5 = 0;
    Replication r1;
    ArrayList<ReplicationCustomer> r1Log;
    Replication r2;
    ArrayList<ReplicationCustomer> r2Log;
    Replication r3;
    ArrayList<ReplicationCustomer> r3Log;
    Replication r4;
    ArrayList<ReplicationCustomer> r4Log;
    Replication r5;
    ArrayList<ReplicationCustomer> r5Log;

    /**
     * Creates new form SimulationGUI
     */
    public SimulationGUI() {

        FrameDragListener frameDragListener = new FrameDragListener(this);
        this.addMouseListener(frameDragListener);
        this.addMouseMotionListener(frameDragListener);
        initComponents();
    }

    public SimulationGUI(int nc, Replication r1, ArrayList<ReplicationCustomer> r1Log, Replication r2, ArrayList<ReplicationCustomer> r2Log, Replication r3, ArrayList<ReplicationCustomer> r3Log, Replication r4, ArrayList<ReplicationCustomer> r4Log, Replication r5, ArrayList<ReplicationCustomer> r5Log) {

        FrameDragListener frameDragListener = new FrameDragListener(this);
        this.addMouseListener(frameDragListener);
        this.addMouseMotionListener(frameDragListener);
        initComponents();
        numCust = nc;
        this.r1 = r1;
        this.r1Log = r1Log;
        this.r2 = r2;
        this.r2Log = r2Log;
        this.r3 = r3;
        this.r3Log = r3Log;
        this.r4 = r4;
        this.r4Log = r4Log;
        this.r5 = r5;
        this.r5Log = r5Log;
        start();
    }

    public void start() {
        r1Log = simulate(r1);
        r2Log = simulate(r2);
        r3Log = simulate(r3);
        r4Log = simulate(r4);
        r5Log = simulate(r5);
    }

    public ArrayList<ReplicationCustomer> simulate(Replication replication) {
        methodCallCount++;
        int clock = 0;
        boolean end = false;
        int departure = 0;
        int waitingTime = 0;
        int p = 0;
        int n = 0;
        int totalWQ = 0;
        int maxWQ = 0;
        int totalTS = 0;
        int maxTS = 0;
        LinkedList<Integer> inService = new LinkedList<Integer>();
        LinkedList<Integer> inQ = new LinkedList<Integer>();
        ArrayList<ReplicationCustomer> log = new ArrayList<ReplicationCustomer>();
        LinkedList<Customer> customers = new LinkedList<Customer>();
        LinkedList<ReplicationCustomer> onGoing = new LinkedList<ReplicationCustomer>(); // arrived but haven't departed
        customers.addAll(replication.getCustomers());
        int depCount = customers.size() + 1;

        log.add(new ReplicationCustomer(0, 0, "INIT", 0, 0, new LinkedList<Integer>(), new LinkedList<Integer>(), 0, 0,
                0, 0, 0, 0));

        while (!end && depCount != 0) {
            if (customers.size() != 0) {
                if (clock == customers.get(0).getArrivalTime()) {
                    Customer c = customers.pop();
                    ReplicationCustomer rc = new ReplicationCustomer();
                    if (inService.size() == 0) {
                        n++;
                        rc.setN(n);
                        inService.add(c.getArrivalTime());
                        rc.setInService(inService);
                    } else {
                        inQ.add(c.getArrivalTime());
                        LinkedList<Integer> tempQ = new LinkedList<Integer>(inQ);
                        rc.setN(n);
                        rc.setInService(inService);
                        rc.setInQueue(tempQ);
                        rc.setQt(inQ.size());
                        switch (methodCallCount) {
                            case 1:
                                maxQt1 = determineHigher(maxQt1, inQ.size());
                                break;
                            case 2:
                                maxQt2 = determineHigher(maxQt2, inQ.size());
                                break;
                            case 3:
                                maxQt3 = determineHigher(maxQt3, inQ.size());
                                break;
                            case 4:
                                maxQt4 = determineHigher(maxQt4, inQ.size());
                                break;
                            case 5:
                                maxQt5 = determineHigher(maxQt5, inQ.size());
                                break;
                            default:
                                System.out.println("");
                                break;
                        }
                    }
                    rc.setCnum(c.getNumber());
                    rc.setTime(c.getArrivalTime());
                    rc.setEventType("ARR");
                    rc.setTotalWQ(totalWQ);
                    rc.setWqMax(maxWQ);
                    rc.setTotalTS(totalTS);
                    rc.setTsMax(maxTS);

                    departure = c.getArrivalTime() + c.getServiceTime() + waitingTime;
                    waitingTime = departure - inService.get(0);
                    rc.setTimeDep(departure);

                    if (inService.size() != 0) {
                        rc.setBt(1);
                    } else {
                        rc.setBt(0);
                    }
                    rc.setP(p);
                    log.add(rc);
                    onGoing.add(rc);
                }
            }

            if (onGoing.size() != 0 && clock == onGoing.peek().getTimeDep()) {
                ReplicationCustomer temp = onGoing.pop();
                ReplicationCustomer rc = new ReplicationCustomer();
                int wTime = 0;
                rc.setCnum(temp.getCnum());
                rc.setTime(temp.getTimeDep());
                rc.setEventType("DEP");

                if (inQ.size() > 0) {
                    n++;
                    inService = new LinkedList<Integer>();
                    inService.add(inQ.pop());
                    LinkedList<Integer> tempQ = new LinkedList<Integer>(inQ);
                    rc.setInService(inService);
                    rc.setInQueue(tempQ);
                    rc.setQt(inQ.size());
                    wTime = temp.getTimeDep() - inService.peek(); // get waiting time if there's an entity in service
                } else {
                    inService = new LinkedList<Integer>();
                    rc.setQt(inQ.size());
                }

                if (inQ.size() != 0) {
                    rc.setBt(1);
                } else {
                    rc.setBt(0);
                }

                maxWQ = Math.max(maxWQ, wTime); // set max waiting time
                totalWQ += wTime;
                rc.setWqMax(maxWQ);
                rc.setWq(wTime); // set waiting time of entity
                rc.setTotalWQ(totalWQ); // set total waiting time
                rc.setN(n);
                p++; // increment production numbers
                rc.setP(p);

                int cycleTime = temp.getTimeDep() - temp.getTime();
                totalTS += cycleTime;
                rc.setTs(cycleTime);
                rc.setTotalTS(totalTS);
                maxTS = Math.max(maxTS, cycleTime);
                rc.setTsMax(maxTS);

                log.add(rc);
                depCount--;

            }
            clock++;
            if (clock >= SIMULATION_TIME) {
                end = true;
            }

        }

        log = computeAreaUnderQ(log);
        log = computeAreaUnderB(log);

        int logSize = log.size() - 1;

        log.add(new ReplicationCustomer(log.get(logSize).getCnum(), (int) SIMULATION_TIME, "END",
                log.get(logSize).getQt(), log.get(logSize).getBt(), log.get(logSize).getInQueue(),
                log.get(logSize).getInService(), log.get(logSize).getP(), log.get(logSize).getN(),
                log.get(logSize).getTotalWQ(), log.get(logSize).getWqMax(), log.get(logSize).getTotalTS(),
                log.get(logSize).getTsMax(), log.get(logSize).getTotalAreaQ(), log.get(logSize).getTotalAreaB()));
        replication.setReplication(log);
        return log;
    }

    public ArrayList<ReplicationCustomer> computeAreaUnderQ(ArrayList<ReplicationCustomer> r) {
        ArrayList<ReplicationCustomer> simulation = new ArrayList<ReplicationCustomer>(r);
        int totalAreaQ = 0;
        int qt = 0;
        for (int i = 0; i < r.size(); i++) {
            if (i > 0) {

                if (checkDuplicateTime(r, simulation.get(i - 1).getTime(), i) == true) {
                    int index = getDuplicateIndex(r, simulation.get(i - 1).getTime(), i);
                    qt = Math.max(simulation.get(index + 1).getQt(), simulation.get(index).getQt());

                } else {
                    qt = simulation.get(i - 1).getQt();
                }

                int areaQ = qt * (simulation.get(i).getTime() - simulation.get(i - 1).getTime());
                totalAreaQ += areaQ;
                simulation.get(i).setTotalAreaQ(totalAreaQ);

            }

        }

        return simulation;
    }

    public ArrayList<ReplicationCustomer> computeAreaUnderB(ArrayList<ReplicationCustomer> r) {
        ArrayList<ReplicationCustomer> simulation = new ArrayList<ReplicationCustomer>(r);
        int totalAreaB = 0;
        int bt = 0;
        for (int i = 0; i < r.size(); i++) {
            if (i > 0) {

                if (checkDuplicateTime(r, simulation.get(i - 1).getTime(), i) == true) {
                    int index = getDuplicateIndex(r, simulation.get(i - 1).getTime(), i);
                    bt = Math.max(simulation.get(index + 1).getBt(), simulation.get(index).getBt());
                } else {
                    bt = simulation.get(i - 1).getBt();
                }

                int areaB = bt * (simulation.get(i).getTime() - simulation.get(i - 1).getTime());
                totalAreaB += areaB;
                simulation.get(i).setTotalAreaB(totalAreaB);

            }

        }

        return simulation;
    }

    public boolean checkDuplicateTime(ArrayList<ReplicationCustomer> r, int time, int index) {
        int count = -1;
        for (int i = 0; i < r.size(); i++) {
            if (i != index) {
                if (time != 0) {
                    if (r.get(i).getTime() == time) {
                        count++;
                    }
                }

            }
        }
        if (count > 0) {
            return true;
        } else {
            return false;
        }

    }

    public int getDuplicateIndex(ArrayList<ReplicationCustomer> r, int time, int inTime) {
        int index = -1;
        for (int i = 0; i < r.size(); i++) {
            if (i != inTime) {
                if (r.get(i).getTime() == time) {
                    index = i;
                }
            }
        }
        return index;
    }

    public int determineHigher(int x, int y) {
        if (x > y) {
            return x;
        } else {
            return y;
        }
    }

    public void displayTable(Replication r1, Replication r2, Replication r3, Replication r4, Replication r5) {
        JFrame frame = new JFrame("Randomly Generated Values");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTable tbl1 = new JTable();
        tbl1.setDefaultRenderer(Object.class, new StripedRowTableCellRenderer());
        DefaultTableModel dtm1 = new DefaultTableModel(0, 0);
        String header1[] = new String[]{"Customer", "RDA Interarrival", "Interarrival Time", "Arrival Time", "RDA Service", "Service Time"};

        dtm1.setColumnIdentifiers(header1);

        tbl1.setModel(dtm1);
        for (int i = 0;
                i < r1.getNumber()
                        .size(); i++) {
            dtm1.addRow(new Object[]{r1.getNumber().get(i), r1.getRandomInter().get(i), r1.getInterArrivalTime().get(i),
                r1.getArrivalTime().get(i), r1.getRandomService().get(i), r1.getServiceTime().get(i)
            });
        }
        JTable tbl2 = new JTable();
        tbl2.setDefaultRenderer(Object.class, new StripedRowTableCellRenderer());
        DefaultTableModel dtm2 = new DefaultTableModel(0, 0);
        String header2[] = new String[]{"Customer", "RDA Interarrival", "Interarrival Time", "Arrival Time", "RDA Service", "Service Time"};

        dtm2.setColumnIdentifiers(header1);

        tbl2.setModel(dtm2);
        for (int i = 0;
                i < r2.getNumber()
                        .size(); i++) {
            dtm2.addRow(new Object[]{r2.getNumber().get(i), r2.getRandomInter().get(i), r2.getInterArrivalTime().get(i),
                r2.getArrivalTime().get(i), r2.getRandomService().get(i), r2.getServiceTime().get(i)
            });
        }
        JTable tbl3 = new JTable();
        tbl3.setDefaultRenderer(Object.class, new StripedRowTableCellRenderer());
        DefaultTableModel dtm3 = new DefaultTableModel(0, 0);
        String header3[] = new String[]{"Customer", "RDA Interarrival", "Interarrival Time", "Arrival Time", "RDA Service", "Service Time"};

        dtm3.setColumnIdentifiers(header3);

        tbl3.setModel(dtm3);
        for (int i = 0;
                i < r3.getNumber()
                        .size(); i++) {
            dtm3.addRow(new Object[]{r3.getNumber().get(i), r3.getRandomInter().get(i), r3.getInterArrivalTime().get(i),
                r3.getArrivalTime().get(i), r3.getRandomService().get(i), r3.getServiceTime().get(i)
            });
        }
        JTable tbl4 = new JTable();
        tbl4.setDefaultRenderer(Object.class, new StripedRowTableCellRenderer());
        DefaultTableModel dtm4 = new DefaultTableModel(0, 0);
        String header4[] = new String[]{"Customer", "RDA Interarrival", "Interarrival Time", "Arrival Time", "RDA Service", "Service Time"};

        dtm4.setColumnIdentifiers(header4);

        tbl4.setModel(dtm4);
        for (int i = 0;
                i < r4.getNumber()
                        .size(); i++) {
            dtm4.addRow(new Object[]{r4.getNumber().get(i), r4.getRandomInter().get(i), r4.getInterArrivalTime().get(i),
                r4.getArrivalTime().get(i), r4.getRandomService().get(i), r4.getServiceTime().get(i)
            });
        }
        JTable tbl5 = new JTable();
        tbl5.setDefaultRenderer(Object.class, new StripedRowTableCellRenderer());
        DefaultTableModel dtm5 = new DefaultTableModel(0, 0);
        String header5[] = new String[]{"Customer", "RDA Interarrival", "Interarrival Time", "Arrival Time", "RDA Service", "Service Time"};

        dtm5.setColumnIdentifiers(header5);

        tbl5.setModel(dtm5);
        for (int i = 0;
                i < r5.getNumber()
                        .size(); i++) {
            dtm5.addRow(new Object[]{r5.getNumber().get(i), r5.getRandomInter().get(i), r5.getInterArrivalTime().get(i),
                r5.getArrivalTime().get(i), r5.getRandomService().get(i), r5.getServiceTime().get(i)
            });
        }

        tbl1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        tbl2.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        tbl3.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        tbl4.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        tbl5.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        tbl1.setSelectionBackground(
                new java.awt.Color(255, 178, 109));
        tbl2.setSelectionBackground(
                new java.awt.Color(255, 178, 109));
        tbl3.setSelectionBackground(
                new java.awt.Color(255, 178, 109));
        tbl4.setSelectionBackground(
                new java.awt.Color(255, 178, 109));
        tbl5.setSelectionBackground(
                new java.awt.Color(255, 178, 109));

        JPanel p1 = new JPanel();
        JPanel p2 = new JPanel();
        JPanel p3 = new JPanel();
        JPanel p4 = new JPanel();
        JPanel p5 = new JPanel();
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new java.awt.Color(255, 255, 255));
        tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        dataPanel.add(tabbedPane, BorderLayout.CENTER);

        p1.setLayout(
                new BorderLayout());
        p1.add(
                new JScrollPane(tbl1), BorderLayout.CENTER);
        p2.setLayout(
                new BorderLayout());
        p2.add(
                new JScrollPane(tbl2), BorderLayout.CENTER);
        p3.setLayout(
                new BorderLayout());
        p3.add(
                new JScrollPane(tbl3), BorderLayout.CENTER);
        p4.setLayout(
                new BorderLayout());
        p4.add(
                new JScrollPane(tbl4), BorderLayout.CENTER);
        p5.setLayout(
                new BorderLayout());
        p5.add(
                new JScrollPane(tbl5), BorderLayout.CENTER);

        tabbedPane.addTab(
                "Replication 1", p1);
        tabbedPane.addTab(
                "Replication 2", p2);
        tabbedPane.addTab(
                "Replication 3", p3);
        tabbedPane.addTab(
                "Replication 4", p4);
        tabbedPane.addTab(
                "Replication 5", p5);

    }

    public void displaySimulationTable(ArrayList<ReplicationCustomer> log1, ArrayList<ReplicationCustomer> log2, ArrayList<ReplicationCustomer> log3, ArrayList<ReplicationCustomer> log4, ArrayList<ReplicationCustomer> log5) {
        JTable tbl1 = new JTable();
        tbl1.setDefaultRenderer(Object.class, new StripedRowTableCellRenderer());

        DefaultTableModel dtm1 = new DefaultTableModel(0, 0);
        String header1[] = new String[]{"Cust#", "Time", "Event", "Q(t)", "B(t)", "In Queue", "In Service", "P", "N", "ΣWQ", "WQ*", "ΣTS", "TS*", "∫Q", "∫B"};
        dtm1.setColumnIdentifiers(header1);
        tbl1.setModel(dtm1);

        for (int i = 0; i < log1.size(); i++) {
            dtm1.addRow(new Object[]{log1.get(i).getCnum(), log1.get(i).getTime(), log1.get(i).getEventType(), log1.get(i).getQt(), log1.get(i).getBt(),
                log1.get(i).getInQueue(), log1.get(i).getInService(), log1.get(i).getP(), log1.get(i).getN(),
                log1.get(i).getTotalWQ(), log1.get(i).getWqMax(), log1.get(i).getTotalTS(), log1.get(i).getTsMax(),
                log1.get(i).getTotalAreaQ(), log1.get(i).getTotalAreaB()
            });
        }
        JTable tbl2 = new JTable();
        tbl2.setDefaultRenderer(Object.class, new StripedRowTableCellRenderer());

        DefaultTableModel dtm2 = new DefaultTableModel(0, 0);
        String header2[] = new String[]{"Cust#", "Time", "Event", "Q(t)", "B(t)", "In Queue", "In Service", "P", "N", "ΣWQ", "WQ*", "ΣTS", "TS*", "∫Q", "∫B"};
        dtm2.setColumnIdentifiers(header1);
        tbl2.setModel(dtm2);
        for (int i = 0; i < log2.size(); i++) {
            dtm2.addRow(new Object[]{log2.get(i).getCnum(), log2.get(i).getTime(), log2.get(i).getEventType(), log2.get(i).getQt(), log2.get(i).getBt(),
                log2.get(i).getInQueue(), log2.get(i).getInService(), log2.get(i).getP(), log2.get(i).getN(),
                log2.get(i).getTotalWQ(), log2.get(i).getWqMax(), log2.get(i).getTotalTS(), log2.get(i).getTsMax(),
                log2.get(i).getTotalAreaQ(), log2.get(i).getTotalAreaB()
            });
        }
        JTable tbl3 = new JTable();
        tbl3.setDefaultRenderer(Object.class, new StripedRowTableCellRenderer());
        DefaultTableModel dtm3 = new DefaultTableModel(0, 0);
        String header3[] = new String[]{"Cust#", "Time", "Event", "Q(t)", "B(t)", "In Queue", "In Service", "P", "N", "ΣWQ", "WQ*", "ΣTS", "TS*", "∫Q", "∫B"};
        dtm3.setColumnIdentifiers(header3);
        tbl3.setModel(dtm3);
        for (int i = 0; i < log3.size(); i++) {
            dtm3.addRow(new Object[]{log3.get(i).getCnum(), log3.get(i).getTime(), log3.get(i).getEventType(), log3.get(i).getQt(), log3.get(i).getBt(),
                log3.get(i).getInQueue(), log3.get(i).getInService(), log3.get(i).getP(), log3.get(i).getN(),
                log3.get(i).getTotalWQ(), log3.get(i).getWqMax(), log3.get(i).getTotalTS(), log3.get(i).getTsMax(),
                log3.get(i).getTotalAreaQ(), log3.get(i).getTotalAreaB()
            });
        }
        JTable tbl4 = new JTable();
        tbl4.setDefaultRenderer(Object.class, new StripedRowTableCellRenderer());

        DefaultTableModel dtm4 = new DefaultTableModel(0, 0);
        String header4[] = new String[]{"Cust#", "Time", "Event", "Q(t)", "B(t)", "In Queue", "In Service", "P", "N", "ΣWQ", "WQ*", "ΣTS", "TS*", "∫Q", "∫B"};
        dtm4.setColumnIdentifiers(header4);
        tbl4.setModel(dtm4);
        for (int i = 0; i < log4.size(); i++) {
            dtm4.addRow(new Object[]{log4.get(i).getCnum(), log4.get(i).getTime(), log4.get(i).getEventType(), log4.get(i).getQt(), log4.get(i).getBt(),
                log4.get(i).getInQueue(), log4.get(i).getInService(), log4.get(i).getP(), log4.get(i).getN(),
                log4.get(i).getTotalWQ(), log4.get(i).getWqMax(), log4.get(i).getTotalTS(), log4.get(i).getTsMax(),
                log4.get(i).getTotalAreaQ(), log4.get(i).getTotalAreaB()
            });
        }
        JTable tbl5 = new JTable();
        tbl5.setDefaultRenderer(Object.class, new StripedRowTableCellRenderer());

        DefaultTableModel dtm5 = new DefaultTableModel(0, 0);
        String header5[] = new String[]{"Cust#", "Time", "Event", "Q(t)", "B(t)", "In Queue", "In Service", "P", "N", "ΣWQ", "WQ*", "ΣTS", "TS*", "∫Q", "∫B"};
        dtm5.setColumnIdentifiers(header5);
        tbl5.setModel(dtm5);
        for (int i = 0; i < log5.size(); i++) {
            dtm5.addRow(new Object[]{log5.get(i).getCnum(), log5.get(i).getTime(), log5.get(i).getEventType(), log5.get(i).getQt(), log5.get(i).getBt(),
                log5.get(i).getInQueue(), log5.get(i).getInService(), log5.get(i).getP(), log5.get(i).getN(),
                log5.get(i).getTotalWQ(), log5.get(i).getWqMax(), log5.get(i).getTotalTS(), log5.get(i).getTsMax(),
                log5.get(i).getTotalAreaQ(), log5.get(i).getTotalAreaB()
            });
        }

        tbl1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbl2.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbl3.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbl4.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbl5.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        tbl1.setSelectionBackground(new java.awt.Color(255, 178, 109));
        tbl2.setSelectionBackground(new java.awt.Color(255, 178, 109));
        tbl3.setSelectionBackground(new java.awt.Color(255, 178, 109));
        tbl4.setSelectionBackground(new java.awt.Color(255, 178, 109));
        tbl5.setSelectionBackground(new java.awt.Color(255, 178, 109));

        JPanel p1 = new JPanel();
        JPanel p2 = new JPanel();
        JPanel p3 = new JPanel();
        JPanel p4 = new JPanel();
        JPanel p5 = new JPanel();
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new java.awt.Color(255, 255, 255));
        tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        simulatePanel.add(tabbedPane, BorderLayout.CENTER);

        p1.setLayout(new BorderLayout());
        p1.add(new JScrollPane(tbl1), BorderLayout.CENTER);
        p2.setLayout(new BorderLayout());
        p2.add(new JScrollPane(tbl2), BorderLayout.CENTER);
        p3.setLayout(new BorderLayout());
        p3.add(new JScrollPane(tbl3), BorderLayout.CENTER);
        p4.setLayout(new BorderLayout());
        p4.add(new JScrollPane(tbl4), BorderLayout.CENTER);
        p5.setLayout(new BorderLayout());
        p5.add(new JScrollPane(tbl5), BorderLayout.CENTER);

        tabbedPane.addTab("Replication 1", p1);
        tabbedPane.addTab("Replication 2", p2);
        tabbedPane.addTab("Replication 3", p3);
        tabbedPane.addTab("Replication 4", p4);
        tabbedPane.addTab("Replication 5", p5);
    }

    public void displayPerformanceMeasures(ArrayList<ReplicationCustomer> r1Log, ArrayList<ReplicationCustomer> r2Log, ArrayList<ReplicationCustomer> r3Log, ArrayList<ReplicationCustomer> r4Log, ArrayList<ReplicationCustomer> r5Log) {
        NumberFormat formatter = new DecimalFormat("#0.0000");
        //JTable table = new JTable(new DefaultTableModel(new Object[]{"Column1","Column2"}));
        Object rowData[][] = {{"Average Waiting Time", formatter.format((double) r1Log.get(r1Log.size() - 1).getTotalWQ() / r1Log.get(r1Log.size() - 1).getN()),
            formatter.format((double) r2Log.get(r2Log.size() - 1).getTotalWQ() / r2Log.get(r2Log.size() - 1).getN()),
            formatter.format((double) r3Log.get(r3Log.size() - 1).getTotalWQ() / r3Log.get(r3Log.size() - 1).getN()),
            formatter.format((double) r4Log.get(r4Log.size() - 1).getTotalWQ() / r4Log.get(r4Log.size() - 1).getN()),
            formatter.format((double) r5Log.get(r5Log.size() - 1).getTotalWQ() / r5Log.get(r5Log.size() - 1).getN())},
        {"Maximum Waiting Time", r1Log.get(r1Log.size() - 1).getWqMax(),
            r2Log.get(r2Log.size() - 1).getWqMax(),
            r3Log.get(r3Log.size() - 1).getWqMax(),
            r4Log.get(r4Log.size() - 1).getWqMax(),
            r5Log.get(r5Log.size() - 1).getWqMax()},
        {"Time-Average # of custs in queue", formatter.format(r1Log.get(r1Log.size() - 1).getTotalAreaQ() / SIMULATION_TIME),
            formatter.format(r2Log.get(r2Log.size() - 1).getTotalAreaQ() / SIMULATION_TIME),
            formatter.format(r3Log.get(r3Log.size() - 1).getTotalAreaQ() / SIMULATION_TIME),
            formatter.format(r4Log.get(r4Log.size() - 1).getTotalAreaQ() / SIMULATION_TIME),
            formatter.format(r5Log.get(r5Log.size() - 1).getTotalAreaQ() / SIMULATION_TIME)},
        {"Maximum # of custs in queue", maxQt1, maxQt2, maxQt3, maxQt4, maxQt5},
        {"Average and Maximum total time in system", formatter.format((double) r1Log.get(r1Log.size() - 1).getTotalTS() / r1Log.get(r1Log.size() - 1).getP()),
            formatter.format((double) r2Log.get(r2Log.size() - 1).getTotalTS() / r2Log.get(r2Log.size() - 1).getP()),
            formatter.format((double) r3Log.get(r3Log.size() - 1).getTotalTS() / r3Log.get(r3Log.size() - 1).getP()),
            formatter.format((double) r4Log.get(r4Log.size() - 1).getTotalTS() / r4Log.get(r4Log.size() - 1).getP()),
            formatter.format((double) r5Log.get(r5Log.size() - 1).getTotalTS() / r5Log.get(r5Log.size() - 1).getP())},
        {"Utilization of the resource", formatter.format(r1Log.get(r1Log.size() - 1).getTotalAreaB() / SIMULATION_TIME),
            formatter.format(r2Log.get(r2Log.size() - 1).getTotalAreaB() / SIMULATION_TIME),
            formatter.format(r3Log.get(r3Log.size() - 1).getTotalAreaB() / SIMULATION_TIME),
            formatter.format(r4Log.get(r4Log.size() - 1).getTotalAreaB() / SIMULATION_TIME),
            formatter.format(r5Log.get(r5Log.size() - 1).getTotalAreaB() / SIMULATION_TIME)}
        };

        Object columnNames[] = {"Performance Measures", "Replication 1", "Replication 2", "Replication 3", "Replication 4", "Replication 5"};
        JTable table = new JTable(rowData, columnNames);
        table.setDefaultRenderer(Object.class, new StripedRowTableCellRenderer());

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setSelectionBackground(new java.awt.Color(255, 178, 109));
        performancePanel.add(new JScrollPane(table));

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bg = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        sidePanel = new javax.swing.JPanel();
        performanceTables = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        dataTables = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        simulationTables = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        close = new javax.swing.JPanel();
        dynamicPanel = new javax.swing.JPanel();
        dataPanel = new javax.swing.JPanel();
        simulatePanel = new javax.swing.JPanel();
        performancePanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        setUndecorated(true);

        bg.setBackground(new java.awt.Color(255, 255, 255));
        bg.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setFont(new java.awt.Font("Arial Narrow", 0, 24)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(254, 202, 87));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("X");
        bg.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 0, 34, -1));

        sidePanel.setBackground(new java.awt.Color(255, 159, 67));
        sidePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        performanceTables.setBackground(new java.awt.Color(253, 139, 30));
        performanceTables.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        performanceTables.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                performanceTablesMousePressed(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/simulation/images/performance_25.png"))); // NOI18N
        jLabel1.setMaximumSize(new java.awt.Dimension(96, 96));
        jLabel1.setMinimumSize(new java.awt.Dimension(96, 96));
        jLabel1.setPreferredSize(new java.awt.Dimension(96, 96));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Performance Measures");

        javax.swing.GroupLayout performanceTablesLayout = new javax.swing.GroupLayout(performanceTables);
        performanceTables.setLayout(performanceTablesLayout);
        performanceTablesLayout.setHorizontalGroup(
            performanceTablesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(performanceTablesLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        performanceTablesLayout.setVerticalGroup(
            performanceTablesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(performanceTablesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        sidePanel.add(performanceTables, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 180, 190, 40));

        dataTables.setBackground(new java.awt.Color(255, 178, 109));
        dataTables.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        dataTables.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                dataTablesMousePressed(evt);
            }
        });

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/simulation/images/data_25.png"))); // NOI18N
        jLabel3.setMaximumSize(new java.awt.Dimension(96, 96));
        jLabel3.setMinimumSize(new java.awt.Dimension(96, 96));
        jLabel3.setPreferredSize(new java.awt.Dimension(96, 96));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Data Tables");

        javax.swing.GroupLayout dataTablesLayout = new javax.swing.GroupLayout(dataTables);
        dataTables.setLayout(dataTablesLayout);
        dataTablesLayout.setHorizontalGroup(
            dataTablesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataTablesLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(29, Short.MAX_VALUE))
        );
        dataTablesLayout.setVerticalGroup(
            dataTablesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(dataTablesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        sidePanel.add(dataTables, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 100, 190, 40));

        simulationTables.setBackground(new java.awt.Color(253, 139, 30));
        simulationTables.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        simulationTables.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                simulationTablesMousePressed(evt);
            }
        });

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/simulation/images/simulate_25.png"))); // NOI18N
        jLabel5.setMaximumSize(new java.awt.Dimension(96, 96));
        jLabel5.setMinimumSize(new java.awt.Dimension(96, 96));
        jLabel5.setPreferredSize(new java.awt.Dimension(96, 96));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Simulation Tables");

        javax.swing.GroupLayout simulationTablesLayout = new javax.swing.GroupLayout(simulationTables);
        simulationTables.setLayout(simulationTablesLayout);
        simulationTablesLayout.setHorizontalGroup(
            simulationTablesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(simulationTablesLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(29, Short.MAX_VALUE))
        );
        simulationTablesLayout.setVerticalGroup(
            simulationTablesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(simulationTablesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        sidePanel.add(simulationTables, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 140, 190, 40));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Simulation");
        sidePanel.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 170, 40));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Simple Processing System");
        sidePanel.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, -1));

        jSeparator1.setForeground(new java.awt.Color(254, 202, 87));
        jSeparator1.setPreferredSize(new java.awt.Dimension(50, 5));
        sidePanel.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 75, 180, 10));

        bg.add(sidePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 190, 490));

        jPanel1.setBackground(new java.awt.Color(254, 202, 87));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        bg.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 30, 730, 90));

        close.setBackground(new java.awt.Color(255, 255, 255));
        close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                closeMousePressed(evt);
            }
        });

        javax.swing.GroupLayout closeLayout = new javax.swing.GroupLayout(close);
        close.setLayout(closeLayout);
        closeLayout.setHorizontalGroup(
            closeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );
        closeLayout.setVerticalGroup(
            closeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 29, Short.MAX_VALUE)
        );

        bg.add(close, new org.netbeans.lib.awtextra.AbsoluteConstraints(876, 0, 40, -1));

        dynamicPanel.setLayout(new java.awt.CardLayout());

        dataPanel.setBackground(new java.awt.Color(254, 202, 87));
        dataPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                dataPanelMousePressed(evt);
            }
        });
        dataPanel.setLayout(new javax.swing.BoxLayout(dataPanel, javax.swing.BoxLayout.LINE_AXIS));
        dynamicPanel.add(dataPanel, "card2");

        simulatePanel.setBackground(new java.awt.Color(254, 202, 87));
        simulatePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                simulatePanelMousePressed(evt);
            }
        });
        simulatePanel.setLayout(new javax.swing.BoxLayout(simulatePanel, javax.swing.BoxLayout.LINE_AXIS));
        dynamicPanel.add(simulatePanel, "card2");

        performancePanel.setBackground(new java.awt.Color(254, 202, 87));
        performancePanel.setLayout(new javax.swing.BoxLayout(performancePanel, javax.swing.BoxLayout.LINE_AXIS));
        dynamicPanel.add(performancePanel, "card2");

        bg.add(dynamicPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 80, 730, 410));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bg, javax.swing.GroupLayout.PREFERRED_SIZE, 913, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bg, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMousePressed
        // TODO add your handling code here:
        int confirmed = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to exit?", "Exit Program",
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }//GEN-LAST:event_closeMousePressed

    private void dataTablesMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dataTablesMousePressed
        // TODO add your handling code here:

        setColor(dataTables);
        resetColor(performanceTables);
        resetColor(simulationTables);

        dataPanel.removeAll();
        dataPanel.repaint();
        dataPanel.revalidate();

        dynamicPanel.removeAll();
        dynamicPanel.repaint();
        dynamicPanel.revalidate();

        dynamicPanel.add(dataPanel);
        dynamicPanel.repaint();
        dynamicPanel.revalidate();
        displayTable(r1, r2, r3, r4, r5);
        System.out.println("Data Table Pressed");

    }//GEN-LAST:event_dataTablesMousePressed

    private void simulationTablesMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_simulationTablesMousePressed
        // TODO add your handling code here:

        setColor(simulationTables);
        resetColor(performanceTables);
        resetColor(dataTables);

        simulatePanel.removeAll();
        simulatePanel.repaint();
        simulatePanel.revalidate();

        dynamicPanel.removeAll();
        dynamicPanel.repaint();
        dynamicPanel.revalidate();

        dynamicPanel.add(simulatePanel);
        dynamicPanel.repaint();
        dynamicPanel.revalidate();
        displaySimulationTable(r1Log, r2Log, r3Log, r4Log, r5Log);

        System.out.print("Simulate Table Pressed");
    }//GEN-LAST:event_simulationTablesMousePressed

    private void performanceTablesMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_performanceTablesMousePressed
        // TODO add your handling code here:

        setColor(performanceTables);
        resetColor(simulationTables);
        resetColor(dataTables);

        performancePanel.removeAll();
        performancePanel.repaint();
        performancePanel.revalidate();

        dynamicPanel.removeAll();
        dynamicPanel.repaint();
        dynamicPanel.revalidate();

        dynamicPanel.add(performancePanel);
        dynamicPanel.repaint();
        dynamicPanel.revalidate();
        displayPerformanceMeasures(r1Log, r2Log, r3Log, r4Log, r5Log);
        System.out.print("Performance Table Pressed");
    }//GEN-LAST:event_performanceTablesMousePressed

    void setColor(JPanel panel) {
        panel.setBackground(new Color(255, 178, 109));
    }

    void resetColor(JPanel panel) {
        panel.setBackground(new Color(253, 139, 30));
    }

    private void dataPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dataPanelMousePressed
        // TODO add your handling code here:

    }//GEN-LAST:event_dataPanelMousePressed

    private void simulatePanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_simulatePanelMousePressed
        // TODO add your handling code here:

    }//GEN-LAST:event_simulatePanelMousePressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SimulationGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SimulationGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SimulationGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SimulationGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SimulationGUI().setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bg;
    private javax.swing.JPanel close;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JPanel dataTables;
    private javax.swing.JPanel dynamicPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel performancePanel;
    private javax.swing.JPanel performanceTables;
    private javax.swing.JPanel sidePanel;
    private javax.swing.JPanel simulatePanel;
    private javax.swing.JPanel simulationTables;
    // End of variables declaration//GEN-END:variables
}

class Customer {

    private int number;
    private int randomInterArrival;
    private int randomService;
    private int interArrivalTime;
    private int arrivalTime;
    private int serviceTime;

    public Customer(int n, int a, int ra, int iat, int rs, int lt) {
        number = n;
        randomInterArrival = ra;
        randomService = rs;
        arrivalTime = a;
        interArrivalTime = iat;
        serviceTime = lt;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getInterArrivalTime() {
        return interArrivalTime;
    }

    public void setInterArrivalTime(int interArrivalTime) {
        this.interArrivalTime = interArrivalTime;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(int serviceTime) {
        this.serviceTime = serviceTime;
    }

    public String toString() {
        System.out.println("");
        System.out.printf("%-12s%-12s%-20s%-12s", number, arrivalTime, interArrivalTime, serviceTime);
        return "";
    }

    public int getRandomInterArrival() {
        return randomInterArrival;
    }

    public void setRandomInterArrival(int randomInterArrival) {
        this.randomInterArrival = randomInterArrival;
    }

    public int getRandomService() {
        return randomService;
    }

    public void setRandomService(int randomService) {
        this.randomService = randomService;
    }
}

class Replication {

    private ArrayList<Integer> numbers;
    private ArrayList<Integer> randomInter;
    private ArrayList<Integer> interArrivalTimes;
    private ArrayList<Integer> arrivalTimes;
    private ArrayList<Integer> serviceTimes;
    private ArrayList<Integer> randomService;
    private ArrayList<Customer> customers;
    private ArrayList<ReplicationCustomer> replication;

    public Replication(int custNum) {
        numbers = new ArrayList<Integer>();
        customers = new ArrayList<Customer>();
        for (int i = 1; i <= custNum; i++) {
            numbers.add(i);
        }

        randomInter = generateRandomInterArrival(custNum);
        interArrivalTimes = convertInterArrivalTime(randomInter);
        randomService = generateRandomService(custNum);
        serviceTimes = convertServiceTime(randomService);
        arrivalTimes = computeArrivalTime();
        customers = generateEntities();
    }

    public Replication(ArrayList<Integer> number, ArrayList<Integer> interArrivalTime, ArrayList<Integer> serviceTime) {
        this.numbers = number;
        this.interArrivalTimes = interArrivalTime;
        this.serviceTimes = serviceTime;
    }

    public ArrayList<Integer> generateRandomInterArrival(int n) {
        ArrayList<Integer> randomInter = new ArrayList<Integer>();
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            randomInter.add(rand.nextInt(1000));
        }
        return randomInter;
    }

    /**
     * Generate random digit assignment for service time
     *
     * @param n
     */
    public ArrayList<Integer> generateRandomService(int n) {
        ArrayList<Integer> randomService = new ArrayList<Integer>();
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            randomService.add(rand.nextInt(100));
        }
        return randomService;
    }

    public void initialize() {
        convertInterArrivalTime(interArrivalTimes);
        convertServiceTime(serviceTimes);
        computeArrivalTime();

    }

    public ArrayList<Customer> generateEntities() {
        for (int i = 0; i < numbers.size(); i++) {
            customers.add(new Customer(numbers.get(i), arrivalTimes.get(i), randomInter.get(i),
                    interArrivalTimes.get(i), randomService.get(i), serviceTimes.get(i)));
        }
        return customers;
    }

    public ArrayList<Integer> computeArrivalTime() {
        ArrayList<Integer> arrT = new ArrayList<Integer>();
        arrT.add(0);
        for (int i = 1; i < numbers.size(); i++) {
            arrT.add(arrT.get(i - 1) + interArrivalTimes.get(i - 1));
        }
        return arrT;
    }

    public ArrayList<Integer> convertInterArrivalTime(ArrayList<Integer> randomInterArrival) {
        ArrayList<Integer> interArrival = new ArrayList<Integer>();

        for (int i = 0; i < randomInterArrival.size(); i++) {
            if (randomInterArrival.get(i) >= 1 && randomInterArrival.get(i) <= 125) {
                interArrival.add(1);
            } else if (randomInterArrival.get(i) >= 126 && randomInterArrival.get(i) <= 250) {
                interArrival.add(2);
            } else if (randomInterArrival.get(i) >= 251 && randomInterArrival.get(i) <= 375) {
                interArrival.add(3);
            } else if (randomInterArrival.get(i) >= 376 && randomInterArrival.get(i) <= 500) {
                interArrival.add(4);
            } else if (randomInterArrival.get(i) >= 501 && randomInterArrival.get(i) <= 625) {
                interArrival.add(5);
            } else if (randomInterArrival.get(i) >= 626 && randomInterArrival.get(i) <= 750) {
                interArrival.add(6);
            } else if (randomInterArrival.get(i) >= 751 && randomInterArrival.get(i) <= 875) {
                interArrival.add(7);
            } else if (randomInterArrival.get(i) >= 876 || randomInterArrival.get(i) == 0) {
                interArrival.add(8);
            }
        }
        return interArrival;
    }

    public ArrayList<Integer> convertServiceTime(ArrayList<Integer> randomService) {
        ArrayList<Integer> serviceTime = new ArrayList<Integer>();

        for (int i = 0; i < randomService.size(); i++) {
            if (randomService.get(i) >= 1 && randomService.get(i) <= 10) {
                serviceTime.add(1);
            } else if (randomService.get(i) >= 11 && randomService.get(i) <= 30) {
                serviceTime.add(2);
            } else if (randomService.get(i) >= 31 && randomService.get(i) <= 60) {
                serviceTime.add(3);
            } else if (randomService.get(i) >= 61 && randomService.get(i) <= 85) {
                serviceTime.add(4);
            } else if (randomService.get(i) >= 86 && randomService.get(i) <= 95) {
                serviceTime.add(5);
            } else if (randomService.get(i) >= 96 || randomService.get(i) == 0) {
                serviceTime.add(6);
            }
        }
        return serviceTime;
    }

    public ArrayList<Integer> getNumber() {
        return numbers;
    }

    public void setNumber(ArrayList<Integer> number) {
        this.numbers = number;
    }

    public ArrayList<Integer> getInterArrivalTime() {
        return interArrivalTimes;
    }

    public void setInterArrivalTime(ArrayList<Integer> interArrivalTime) {
        this.interArrivalTimes = interArrivalTime;
    }

    public ArrayList<Integer> getArrivalTime() {
        return arrivalTimes;
    }

    public void setArrivalTime(ArrayList<Integer> arrivalTime) {
        this.arrivalTimes = arrivalTime;
    }

    public ArrayList<Integer> getServiceTime() {
        return serviceTimes;
    }

    public void setServiceTime(ArrayList<Integer> serviceTime) {
        this.serviceTimes = serviceTime;
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(ArrayList<Customer> customers) {
        this.customers = customers;
    }

    public String toString() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTable tbl = new JTable();
        DefaultTableModel dtm = new DefaultTableModel(0, 0);
        String header[] = new String[]{"Customer", "RDA Interarrival", "Interarrival", "Arrival", "RDA Service", "Service"};
        dtm.setColumnIdentifiers(header);
        tbl.setModel(dtm);
        for (int i = 0; i < numbers.size(); i++) {
            dtm.addRow(new Object[]{numbers.get(i), randomInter.get(i), interArrivalTimes.get(i),
                arrivalTimes.get(i), randomService.get(i), serviceTimes.get(i)
            });
        }

        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scrollPane = new JScrollPane(tbl);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setSize(1000, 350);
        frame.setVisible(true);

        return "";
    }

    public ArrayList<ReplicationCustomer> getReplication() {
        return replication;
    }

    public void setReplication(ArrayList<ReplicationCustomer> replication) {
        this.replication = replication;
    }

    public ArrayList<Integer> getRandomInter() {
        return randomInter;
    }

    public void setRandomInter(ArrayList<Integer> randomInter) {
        this.randomInter = randomInter;
    }

    public ArrayList<Integer> getRandomService() {
        return randomService;
    }

    public void setRandomService(ArrayList<Integer> randomService) {
        this.randomService = randomService;
    }
}

class ReplicationCustomer implements Comparator<ReplicationCustomer> {

    private int cnum;
    private int timeArr;
    private int timeDep;
    private int time;
    private int serveTime;
    private String eventType;
    private int qt;
    private int bt;
    private LinkedList<Integer> inQueue;
    private LinkedList<Integer> inService;
    private int p;
    private int n;
    private int wq;
    private int wqMax;
    private int ts;
    private int tsMax;
    private int totalWQ;
    private int totalTS;
    private int totalAreaQ;
    private int totalAreaB;

    public ReplicationCustomer(int cnum, int time, String eventType, int qt, int bt, LinkedList<Integer> inQueue,
            LinkedList<Integer> inService, int p, int n, int wq, int wqMax, int ts, int tsMax) {
        this.cnum = cnum;
        this.time = time;
        this.eventType = eventType;
        this.qt = qt;
        this.bt = bt;
        this.inQueue = inQueue;
        this.inService = inService;
        this.p = p;
        this.n = n;
        this.totalWQ = wq;
        this.wqMax = wqMax;
        this.totalTS = ts;
        this.tsMax = tsMax;
    }

    public ReplicationCustomer(int cnum, int time, String eventType, int qt, int bt, LinkedList<Integer> inQueue,
            LinkedList<Integer> inService, int p, int n, int wq, int wqMax, int ts, int tsMax, int aQ, int aB) {
        this.cnum = cnum;
        this.time = time;
        this.eventType = eventType;
        this.qt = qt;
        this.bt = bt;
        this.inQueue = inQueue;
        this.inService = inService;
        this.p = p;
        this.n = n;
        this.totalWQ = wq;
        this.wqMax = wqMax;
        this.totalTS = ts;
        this.tsMax = tsMax;
        this.totalAreaQ = aQ;
        this.totalAreaB = aB;
    }

    public ReplicationCustomer(int cnum, int timeArr, int timeDep, int serveTime) {
        this.cnum = cnum;
        this.timeArr = timeArr;
        this.timeDep = timeDep;
        this.serveTime = serveTime;
    }

    public ReplicationCustomer() {
        cnum = 0;
        time = 0;
        eventType = "";
        qt = 0;
        bt = 0;
        inQueue = new LinkedList<Integer>();
        inService = new LinkedList<Integer>();
        p = 0;
        n = 0;
        wq = 0;
        wqMax = 0;
        ts = 0;
        tsMax = 0;
    }

    public int compare(ReplicationCustomer rc1, ReplicationCustomer rc2) {
        return rc1.getTime() - rc2.getTime();
    }

    public int getCnum() {
        return cnum;
    }

    public void setCnum(int cnum) {
        this.cnum = cnum;
    }

    public int getTimeArr() {
        return timeArr;
    }

    public void setTimeArr(int timeArr) {
        this.timeArr = timeArr;
    }

    public int getTimeDep() {
        return timeDep;
    }

    public void setTimeDep(int timeDep) {
        this.timeDep = timeDep;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getServeTime() {
        return serveTime;
    }

    public void setServeTime(int serveTime) {
        this.serveTime = serveTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public int getQt() {
        return qt;
    }

    public void setQt(int qt) {
        this.qt = qt;
    }

    public int getBt() {
        return bt;
    }

    public void setBt(int bt) {
        this.bt = bt;
    }

    public LinkedList<Integer> getInQueue() {
        return inQueue;
    }

    public void setInQueue(LinkedList<Integer> inQueue) {
        this.inQueue = inQueue;
    }

    public LinkedList<Integer> getInService() {
        return inService;
    }

    public void setInService(LinkedList<Integer> inService) {
        this.inService = inService;
    }

    public int getP() {
        return p;
    }

    public void setP(int p) {
        this.p = p;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getWq() {
        return wq;
    }

    public void setWq(int wq) {
        this.wq = wq;
    }

    public int getWqMax() {
        return wqMax;
    }

    public void setWqMax(int wqMax) {
        this.wqMax = wqMax;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public int getTsMax() {
        return tsMax;
    }

    public void setTsMax(int tsMax) {
        this.tsMax = tsMax;
    }

    public int getTotalWQ() {
        return totalWQ;
    }

    public void setTotalWQ(int totalWQ) {
        this.totalWQ = totalWQ;
    }

    public int getTotalTS() {
        return totalTS;
    }

    public void setTotalTS(int totalTS) {
        this.totalTS = totalTS;
    }

    public int getTotalAreaQ() {
        return totalAreaQ;
    }

    public void setTotalAreaQ(int totalAreaQ) {
        this.totalAreaQ = totalAreaQ;
    }

    public int getTotalAreaB() {
        return totalAreaB;
    }

    public void setTotalAreaB(int totalAreaB) {
        this.totalAreaB = totalAreaB;
    }
}

class FrameDragListener extends MouseAdapter {

    private final JFrame frame;
    private Point mouseDownCompCoords = null;

    public FrameDragListener(JFrame frame) {
        this.frame = frame;
    }

    public void mouseReleased(MouseEvent e) {
        mouseDownCompCoords = null;
    }

    public void mousePressed(MouseEvent e) {
        mouseDownCompCoords = e.getPoint();
    }

    public void mouseDragged(MouseEvent e) {
        Point currCoords = e.getLocationOnScreen();
        frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
    }
}

class StripedRowTableCellRenderer extends DefaultTableCellRenderer {
    private static final Color STRIPE = new Color(0.929f, 0.953f, 0.996f);
    private static final Color WHITE = UIManager.getColor("Table.background");

    private final JCheckBox ckb = new JCheckBox();

    public StripedRowTableCellRenderer() {
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JComponent c = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (!isSelected) {
            if (row % 2 == 0) {
                c.setBackground(WHITE);
            } else {
                c.setBackground(STRIPE);
            }
        }

        if (value instanceof Boolean) {
            ckb.setSelected(((Boolean) value));
            ckb.setHorizontalAlignment(JLabel.CENTER);
            ckb.setBackground(super.getBackground());
            if (isSelected || hasFocus) {
                ckb.setBackground(table.getSelectionBackground());
            }
            return ckb;
        }

        return c;
    }

}
