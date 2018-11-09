package cs131.pa2.CarsTunnels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Log;

public class PreemptivePriorityScheduler extends Tunnel{
	
	private final Lock lock = new ReentrantLock(); 
	private final Condition prioCond = lock.newCondition();
		
	public HashMap<Vehicle, Tunnel> VehicleAndTunnel = new HashMap();
	
	public ArrayList<Vehicle> prioWait = new ArrayList();
	
	public HashMap<Tunnel, Lock> tLockList = new HashMap<Tunnel, Lock>();
	public HashMap<Tunnel, Condition> tCondList = new HashMap<Tunnel, Condition>();
	
	
	int maxWaitingPriority = 0;
	
	public boolean gottaWait(Vehicle vehicle) {
		return (vehicle.getPriority() < maxWaitingPriority);
	}
	
	
	public boolean onWaitingList(Vehicle vehicle) {
		boolean answer = prioWait.contains(vehicle);
		return answer;
	}

	public PreemptivePriorityScheduler(String name, Collection<Tunnel> tunnels, Log log) {
		super(name);
		for(Tunnel t : tunnels) {
			Lock loq = new ReentrantLock();
			tLockList.put(t, loq);
			Condition loqCond = loq.newCondition();
			tCondList.put(t, loqCond);
		}
		//System.out.println("Everything created");
		//prioSched = new PriorityScheduler(name, tunnels, log);
		//use this to make the locks and map them to each lock
	}

	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		boolean entered = false;
		lock.lock();
		try {
			while(!entered){
				
			}
		} finally {
			lock.unlock();
		}
		return entered;
	}

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		boolean exitted = false;
		lock.lock();
		try {
			
		} finally {
			if(exitted) {prioCond.signalAll();}
			lock.unlock();
		}
	}
	
}

