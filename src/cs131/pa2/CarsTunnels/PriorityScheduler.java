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
	private final Lock enterLock = new ReentrantLock(); 
	
	private final Lock exitLock = new ReentrantLock();
	private final Lock pairLock = new ReentrantLock();
	private final Condition prioCond = enterLock.newCondition();//vehicle was at the highest priority
	private final Condition lowPrioCond = enterLock.newCondition();
	
	public ArrayList<Pair<Vehicle, Tunnel>> TunnelAndVehicle = new ArrayList();
	public Collection<Tunnel> TunnelList = new ArrayList();
	public Collection<Vehicle> maxPrioList = new PriorityQueue();
	
	int maxWaitingPriority = 0;
	
	Boolean gottaWait(Vehicle vehicle) {
		return (vehicle.getPriority() < maxWaitingPriority);
	}

	
	

	public PriorityScheduler(String name, Collection<Tunnel> c) {
		super(name);
		TunnelList = (ArrayList<Tunnel>)c;
	}

	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		enterLock.lock();
		try {
			if(vehicle.getPriority() >= maxWaitingPriority) {
				for(Tunnel tunnel: TunnelList) {
					if(tunnel.tryToEnterInner(vehicle)) {
						pairLock.lock();
						TunnelAndVehicle.add(new Pair<Vehicle, Tunnel>(vehicle, tunnel));
						pairLock.unlock();
						enterLock.unlock();
						return true;	
					}	
				}
			try {
				maxWaitingPriority = vehicle.getPriority();
				maxPrioList.add(vehicle);
				System.out.println("waiting on priority" + maxWaitingPriority);
				prioCond.await();
<<<<<<< HEAD
				} catch (InterruptedException e) {}
			}
=======
			} catch (InterruptedException e) {}
		}
			System.out.println("My priority" + vehicle.getPriority() + " Waiting priority" + maxWaitingPriority);
>>>>>>> branch 'master' of https://github.com/GitHubIsForGits/PA2_Part1.git
		
		
		
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
			
			while(gottaWait(vehicle)) {
				try {
					prioCond.await();
				} catch (InterruptedException e) {}
			} */
			
			
			
			
			
		} finally {
			enterLock.unlock();
			return false;
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
		exitLock.lock();
		boolean removedSomething = false;
		try {
			Iterator <Pair<Vehicle, Tunnel>> iter = TunnelAndVehicle.iterator();
			while(iter.hasNext()) {
				System.out.println("Stuck in iterator loop");
				Pair<Vehicle, Tunnel> bingo = iter.next();
				if(bingo.getKey().equals(vehicle)) {
					pairLock.lock();
					removedSomething = true;
					bingo.getValue().exitTunnel(vehicle);
					maxPrioList.remove(vehicle);
					TunnelAndVehicle.remove(bingo);	
					pairLock.unlock();
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
				exitLock.unlock();
			}
		
	}
	
}
