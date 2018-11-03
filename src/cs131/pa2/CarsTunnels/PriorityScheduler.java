package cs131.pa2.CarsTunnels;

import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;

public class PriorityScheduler extends Tunnel{
	private final Lock lock = new ReentrantLock(); 
	public Queue<Tunnel> TunnelList = new PriorityQueue();
	
	private int Cars = 0;
	private int Sleds = 0;
	private int Amberlamps = 0;
	
	

	public PriorityScheduler(String name, Collection<Tunnel> c) {
		super(name);
		TunnelList = (Queue<Tunnel>)c;
	}

	@Override
	public synchronized boolean tryToEnterInner(Vehicle vehicle) {
		lock.lock();
		Boolean result = false;
		
		int vPrio = vehicle.getPriority();
		Iterator<Tunnel> i = TunnelList.iterator();
		Tunnel temp = i.next();
		while (i.hasNext()) {
			if(temp instanceof BasicTunnel) {
				if(((BasicTunnel)temp).Priority < vPrio) {
					result = ((BasicTunnel) i).tryToEnterInner(vehicle);
				}
			}
			temp = i.next();
		} 
		
		if (result) {
			lock.unlock();
			return result;
		} 
	
		/*
		 * Check priority of every tunnel.
		 * If one of the tunnels has a priority =< the vehicles priority, call TargetTunnel.tryToEnterInner(true) then return true.
		 * If all the tunnels are high priority, make the vehicle wait
		 * When a tunnel empties, reset it's priority and wake all vehicles.
		 */
		
		
		
		
		return false;
	}

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		exitTunnelInner(vehicle);	
		Iterator<Tunnel> i = TunnelList.iterator();
		Tunnel temp = i.next();
		while (i.hasNext()) {
			if(temp instanceof BasicTunnel) {
				if( ((BasicTunnel)temp).activeCars == 0 && ((BasicTunnel)temp).activeSled == 0 ) {
					
					
				}
			}
		} 
	}
	
}
