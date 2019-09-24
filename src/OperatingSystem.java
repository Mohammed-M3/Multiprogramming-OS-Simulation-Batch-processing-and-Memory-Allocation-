import java.awt.Color;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class OperatingSystem extends Thread {

	// GUI Properties
	JFrame f1;
	JFrame f2;
	JLabel memory;
	JLabel memoryCounter;
	JTextArea RRQueueActions;
	JTextArea FCFSActions;
	JLabel RRQueuelbl;
	JLabel FCFSlbl;
	JLabel cpu;
	JTextArea cpuActions;
	
	// OS prop
	private double RAM_SIZE = 240;
	private double usedRam;
	private Scanner input;
	private BufferedWriter outPut;
	private Queue<Process> jobQueue;
	private Queue<Process> waitQueue;
	private Queue<Process> IOqueue;
	protected LinkedList<Process> end;
	protected int numberOFprocess;
	protected int numberOFprocessEntered;
	protected int clock;

	// MultiLevelQueue
	private Queue<Process> RRQueue;
	private final double QUANTOM = 5;
	private Queue<Process> FCFS;

	public OperatingSystem() {
		
		// initializing
		f1 = new JFrame("CSC227 PRoject");
		f2 = new JFrame("CSC227 PRoject");
		f1.setLayout(new GridLayout(2, 4));
		f1.setSize(640, 400);
		memoryCounter = new JLabel("0");
		memory = new JLabel("Memory");
		RRQueuelbl = new JLabel("RRQueue");
		FCFSlbl = new JLabel("FCFS");
		RRQueueActions = new JTextArea();
		FCFSActions = new JTextArea();
		JLabel cpu = new JLabel("CPU");
		cpuActions = new JTextArea();

		RRQueueActions.setEditable(false);
		FCFSActions.setEditable(false);
		cpuActions.setEditable(false);

		RRQueueActions.setBackground(Color.gray);
		FCFSActions.setBackground(Color.WHITE);
		cpuActions.setBackground(Color.GRAY);
		f1.getContentPane().add(memory);
		f1.getContentPane().add(RRQueuelbl);
		f1.getContentPane().add(FCFSlbl);
		f1.getContentPane().add(cpu);
		f1.getContentPane().add(memoryCounter);
		DefaultCaret caret1 = (DefaultCaret) RRQueueActions.getCaret();
		caret1.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		DefaultCaret caret2 = (DefaultCaret) FCFSActions.getCaret();
		caret2.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		DefaultCaret caret3 = (DefaultCaret) cpuActions.getCaret();
		caret3.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		f1.getContentPane().add(new JScrollPane(RRQueueActions));
		f1.getContentPane().add(new JScrollPane(FCFSActions));
		f1.getContentPane().add(new JScrollPane(cpuActions));
		f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f1.setVisible(true);

		usedRam = 0;
		jobQueue = new LinkedList<>();
		waitQueue = new LinkedList<>();
		RRQueue = new LinkedList<>();
		FCFS = new LinkedList<>();
		IOqueue = new LinkedList<>();
		end = new LinkedList<>();
		numberOFprocess = 0;
		numberOFprocessEntered = 0;
		clock = 0;
	}

	/*
	 * *********dealing with Files****************
	 */
	public void fill(String fileName) {

		try {
			input = new Scanner(new File(fileName));
			while (input.hasNextLine()) {
				String info[] = input.nextLine().split("   ");
				Process p = new Process(info[0]);
				p.fillSequence(info);
				jobQueue.add(p);
				numberOFprocess++;
			}

		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			input.close();
		}
	}

	public void write(String fileName, int num) {

		try {
			outPut = new BufferedWriter(new FileWriter(new File(fileName)));
			for (int i = 0; i < num; i++) {
				String actions[] = new String[13];
				int kind = 0;
				actions[0] = "Process";
				for (int y = 1; y < actions.length - 1; y++) {
					if (kind == 0) {
						Integer random1 = (new Random().nextInt(45) + 5);
						actions[y] = "c" + random1.toString();
						kind++;
					} else if (kind == 1) {
						int negativeOrPositive = new Random().nextInt(4);
						if (negativeOrPositive == 0) {
							Integer random1 = -(new Random().nextInt(1) + 1);
							actions[y] = random1.toString();
							kind++;
						} else {
							Integer random1 = (new Random().nextInt(15) + 1);
							actions[y] = random1.toString();
							kind++;
						}
					} else {
						Integer random1 = (new Random().nextInt(12) + 3);
						actions[y] = "o" + random1.toString();
						kind = 0;
					}
				}
				actions[actions.length - 1] = "-1";

				for (int y = 0; y < actions.length; y++) {
					outPut.write(actions[y]);
					if (y < actions.length - 1)
						outPut.write("   ");
				}
				if (i < num - 1)
					outPut.newLine();
			}
			outPut.close();

		} catch (Exception e) {

		}
	}
	
	

	/* **********************************
	 * ********avoid critical section****
	 ************************************/
	public synchronized void increaseUSedRam(double amount) {
		Double memory = usedRam + amount;
		memoryCounter.setText(memory.toString());
		usedRam += amount;
	}

	public synchronized void deccreaseUSedRam(double amount) {
		Double memory = usedRam - amount;
		memoryCounter.setText(memory.toString());
		usedRam -= amount;
	}

	public synchronized void increaseNumberOFProcessEntered() {
		numberOFprocessEntered++;
	}

	public synchronized void decreaseNumberOFProcessEntered() {
		numberOFprocessEntered--;
	}

	public synchronized void increaseNumberOFProcess() {
		numberOFprocess++;
	}

	public synchronized void decreaseNumberOFProcess() {
		numberOFprocess--;
	}

	public synchronized void insertToRRQueue(Process p) {
		RRQueueActions.append(p.name + p.id + " Has entered\n");
		try {
			sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		RRQueue.add(p);
	}

	public synchronized Process pollRRQueue() {
		Process p = RRQueue.poll();
		RRQueueActions.append(p.name + p.id + " Has Left\n");
		return p;
	}

	public synchronized void insertToFCFS(Process p) {
		FCFSActions.append(p.name + p.id + " Has entered\n");
		try {
			sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		FCFS.add(p);
	}

	public synchronized Process pollFCFS() {
		Process p = FCFS.poll();
		FCFSActions.append(p.name + p.id + " Has Left\n");
		return p;
	}

	public synchronized void addEnd(Process p) {
		end.add(p);
	}

	public synchronized void addWaitQueue(Process p) {
		waitQueue.add(p);
	}

	public synchronized Process pollWaitQueue() {
		return waitQueue.poll();
	}

	public synchronized void addIoQueue(Process p) {
		IOqueue.add(p);
	}

	public synchronized Process pollIoQueue() {
		return IOqueue.poll();
	}
	
	

	/* *****************************************
	 * ******Important Methods OF the OS********
	 *******************************************/

	public synchronized void upgrade() {
		if (FCFS.isEmpty())
			return;
		if (FCFS.size() - RRQueue.size() > 5) {
			Process p = pollFCFS();
			FCFSActions.append(p.name + p.id + " Has Upgraded\n");
			insertToRRQueue(p);
		}
	}

	public synchronized void demote() {
		if (RRQueue.isEmpty())
			return;
		if (RRQueue.size() - FCFS.size() > 5) {
			Process p = pollRRQueue();
			RRQueueActions.append(p.name + p.id + " Has Demoted\n");
			insertToFCFS(p);
		}
	}

	public synchronized void LongTermScheduler() {

		int count = jobQueue.size();
		int i = 0;
		while (i < count) {
			Process p = jobQueue.poll();
			double size = p.size;
			if (RAM_SIZE * 0.8 < usedRam + size) {
				jobQueue.add(p);
			} else {
				p.start = clock;
				p.state = "Ready";
				increaseUSedRam(size);
				increaseNumberOFProcessEntered();
				selectQueue(p);
			}
			i++;
		}

	}

	public synchronized void selectQueue(Process p) {

		if (p.FinishedCpuBusrt)
			insertToRRQueue(p);
		else {
			insertToFCFS(p);
		}
	}

	public synchronized void shortTermScheduler() {

		int i = 0;
		while (!RRQueue.isEmpty() && i < 4) {
			Process p = pollRRQueue();
			p.state = "Running";
			CPUburst(p, "RRQUEUE");
			i++;
		}

		if (!FCFS.isEmpty()) {
			Process p = pollFCFS();
			p.state = "Running";
			CPUburst(p, "FCFS");
		}

	}

	public synchronized void CPUburst(Process p, String queue) {

		cpuActions.append(p.name + p.id + " Has Entered\n");
		try {
			sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long time = 0;
		double cpuBurst = p.cpu.pop();

		if (queue.equals("RRQUEUE")) {
			double memory = 0;

			// does the memory belong to the cpuBusrt
			if (p.cpu.size() + 1 == p.memory.size()) {
				memory = p.memory.pop();
			}
			// more memory required
			if (RAM_SIZE > usedRam + memory) {
				increaseUSedRam(memory);
				p.size += memory;
			} else {
				p.memory.push(memory);
				p.cpu.push(cpuBurst);
				p.state = "Waiting";
				waitQueue.add(p);
				cpuActions.append(p.name + p.id + " Has Left\n");
				return;
			}
			//Burst
			while (time < QUANTOM && time < cpuBurst) {
				time++;
			}
			
			cpuBurst -= time;
			p.totalTimeCPU += time;
		}
		// if it is FCFS Queue
		else {

			double memory = 0;
			// does the memory belong to the cpuBusrt?
			if (p.cpu.size() + 1 == p.memory.size()) {
				memory = p.memory.pop();
			}
			// more memory required
			if (RAM_SIZE > usedRam + memory) {
				increaseUSedRam(memory);
				p.size += memory;
			} else {
				p.memory.push(memory);
				p.cpu.push(cpuBurst);
				p.state = "Waiting";
				waitQueue.add(p);
				cpuActions.append(p.name + p.id + " Has Left\n");
				return;
			}
			
			//Burst
			while (time < cpuBurst)
				time++;

			
			
			p.totalTimeCPU += time;
			cpuBurst -= time;
		}
		

		// adding the wait time to all processes
		for (Process Process : RRQueue) {
			Process.ending += time;
		}

		for (Process Process : FCFS) {
			Process.ending += time;
		}

		
		cpuActions.append(p.name + p.id + " Has Left\n");
		p.ending += time;
		clock += time;
		p.countCPU++;
		if (cpuBurst == 0) {
			double last = p.io.pop();
			//finished
			if (last == -1) {
				p.state = "Terminated";
				deccreaseUSedRam(p.size);
				decreaseNumberOFProcessEntered();
				decreaseNumberOFProcess();
				p.turnAroundTime = p.ending;
				p.waitingTime = p.turnAroundTime - p.totalTimeCPU;
				addEnd(p);
			} 
			//needs IO
			else {
				p.FinishedCpuBusrt = true;
				p.io.push(last);
				p.state = "Waiting";
				addIoQueue(p);
			}
		} 
		// cpuBurst not finished
		else {
			p.FinishedCpuBusrt = false;
			p.cpu.push(cpuBurst);
			p.state = "Ready";
			selectQueue(p);
		}

	}

	public synchronized void IOburst() {

		if (!IOqueue.isEmpty()) {
			double time = 0;
			Process p = pollIoQueue();
			double ioBurst = p.io.pop();
			while (time < ioBurst) {
				time++;
			}
			// adding the wait time to all processes
			for (Process Process : IOqueue) {
				Process.ending += time;
			}
			
			p.ending += time;
			p.totalTimeIO += time;
			p.countIO++;
			p.state = "Ready";
			selectQueue(p);
		}
	}

	public synchronized void waiting() {
		
		int size = waitQueue.size();
		int i = 0;
		
		while (i < size) {
			Process p = pollWaitQueue();
			double memory = p.memory.pop();
			if (RAM_SIZE > usedRam + memory) {
				p.state = "Ready";
				p.size += memory;
				increaseUSedRam(memory);
				p.countWaiting++;
				selectQueue(p);
			} else {
				p.memory.push(memory);
				addWaitQueue(p);
			}
			// adding the wait time to all processes
			for (Process Process : waitQueue) {
				Process.ending += 0.0001;
			}
			i++;
		}
		
		
		if (waitQueue.size() == numberOFprocessEntered && numberOFprocessEntered != 0) {
			deadLock();
		}
	}

	private void deadLock() {
		Process kill = null;
		int size = waitQueue.size();
		for (int i = 0; i < size; i++) {
			Process p = pollWaitQueue();
			if (kill == null || kill.size < p.size) {
				kill = p;
			}
			addWaitQueue(p);
		}
		
		kill.countWaiting++;
		deccreaseUSedRam(kill.size);
		kill.state = "Killed";
		decreaseNumberOFProcessEntered();
		decreaseNumberOFProcess();
		kill.turnAroundTime = ((kill.ending + kill.start) - kill.start);
		kill.waitingTime = kill.turnAroundTime - kill.totalTimeCPU;
		waitQueue.remove(kill);
		addEnd(kill);
		JOptionPane.showMessageDialog(new JFrame(), kill.name + kill.id + " has been killed", "kill!!",
				JOptionPane.ERROR_MESSAGE);
	}

	public void run() {

		while (!jobQueue.isEmpty()) {
			LongTermScheduler();
			try {
				sleep(100);

			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	// **********************************************************
	// ***********************Main*******************************
	// **********************************************************

	public static void main(String args[]) throws InterruptedException, IOException, Exception {

		OperatingSystem os = new OperatingSystem();

		int num = Integer.parseInt(JOptionPane.showInputDialog("Enter Number of processes"));
		os.write("files.txt", num);
		os.fill("files.txt");

		os.start();

		Thread UpgradingAndDemoting = new Thread(new Runnable() {

			@Override
			public void run() {
				while (os.numberOFprocess != 0) {
					os.upgrade();
					os.demote();
				}
			}
		});
		Thread Waiting = new Thread(new Runnable() {

			@Override
			public void run() {
				while (os.numberOFprocess != 0)
					os.waiting();
			}
		});

		Thread IO = new Thread(new Runnable() {

			@Override
			public void run() {
				while (os.numberOFprocess != 0)
					os.IOburst();
			}
		});

		UpgradingAndDemoting.start();
		Waiting.start();
		IO.start();

		// shortTermScheduler
		while (os.numberOFprocess != 0) {
			os.shortTermScheduler();
		}

		// computing cpuUtilization for each process
		for (Process p : os.end) {
			p.cpuUtilization = (p.totalTimeCPU / os.clock) * 100;
		}

		// printing the results on a file
		double TTRavg = 0;
		double waitAvg = 0;
		int size = os.end.size();
		os.outPut = new BufferedWriter(new FileWriter(new File("Result.txt")));
		for (Process p : os.end) {
			TTRavg += p.turnAroundTime;
			waitAvg += p.waitingTime;
			String result = p.name + p.id + "   " + p.start + "   " + p.countCPU + "   " + p.totalTimeCPU + "   "
					+ p.countIO + "   " + p.totalTimeIO + "   " + p.countWaiting + "   " + (long) p.ending + "   "
					+ p.state + "   " + p.cpuUtilization + "%   " + (long) p.waitingTime + "   "
					+ (long) p.turnAroundTime;
			os.outPut.write(result);
			os.outPut.newLine();
		}

		os.outPut.write("AVG wait = " + waitAvg / size + " : AVG TTR = " + TTRavg / size);
		os.outPut.close();

		JOptionPane.showMessageDialog(new JFrame(), "Finished", "Done!!", JOptionPane.OK_CANCEL_OPTION);
	}

}
