import java.util.Random;
import java.util.Stack;

public class Process {

static int numOFProcess = 0;
	
	int id;
	double ExcpectedCpuBurst;
	String name;
	int size;
	int countCPU;
	double totalTimeCPU;
	int countIO;
	double totalTimeIO;
	double countWaiting;
	double start;
	double ending;
	String state;
	double cpuUtilization;
	double waitingTime;
	double turnAroundTime;
	boolean FinishedCpuBusrt;
	Stack<Double> cpu = new Stack<>();
	Stack<Double> io = new Stack<>();
	Stack<Double> memory = new Stack<>();
	
	
	public Process(String name) {

		id = ++numOFProcess;
		ExcpectedCpuBurst = 0;
		this.name = name;
		size = new Random().nextInt(100)+4;
		countCPU = 0;
		totalTimeCPU = 0;
		countIO = 0;
		totalTimeIO = 0;
		countWaiting = 0;
		start = 0;
		ending = 0;
		state = "New";
		cpuUtilization = 0;
		waitingTime = 0;
		turnAroundTime = 0;
		FinishedCpuBusrt= true;
	}

	public void fillSequence(String arr[]) {

		for (int i = arr.length-1; i>=1; i--) {
			if (arr[i].contains("c")) {
				arr[i] = arr[i].replace('c', '0');
				ExcpectedCpuBurst+=Double.parseDouble(arr[i]);
				cpu.add(Double.parseDouble(arr[i]));
			} else if (arr[i].contains("o")) {
				arr[i] = arr[i].replace('o', '0');
				if(Integer.parseInt(arr[i])!=0)
				io.add(Double.parseDouble(arr[i]));
			} else {
				if (i == arr.length - 1)
					io.push(Double.parseDouble(arr[i]));
				else
					memory.add(Double.parseDouble(arr[i]));
			}

		}
	}
}
