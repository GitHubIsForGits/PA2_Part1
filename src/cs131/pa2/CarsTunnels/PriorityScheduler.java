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
	private final Condition prioCond = enterLock.newCondition();
	
	private final Lock pairLock = new ReentrantLock();
	public ArrayList<Pair<Vehicle, Tunnel>> TunnelAndVehicle = new ArrayList();
	
	private final Lock prioListLock = new ReentrantLock();
	public ArrayList<Vehicle> prioWait = new ArrayList();
	
	

	public Collection<Tunnel> TunnelList = new ArrayList();

	
	int maxWaitingPriority = 0;
	
	public boolean gottaWait(Vehicle vehicle) {
		return (vehicle.getPriority() < maxWaitingPriority);
	}
	
	public boolean onWaitingList(Vehicle vehicle) {
		boolean answer = false;
		prioListLock.lock();
		try {
			answer = prioWait.contains(vehicle);
		} finally {
			prioListLock.unlock();
			return answer;
		}
	}
	
	

	PriorityScheduler(String name, Collection<Tunnel> c) {
		super(name);
		TunnelList = (ArrayList<Tunnel>)c;
	}

	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		enterLock.lock();
		boolean entered = false;
		try {
			while(!entered) {
				
				
				//If your cool enough to go right in
				if (!gottaWait(vehicle)&&!entered&&!onWaitingList(vehicle)) {
					pairLock.lock();
					try {
						for(Tunnel tunnel: TunnelList) {
							if(tunnel.tryToEnterInner(vehicle)) {
								TunnelAndVehicle.add(new Pair<Vehicle, Tunnel>(vehicle, tunnel));
								entered = true;
							}
						}
					} finally {
						pairLock.unlock();
					}
					if(!entered) {
						prioListLock.lock();
						try {
							prioWait.add(vehicle);
						} finally {
							prioListLock.unlock();
						}
					}
				} else if (onWaitingList(vehicle)&&!entered){
					pairLock.lock();
					try {
						for(Tunnel tunnel: TunnelList) {
							if(tunnel.tryToEnterInner(vehicle)) {
								prioListLock.lock();
								try {
									prioWait.remove(vehicle);
									} finally {
										if(prioWait.isEmpty()) {
											maxWaitingPriority = 0;
										}
										prioListLock.unlock();
									}
								TunnelAndVehicle.add(new Pair<Vehicle, Tunnel>(vehicle, tunnel));
								entered = true;
							}
						}
					} finally {
						pairLock.unlock();
					}
				}

				prioCond.await();
				
			}
			
		} finally {
			enterLock.unlock();
			return entered;
		}
	}
	

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		boolean removedSomething = false;
		pairLock.lock();
		try {
			Iterator <Pair<Vehicle, Tunnel>> iter = TunnelAndVehicle.iterator();
			while(iter.hasNext()) {
				System.out.println("Stuck in iterator loop");
				Pair<Vehicle, Tunnel> bingo = iter.next();
				if(bingo.getKey().equals(vehicle)) {
					removedSomething = true;
					bingo.getValue().exitTunnel(vehicle);
					TunnelAndVehicle.remove(bingo);	
				}
			}

			if (removedSomething) {
				prioCond.signalAll();
			} else {
				//Something went wrong.
			}

		} finally {
			pairLock.unlock();
		}
		
	}
	
}
