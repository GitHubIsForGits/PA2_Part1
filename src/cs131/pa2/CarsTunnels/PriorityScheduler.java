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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Log;
import javafx.util.Pair;

public class PriorityScheduler extends Tunnel{
	private final Lock enterLock = new ReentrantLock(); 
	private final Condition prioCond = enterLock.newCondition();
		
	private final ReentrantReadWriteLock VTLock = new ReentrantReadWriteLock();
	public HashMap<Vehicle, Tunnel> VehicleAndTunnel = new HashMap();
	
	private final ReentrantReadWriteLock prioLock = new ReentrantReadWriteLock();
	public ArrayList<Vehicle> prioWait = new ArrayList();
	
	private final ReentrantReadWriteLock TunnelLock = new ReentrantReadWriteLock();
	public HashMap<Tunnel, Lock> tunnelList = new HashMap<Tunnel, Lock>();
	

	
	int maxWaitingPriority = 0;
	
	public boolean gottaWait(Vehicle vehicle) {
		return (vehicle.getPriority() < maxWaitingPriority);
	}
	
	public boolean onWaitingList(Vehicle vehicle) {
		boolean answer = false;
		prioLock.readLock().lock();
		try {
			answer = prioWait.contains(vehicle);
		} finally {
			prioLock.readLock().unlock();
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
					TunnelLock.readLock().lock();
					try {
						Iterator it = tunnelList.entrySet().iterator();
						while (it.hasNext()) {
							Map.Entry<Tunnel, Lock> pair = (Map.Entry<Tunnel, Lock>)it.next();
							pair.getValue().lock();
							VTLock.writeLock().lock();
							try {
								if(pair.getKey().tryToEnter(vehicle)) {
									VehicleAndTunnel.put(vehicle, pair.getKey());
									entered = true;
								}		
							} finally {
								pair.getValue().unlock();
								VTLock.writeLock().unlock();
							}
						}
					} finally {
						TunnelLock.readLock().unlock();
					}
					//If you didn't enter, go into 
					if(!entered) {
						prioLock.writeLock().lock();
						try {
							prioWait.add(vehicle);
						} finally {
							prioLock.writeLock().unlock();
						}
					}
				} else if (onWaitingList(vehicle)&&!entered&&!gottaWait(vehicle)){
					TunnelLock.readLock().lock();
					try {
						Iterator it = tunnelList.entrySet().iterator();
						while (it.hasNext()) {
							Map.Entry<Tunnel, Lock> pair = (Map.Entry<Tunnel, Lock>)it.next();
							pair.getValue().lock();
							VTLock.writeLock().lock();
							try {
								if(pair.getKey().tryToEnter(vehicle)) {
									prioLock.writeLock().lock();
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
										pair.getValue().lock();
										prioLock.writeLock().unlock();
									}
									
									VehicleAndTunnel.put(vehicle, pair.getKey());
									entered = true;
								}		
							} finally {
								VTLock.writeLock().unlock();
							}
						}
					} finally {
						TunnelLock.readLock().unlock();
					}
	
				}
				
				if (!entered) {
					prioCond.await();
				}
				
				
			}
			
		} finally {
			enterLock.unlock();
			return entered;
		}
	}
	

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		boolean removedSomething = false;
		enterLock.lock();
		VTLock.writeLock().lock();
		try {
			Iterator iter = VehicleAndTunnel.entrySet().iterator();
			while(iter.hasNext()) {
				System.out.println("Stuck in iterator loop");
				Map.Entry<Vehicle, Tunnel> bingo = (Map.Entry<Vehicle, Tunnel>)iter.next();
				if(bingo.getKey().equals(vehicle)) {
					removedSomething = true;
					bingo.getValue().exitTunnel(vehicle);
					VehicleAndTunnel.remove(bingo.getKey(), bingo.getValue());	
				}
			}

			if (removedSomething) {
				prioCond.signal();
			} else {
				//Something went wrong.
			}

		} finally {
			enterLock.unlock();
			VTLock.writeLock().unlock();
		}
		
	}
	
}
