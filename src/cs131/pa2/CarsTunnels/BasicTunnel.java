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
	private int waitingCars;
	private int waitingSleds;
	
	
	private boolean carsShouldPass() {//Returns true if cars should pass this tunnel
		return (activeCars > 2 || waitingSleds < 1 || activeSled < 1);
	}
	
	private boolean sledShouldPass() {//Returns true if sleds should pass this tunnel 
		return (activeCars > 0 || waitingSleds > 0 || activeSled > 0);
	}
	

	public BasicTunnel(String name) {
		super(name);
	}

	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		lock.lock();
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
		/*
		 * Remove a vehicle of vehicle type from the tunnel.
		 * Synchronize while removing?
		 */
		
	}
	
}
