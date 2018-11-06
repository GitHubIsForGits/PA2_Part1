package cs131.pa2.CarsTunnels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Log;
import javafx.util.Pair;

public class PriorityScheduler extends Tunnel{
	private final Lock enterLock = new ReentrantLock(); 
	private final Condition prioCond = enterLock.newCondition();
	
	private final Lock VTLock = new ReentrantLock();
	public HashMap<Vehicle, Tunnel> VehicleAndTunnel = new HashMap();
	
	private final Lock prioLock = new ReentrantLock();
	public ArrayList<Vehicle> prioWait = new ArrayList();
	
	

	public HashMap<Tunnel, Lock> tunnelList = new HashMap<Tunnel, Lock>();
	private final Lock TunnelLock = new ReentrantLock();

	
	int maxWaitingPriority = 0;
	
	public boolean gottaWait(Vehicle vehicle) {
		return (vehicle.getPriority() < maxWaitingPriority);
	}
	
	public boolean onWaitingList(Vehicle vehicle) {
		boolean answer = false;
		prioLock.lock();
		try {
			answer = prioWait.contains(vehicle);
		} finally {
			prioLock.unlock();
			return answer;
		}
	}
	
	

	PriorityScheduler(String name, Collection<Tunnel> c, Log log) {
		super(name);
		for(Tunnel tunnel: c) {
			tunnelList.put(tunnel, new ReentrantLock());
		}
	}

	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		enterLock.lock();
		boolean entered = false;
		try {
			while(!entered) {
				//If your cool enough to go right in
				if (!gottaWait(vehicle)&&!entered&&!onWaitingList(vehicle)) {
					VTLock.lock();
					try {
						Iterator it = tunnelList.entrySet().iterator();
						while (it.hasNext()) {
							Map.Entry<Tunnel, Lock> pair = (Map.Entry<Tunnel, Lock>)it.next();
							pair.getValue().lock();
							TunnelLock.lock();
							try {
								if(pair.getKey().tryToEnter(vehicle)) {
									VehicleAndTunnel.put(vehicle, pair.getKey());
									entered = true;
								}		
							} finally {
								TunnelLock.unlock();
							}
						}
					} finally {
						VTLock.unlock();
					}
					if(!entered) {
						prioLock.lock();
						try {
							prioWait.add(vehicle);
						} finally {
							prioLock.unlock();
						}
					}
				} else if (onWaitingList(vehicle)&&!entered&&!gottaWait(vehicle)){
					TunnelLock.lock();
					try {
						Iterator it = tunnelList.entrySet().iterator();
						while (it.hasNext()) {
							Map.Entry<Tunnel, Lock> pair = (Map.Entry<Tunnel, Lock>)it.next();
							pair.getValue().lock();
							VTLock.lock();
							try {
								if(pair.getKey().tryToEnter(vehicle)) {
									prioLock.lock();
									try {
										prioWait.remove(vehicle);										
									} finally {
										int maxPrio = 0;
										for (Vehicle v: prioWait) {
											if (v.getPriority() > maxPrio) {
												maxPrio = v.getPriority();
											}
										}
										maxWaitingPriority = maxPrio;	
										prioLock.unlock();
									}
									
									VehicleAndTunnel.put(vehicle, pair.getKey());
									entered = true;
								}		
							} finally {
								VTLock.unlock();
							}
						}
					} finally {
						TunnelLock.unlock();
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
		VTLock.lock();
		try {
			Iterator iter = VehicleAndTunnel.entrySet().iterator();
			while(iter.hasNext()) {
				System.out.println("Stuck in iterator loop");
				Map.Entry<Tunnel, Vehicle> bingo = (Map.Entry<Tunnel, Vehicle>)iter.next();
				if(bingo.getValue().equals(vehicle)) {
					removedSomething = true;
					bingo.getKey().exitTunnel(vehicle);
					VehicleAndTunnel.remove(bingo);	
				}
			}

			if (removedSomething) {
				prioCond.signalAll();
			} else {
				//Something went wrong.
			}

		} finally {
			VTLock.unlock();
		}
		
	}
	
}
