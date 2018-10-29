package cs131.pa2.CarsTunnels;

import java.util.LinkedList;
import java.util.concurrent.locks.*;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;

public class BasicTunnel extends Tunnel{
	private final Lock lock = new ReentrantLock(); 
	private final Condition empty = lock.newCondition();
	
	private LinkedList<Vehicle> theTunnel = new LinkedList();
	
	private int activeCars;
	private int activeSled;
	private int waitingSleds;
	
	
	private boolean carsShouldPass() {//Returns true if cars should pass this tunnel
		return (activeCars > 2 || waitingSleds < 1 || activeSled < 1);
	}
	
	private boolean sledShouldWait() {//Returns true if sleds should pass this tunnel 
		return ((activeCars > 0 || activeSled > 0) && waitingSleds < 1);
	}
	
	private boolean sledShouldSkip() {
		return ((activeCars > 0 || activeSled > 0) && waitingSleds > 0);
	}
	

	public BasicTunnel(String name) {
		super(name);
	}

	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		lock.lock();
		if(vehicle instanceof Car) {
			if (carsShouldPass()) {
				activeCars++;
				return true;
			} else {
				lock.unlock();
				return false;
			}
		} else if (vehicle instanceof Sled) {
			if(sledShouldSkip()) {
				lock.unlock();
				return false;
			} else if (sledShouldWait()) {
				waitingSleds++;
				lock.unlock();
				return false;
			} else {
				activeSled++;
				return true;
			}
		}
		/*
		 * Check how many vehicles are in the tunnel
		 * if no room return false and unlock.
		 * 
		 * If there is room, put it in and unlock.
		 * 
		 */
		return false;
	}

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		lock.lock();
		if(vehicle instanceof Car) {
			lock.unlock();
			activeCars--;
		} else if (vehicle instanceof Sled) {
			lock.unlock();
			activeSled--;
		}
		
	}
	
}
