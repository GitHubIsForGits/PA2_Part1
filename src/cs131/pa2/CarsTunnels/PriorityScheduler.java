package cs131.pa2.CarsTunnels;

import java.util.ArrayList;
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
	private final Condition prioCond = lock.newCondition();//vehicle was at the highest priority
	private final Condition lowPrioCond = lock.newCondition();
	
	public ArrayList<Pair<Vehicle, Tunnel>> TunnelAndVehicle = new ArrayList();
	public Collection<Tunnel> TunnelList = new ArrayList();
	public Collection<Vehicle> maxPrioList = new PriorityQueue();
	
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
		if(vehicle.getPriority() >= maxWaitingPriority) {
			for(Tunnel tunnel: TunnelList) {
				if(tunnel.tryToEnterInner(vehicle)) {
					TunnelAndVehicle.add(new Pair<Vehicle, Tunnel>(vehicle, tunnel));
					return true;	
				}	
			}
			try {
				maxWaitingPriority = vehicle.getPriority();
				maxPrioList.add(vehicle);
				prioCond.await();
			} catch (InterruptedException e) {}
		}
		
		
		
		/*
		 * while(!gottaWait(vehicle)) {
		 * for(Tunnel tunnel: TunnelList) {
				if(tunnel.tryToEnterInner(vehicle)) {
					TunnelAndVehicle.add(new Pair<Vehicle, Tunnel>(vehicle, tunnel));
					return true;	
				}	
			}
			try {
				prioCond.await();
			} catch (InterruptedException e) {} 
		}
		 */
			
		while(gottaWait(vehicle)) {
			try {
				prioCond.await();
			} catch (InterruptedException e) {}
		}
				

		lock.unlock();
		return result;
				
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
		boolean removedSomething = false;
		try {
			for(Pair<Vehicle, Tunnel> pairs: TunnelAndVehicle) {
				if(pairs.getKey().equals(vehicle)) {
					removedSomething = true;
					pairs.getValue().exitTunnel(vehicle);
					maxPrioList.remove(vehicle);
					TunnelAndVehicle.removeFirstOccurrence(pairs);		
				}
			}
			if(maxPrioList.size() == 0) {
				maxWaitingPriority--;
			}
			if (removedSomething) {
				prioCond.signalAll();
			} else {
				//Something went wrong.
			}

			} finally {
				lock.unlock();
			}
		
	}
	
}
