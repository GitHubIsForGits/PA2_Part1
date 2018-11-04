package cs131.pa2.CarsTunnels;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import javafx.util.Pair;

public class PriorityScheduler extends Tunnel{
	private final Lock lock = new ReentrantLock(); 
	private final Condition prioCond = lock.newCondition();
	
	public Collection<Pair<Vehicle, Tunnel>> TunnelAndVehicle = new LinkedList();
	public Collection<Tunnel> TunnelList = new LinkedList();
	
	int maxWaitingPriority = 0;
	
	Boolean gottaWait(Vehicle vehicle) {
		return (vehicle.getPriority() < maxWaitingPriority);
	}

	
	

	public PriorityScheduler(String name, Collection<Tunnel> c) {
		super(name);
		TunnelList = (LinkedList<Tunnel>)c;
	}

	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		lock.lock();
		boolean result = false;
		try {
			while (gottaWait(vehicle)) {
				try {
					prioCond.await();
				} catch (InterruptedException e) {} 
			}
			
			for(Tunnel tunnel: TunnelList) {
				if(tunnel.tryToEnterInner(vehicle)) {
					TunnelAndVehicle.add(new Pair<Vehicle, Tunnel>(vehicle, tunnel));
					return true;
				}
				
			} 
			
			
			
		} finally {
			lock.unlock();
			return result;
		}
		
			
	}
		
		
		
		
		
		
		/*
		 * Check priority of every tunnel.
		 * If one of the tunnels has a priority =< the vehicles priority, call TargetTunnel.tryToEnterInner(true) then return true.
		 * If all the tunnels are high priority, make the vehicle wait
		 * When a tunnel empties, reset it's priority and wake all vehicles.
		 */
		
	
	
	
	
	
	
	
	

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		lock.lock();
		try {
			//exitTunnelInner(vehicle) on basictunnel
			prioCond.signalAll();
			maxWaitingPriority = vehicle.getPriority();
		} finally {
			
		}
		
	}
	
}
